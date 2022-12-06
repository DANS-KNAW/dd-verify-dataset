package nl.knaw.dans.verifydataset.core;

import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.file.FileMeta;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.List;

public class DataverseClientWrapper {
    private final DataverseClient dataverseClient;

    public DataverseClientWrapper(DataverseClient dataverseClient) {
        this.dataverseClient = dataverseClient;
    }

    protected List<FileMeta> getFiles(String pid) throws IOException, DataverseException {
        return dataverseClient
            .dataset(pid).getLatestVersion().getData().getLatestVersion()
            .getFiles();
    }

    protected HttpResponse getFile(int fileId) throws DataverseException, IOException {
        return dataverseClient.basicFileAccess(fileId).getFile();
    }
}
