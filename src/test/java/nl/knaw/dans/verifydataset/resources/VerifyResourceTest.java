/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.verifydataset.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import nl.knaw.dans.lib.dataverse.DatasetApi;
import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseResponse;
import nl.knaw.dans.lib.dataverse.model.dataset.DatasetVersion;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataBlock;
import nl.knaw.dans.verifydataset.api.VerifyRequest;
import nl.knaw.dans.verifydataset.api.VerifyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    void verifyRequest() {
        var citationBlock = readMdb("citation-mb.json");
        var spatialBlock = readMdb("spatial-mb.json");
        mockDataverse(citationBlock, spatialBlock);

        VerifyRequest req = new VerifyRequest();
        req.setDatasetPid("");

        var actual = EXT.target("/verify")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE), Response.class);
        assertEquals(200, actual.getStatus());
        // assertEquals("{}", actual.readEntity(String.class));
        VerifyResponse verifyResponse = actual.readEntity(VerifyResponse.class);
        var actualErrors = verifyResponse.getErrors();
        assertEquals(List.of(
            "author[2] (9999-0000-0001-2281-955X) is not a valid ORCID"
        ), actualErrors.get("identifierHasValidMod11"));
        assertEquals(List.of(
            "author[1] ('Barbapappa') does not match [A-Z][a-z]+, ([A-Z][.])+( [a-z]+)?"
        ), actualErrors.get("authorNameFormatOk"));
        assertEquals(List.of(
            "dansSpatialPoint[1] (x=0, y=0, scheme='RD (in m.)') does not conform to its scheme wich requires CoordinatesWithinBoundsConfig{minX=-7000, maxX=300000, minY=289000, maxY=629000}",
            "dansSpatialPoint[3] (x=null, y=null, scheme='null') has an invalid number and/or the scheme is not one of [longitude/latitude (degrees), RD, latlon, RD (in m.)]"
        ), actualErrors.get("coordinatesWithinBounds"));
    }

    @Test
    void withoutSpatial() {
        var citationBlock = readMdb("citation-mb.json");
        mockDataverse(citationBlock, null);

        VerifyRequest req = new VerifyRequest();
        req.setDatasetPid("");

        var actual = EXT.target("/verify")
            .request()
            .post(Entity.entity(req, MediaType.APPLICATION_JSON_TYPE), Response.class);
        assertEquals(200, actual.getStatus());
        assertEquals(
            "{\"errors\":{\"coordinatesWithinBounds\":[],\"authorNameFormatOk\":[\"author[1] ('Barbapappa') does not match [A-Z][a-z]+, ([A-Z][.])+( [a-z]+)?\",\"author[2] ('Barbapappa') does not match [A-Z][a-z]+, ([A-Z][.])+( [a-z]+)?\"],\"identifierHasValidMod11\":[\"author[2] (9999-0000-0001-2281-955X) is not a valid ORCID\"]}}",
            actual.readEntity(String.class));
    }

    private void mockDataverse(MetadataBlock citationBlock, MetadataBlock spatialBlock) {
        HashMap<String, MetadataBlock> map = new HashMap<>();
        map.put("citation", citationBlock);
        if (spatialBlock != null)
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
