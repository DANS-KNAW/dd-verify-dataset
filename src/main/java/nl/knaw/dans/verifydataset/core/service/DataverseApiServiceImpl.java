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
package nl.knaw.dans.verifydataset.core.service;

import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataBlock;
import nl.knaw.dans.lib.dataverse.model.dataverse.Dataverse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.util.Map;

public class DataverseApiServiceImpl implements nl.knaw.dans.verifydataset.core.service.DataverseApiService {
    private static final Logger log = LoggerFactory.getLogger(DataverseApiServiceImpl.class);

    private final Dataverse dataverse;
    private final Client httpClient;

    private DataverseClient client;

    public DataverseApiServiceImpl(Dataverse dataverse, Client client) {
        this.dataverse = dataverse;
        this.httpClient = client;
    }

    DataverseClient getDataverseClient() {
        return client;
    }

    @Override
    public Map<String, MetadataBlock> getMetadataBlocks(int datasetId) throws IOException, DataverseException {
        log.trace("Getting dataset with id {}", datasetId);
        return getDataverseClient().dataset(datasetId).getVersion().getData().getMetadataBlocks();
    }
}
