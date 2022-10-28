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

package nl.knaw.dans.verifydataset;

import io.dropwizard.Configuration;
import io.dropwizard.client.JerseyClientConfiguration;
import nl.knaw.dans.lib.util.DataverseClientFactory;
import nl.knaw.dans.verifydataset.core.config.VerifyDatasetConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class DdVerifyDatasetConfiguration extends Configuration {

    @Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @Valid
    @NotNull
    private DataverseClientFactory dataverse;

    @Valid
    @NotNull
    private VerifyDatasetConfig verifyDataset;

    public @Valid @NotNull DataverseClientFactory getDataverse() {
        return dataverse;
    }

    public void setDataverse(@Valid @NotNull DataverseClientFactory dataverse) {
        this.dataverse = dataverse;
    }

    public JerseyClientConfiguration getJerseyClient() {
        return jerseyClient;
    }

    public void setJerseyClient(JerseyClientConfiguration jerseyClient) {
        this.jerseyClient = jerseyClient;
    }

    public VerifyDatasetConfig getVerifyDataset() {
        return verifyDataset;
    }

    public void setVerifyDataset(VerifyDatasetConfig verifyDataset) {
        this.verifyDataset = verifyDataset;
    }
}
