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
import nl.knaw.dans.verifydataset.core.config.CoordinatesWithinBoundsConfig;
import nl.knaw.dans.verifydataset.core.rule.CoordinatesWithinBounds;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.knaw.dans.verifydataset.DataSupport.loadDistConfig;
import static nl.knaw.dans.verifydataset.DataSupport.readMdb;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CoordinatesWithinBoundsTest {

    @Test
    public void something() throws ConfigurationException, IOException {
        Map<String, CoordinatesWithinBoundsConfig> config = loadDistConfig().getCoordinatesWithinBounds();
        MetadataBlock mb = readMdb("spatial-mb.json");
        List<String> actual = new CoordinatesWithinBounds(config)
            .verify(Collections.singletonMap("dansTemporalSpatial", mb))
            .collect(Collectors.toList());
        assertEquals(List.of(
            "dansSpatialPoint(x=null, y=null, scheme=null) has an invalid number and/or the scheme is not one of [longitude/latitude (degrees), RD, latlon, RD (in m.)]",
            "dansSpatialPoint(x=0 y=0, scheme=RD (in m.)) does not comply to CoordinatesWithinBoundsConfig{minX=-7000, maxX=300000, minY=289000, maxY=629000}"
        ), actual);
    }
}
