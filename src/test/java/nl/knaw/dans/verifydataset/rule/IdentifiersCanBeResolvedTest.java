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

import io.dropwizard.configuration.ConfigurationException;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataBlock;
import nl.knaw.dans.verifydataset.core.rule.IdentifiersCanBeResolved;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static nl.knaw.dans.verifydataset.rule.MetadataRuleSupport.loadDistConfig;
import static nl.knaw.dans.verifydataset.rule.MetadataRuleSupport.mdMapper;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class IdentifiersCanBeResolvedTest {

    @Test
    public void something() throws ConfigurationException, IOException {
        Map<String, String> config = loadDistConfig().getIdentifiersCanBeResolved();
        MetadataBlock mb = mdMapper.readValue(new File("src/test/resources/citation-mb.json"), MetadataBlock.class);
        List<String> actual = new IdentifiersCanBeResolved(config)
            .verify(Collections.singletonMap("citation", mb));
        assertEquals(List.of("Not found https://orcid.org/0000-0001-2281-955X"), actual);
    }
}
