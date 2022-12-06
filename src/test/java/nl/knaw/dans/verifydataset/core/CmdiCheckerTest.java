package nl.knaw.dans.verifydataset.core;

import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.file.DataFile;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import nl.knaw.dans.verifydataset.api.CmdiResponse;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicStatusLine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CmdiCheckerTest {
    private final DataverseClientWrapper dataverse = Mockito.mock(DataverseClientWrapper.class);

    @BeforeEach
    void setup() {
        Mockito.reset(dataverse);
    }

    @Test
    void notFoundException() throws IOException, DataverseException {
        Mockito.doThrow(new DataverseException(404, "not found", null))
            .when(dataverse).getFiles("");

        assertThrows(DataverseException.class, () -> new CmdiChecker(dataverse).find(""));
    }

    @Test
    void no() throws IOException, DataverseException {
        var fm = new FileMeta();
        DataFile df = new DataFile();
        fm.setDataFile(df);
        fm.setLabel("nothing.txt");
        df.setContentType("text");
        df.setId(1);
        Mockito.doReturn(List.of(fm)).when(dataverse).getFiles("");

        CmdiResponse expected = new CmdiResponse();
        expected.setStatus("no");

        CmdiResponse actualResponse = new CmdiChecker(dataverse).find("");
        assertEquals(expected, actualResponse);
    }

    @Test
    void unknownNotFound() throws IOException, DataverseException {
        var fm = new FileMeta();
        DataFile df = new DataFile();
        fm.setDataFile(df);
        fm.setLabel("nothing.cmdi");
        df.setContentType("text");
        df.setId(1);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        BasicStatusLine statusLine = new BasicStatusLine(new HttpVersion(0, 0), 404, "not found");
        Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
        Mockito.doReturn(List.of(fm)).when(dataverse).getFiles("");
        Mockito.doReturn(httpResponse).when(dataverse).getFile(1);

        CmdiResponse expected = new CmdiResponse();
        expected.setStatus("unknown");
        // as if nothing.cmdi was deleted after getting the dataset metadata with list of files
        expected.getErrorMessages().add("fileID=1 nothing.cmdi CAUSED not found");

        CmdiResponse actualResponse = new CmdiChecker(dataverse).find("");
        assertEquals(expected, actualResponse);
    }
    @Test

    void yes() throws IOException, DataverseException {
        var fm = new FileMeta();
        DataFile df = new DataFile();
        fm.setDataFile(df);
        fm.setLabel("something.cmdi");
        fm.setDirectoryLabel("something");
        df.setContentType("text");
        df.setId(1);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        BasicStatusLine statusLine = new BasicStatusLine(new HttpVersion(0, 0), 200, "ok");
        String s = "<CMD CMDVersion=\"1.1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "     xsi:schemaLocation=\"http://www.clarin.eu/cmd/ http://catalog.clarin.eu/ds/ComponentRegistry/rest/registry/profiles/clarin.eu:cr1:p_1369752611610/xsd\" xmlns=\"http://www.clarin.eu/cmd/\"></CMD>\n";
        InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream(s.getBytes()));
        Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        Mockito.doReturn(List.of(fm)).when(dataverse).getFiles("");
        Mockito.doReturn(httpResponse).when(dataverse).getFile(1);

        CmdiResponse expected = new CmdiResponse();
        expected.setStatus("yes");
        expected.getCmdiFiles().add("something/something.cmdi");

        CmdiResponse actualResponse = new CmdiChecker(dataverse).find("");
        assertEquals(expected, actualResponse);
    }

    @Test
    void empty() throws IOException, DataverseException {
        var fm = new FileMeta();
        DataFile df = new DataFile();
        fm.setDataFile(df);
        fm.setLabel("something.cmdi");
        fm.setDirectoryLabel("something");
        df.setContentType("text");
        df.setId(1);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        BasicStatusLine statusLine = new BasicStatusLine(new HttpVersion(0, 0), 200, "ok");
        InputStreamEntity entity = new InputStreamEntity(new ByteArrayInputStream("".getBytes()));
        Mockito.doReturn(statusLine).when(httpResponse).getStatusLine();
        Mockito.doReturn(entity).when(httpResponse).getEntity();
        Mockito.doReturn(List.of(fm)).when(dataverse).getFiles("");
        Mockito.doReturn(httpResponse).when(dataverse).getFile(1);

        CmdiResponse expected = new CmdiResponse();
        expected.setStatus("unknown");
        expected.getErrorMessages().add("fileID=1 something/something.cmdi CAUSED org.xml.sax.SAXParseException; lineNumber: 1; columnNumber: 1; Premature end of file.");

        CmdiResponse actualResponse = new CmdiChecker(dataverse).find("");
        assertEquals(expected, actualResponse);
    }
}
