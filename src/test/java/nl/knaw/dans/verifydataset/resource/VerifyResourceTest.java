package nl.knaw.dans.verifydataset.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseResponse;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataBlock;
import nl.knaw.dans.verifydataset.api.VerifyRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static nl.knaw.dans.verifydataset.DataSupport.loadDistConfig;
import static nl.knaw.dans.verifydataset.DataSupport.readMdb;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(DropwizardExtensionsSupport.class)
public class VerifyResourceTest {

    DataverseClient dataverse = Mockito.mock(DataverseClient.class);
    public final ResourceExtension EXT = ResourceExtension.builder()
        .addResource(new VerifyResource(dataverse, loadDistConfig()))
        .build();

    @BeforeEach
    void setup() {
        Mockito.reset(dataverse);
    }

    @Test
    void VerifyMethod() throws ConfigurationException, IOException {
        MetadataBlock citationBlock = readMdb("citation-mb.json");
        MetadataBlock spatialBlock = readMdb("spatial-mb.json");
        mockDataverse(citationBlock, spatialBlock);

        VerifyRequest req = new VerifyRequest();
        req.setDatasetPid("");

        var actual = new VerifyResource(dataverse, loadDistConfig())
            .verify(req);
        assertEquals(List.of(
            "dansSpatialPoint(x=null, y=null, scheme=null) has an invalid number and/or the scheme is not one of [longitude/latitude (degrees), RD, latlon, RD (in m.)]",
            "dansSpatialPoint(x=0 y=0, scheme=RD (in m.)) does not comply to CoordinatesWithinBoundsConfig{minX=-7000, maxX=300000, minY=289000, maxY=629000}"
        ), actual);
    }

    @Test
    void VerifyRequest() throws ConfigurationException, IOException {
        var citationBlock = readMdb("citation-mb.json");
        var spatialBlock = readMdb("spatial-mb.json");
        mockDataverse(citationBlock, spatialBlock);
        var json = Entity.entity("{ 'datasetPId': 'doi:...'}", MediaType.APPLICATION_JSON_TYPE);

        var r = EXT.target("/")
            .request()
            .post(json, Response.class);
        assertEquals(200, r.getStatus());
    }

    private void mockDataverse(MetadataBlock citationBlock, MetadataBlock spatialBlock) {
        HashMap<String, MetadataBlock> map = new HashMap<>();
        map.put("citation", citationBlock);
        map.put("dansTemporalSpatial", spatialBlock);
        var dv = new DatasetVersion();
        dv.setMetadataBlocks(map);
        var datasetApi = new DatasetApi(null, "", false) {

            @Override
            public DataverseResponse<DatasetVersion> getVersion() {
                return new DataverseResponse<>("", new ObjectMapper(), DatasetVersion.class) {

                    @Override
                    public DatasetVersion getData() {
                        return dv;
                    }
                };
            }
        };
        Mockito.doReturn(datasetApi)
            .when(dataverse).dataset(Mockito.any(String.class));
    }
}