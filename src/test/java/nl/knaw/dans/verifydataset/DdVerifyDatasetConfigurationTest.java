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
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DdVerifyDatasetConfigurationTest {

    private final YamlConfigurationFactory<DdVerifyDatasetConfiguration> factory;

    {
        ObjectMapper mapper = Jackson.newObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        factory = new YamlConfigurationFactory<>(DdVerifyDatasetConfiguration.class, Validators.newValidator(), mapper, "dw");
    }

    @Test
    public void canReadDist() throws IOException, ConfigurationException {
        var config = factory.build(FileInputStream::new, "src/main/assembly/dist/cfg/config.yml");
        assertEquals(629000, Objects.requireNonNull(config.getVerifyDataset().getCoordinatesWithinBounds().get("RD")).getMaxY());
        assertArrayEquals(
            List.of("https://orcid.org/{id}", "https://www.isni.org/isni/{id}").toArray(),
            config.getVerifyDataset().getIdentifiersCanBeResolved().values().toArray()
        );
    }

    @Test
    public void canReadTest() throws IOException, ConfigurationException {
        factory.build(new ResourceConfigurationSourceProvider(), "debug-etc/config.yml");
    }
}
