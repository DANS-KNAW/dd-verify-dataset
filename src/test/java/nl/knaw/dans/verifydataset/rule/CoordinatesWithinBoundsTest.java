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
package nl.knaw.dans.verifydataset.rule;

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
import nl.knaw.dans.verifydataset.core.config.CoordinatesWithinBoundsConfig;
import nl.knaw.dans.verifydataset.core.config.VerifyDatasetConfig;
import nl.knaw.dans.verifydataset.core.rule.CoordinatesWithinBounds;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinatesWithinBoundsTest {

    private ObjectMapper mdMapper;
    private final YamlConfigurationFactory<DdVerifyDatasetConfiguration> factory;

    {
        ObjectMapper mapper = Jackson.newObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        factory = new YamlConfigurationFactory<>(DdVerifyDatasetConfiguration.class, Validators.newValidator(), mapper, "dw");

        mdMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(MetadataField.class, new MetadataFieldDeserializer());
        module.addDeserializer(DataverseItem.class, new DataverseItemDeserializer());
        module.addDeserializer(ResultItem.class, new ResultItemDeserializer(mdMapper));
        mdMapper.registerModule(module);
    }

    @Test
    public void something() throws ConfigurationException, IOException {
        MetadataBlock mb = mdMapper.readValue(new File("src/test/resources/spatial-mb.json"), MetadataBlock.class);
        List<String> actual = new CoordinatesWithinBounds(loadDistConfig())
            .verify(Collections.singletonMap("dansTemporalSpatial", mb));
        assertEquals(List.of(
            "dansSpatialPoint(x=null, y=null, scheme=null) has an invalid number and/or the scheme is not one of [longitude/latitude (degrees), RD, latlon, RD (in m.)]",
            "dansSpatialPoint(x=0 y=0, scheme=RD (in m.)) does not comply to CoordinatesWithinBoundsConfig{minX=-7000, maxX=300000, minY=289000, maxY=629000}"
        ), actual);
    }

    private Map<String, CoordinatesWithinBoundsConfig> loadDistConfig() throws IOException, ConfigurationException {
        DdVerifyDatasetConfiguration appCfg = factory
            .build(FileInputStream::new, "src/main/assembly/dist/cfg/config.yml");
        VerifyDatasetConfig verifyCfg = appCfg.getVerifyDataset();
        return verifyCfg.getCoordinatesWithinBounds();
    }
}
