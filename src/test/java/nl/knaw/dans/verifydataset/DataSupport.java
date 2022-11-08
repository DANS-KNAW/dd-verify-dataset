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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import nl.knaw.dans.lib.dataverse.DataverseItemDeserializer;
import nl.knaw.dans.lib.dataverse.MetadataFieldDeserializer;
import nl.knaw.dans.lib.dataverse.ResultItemDeserializer;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataBlock;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataField;
import nl.knaw.dans.lib.dataverse.model.dataverse.DataverseItem;
import nl.knaw.dans.lib.dataverse.model.search.ResultItem;
import nl.knaw.dans.verifydataset.DdVerifyDatasetConfiguration;
import nl.knaw.dans.verifydataset.core.config.VerifyDatasetConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

public class DataSupport {

    private static final ObjectMapper mdMapper = createObjectMapper();
    private static final YamlConfigurationFactory<DdVerifyDatasetConfiguration> factory = createFactory();

    private static ObjectMapper createObjectMapper() {
        var mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MetadataField.class, new MetadataFieldDeserializer());
        module.addDeserializer(DataverseItem.class, new DataverseItemDeserializer());
        module.addDeserializer(ResultItem.class, new ResultItemDeserializer(mdMapper));
        mapper.registerModule(module);
        return mapper;
    }

    private static YamlConfigurationFactory<DdVerifyDatasetConfiguration> createFactory() {
        ObjectMapper mapper = Jackson.newObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return new YamlConfigurationFactory<>(DdVerifyDatasetConfiguration.class, Validators.newValidator(), mapper, "dw");
    }

    public static MetadataBlock readMdb(String testResource) {
        try {
            return mdMapper.readValue(new File("src/test/resources/" + testResource), MetadataBlock.class);
        }
        catch (IOException e) {
            return fail(e);
        }
    }

    public static VerifyDatasetConfig loadDistConfig() {
        try {
            return factory
                .build(FileInputStream::new, "src/main/assembly/dist/cfg/config.yml")
            .getVerifyDataset();
        }
        catch (IOException | ConfigurationException e) {
            return fail(e);
        }
    }
}
