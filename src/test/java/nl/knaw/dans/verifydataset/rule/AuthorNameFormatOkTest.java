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
import nl.knaw.dans.verifydataset.core.rule.AuthorNameFormatOk;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static nl.knaw.dans.verifydataset.DataSupport.loadDistConfig;
import static nl.knaw.dans.verifydataset.DataSupport.readMdb;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AuthorNameFormatOkTest {

    @Test
    public void something() throws ConfigurationException, IOException {
        var config = loadDistConfig().getAuthorNameFormatOk();
        MetadataBlock mb = readMdb("citation-mb.json");
        List<String> actual = new AuthorNameFormatOk(config)
            .verify(Collections.singletonMap("citation", mb));
        assertEquals(List.of(
            "author[1] ('Barbapappa') does not match [A-Z][a-z]+, ([A-Z][.])+( [a-z]+)?",
            "author[2] ('Barbapappa') does not match [A-Z][a-z]+, ([A-Z][.])+( [a-z]+)?"
        ), actual);
    }
}
