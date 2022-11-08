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
package nl.knaw.dans.verifydataset.core.rule;

import nl.knaw.dans.lib.dataverse.model.dataset.SingleValueField;
import nl.knaw.dans.verifydataset.core.config.CoordinatesWithinBoundsConfig;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;

public class CoordinatesWithinBounds extends MetadataRule {
    private final Map<String, CoordinatesWithinBoundsConfig> config;

    public CoordinatesWithinBounds(Map<String, CoordinatesWithinBoundsConfig> config) {
        blockName = "dansTemporalSpatial";
        fieldName = "dansSpatialPoint";
        this.config = new HashMap<>();
        if (!config.keySet().containsAll(Set.of("RD", "latlon")))
            throw new IllegalStateException(String.format("Expecting at least schemes 'RD' and 'latlon' but got %s", config.keySet()));
        this.config.put("longitude/latitude (degrees)", config.get("latlon")); // TODO confusion between latlon and longitude/latitude
        this.config.put("RD (in m.)", config.get("RD"));
        this.config.putAll(config);
    }

    @Override
    public String verifySingleField(Map<String, SingleValueField> attributes) {
        String scheme = attributes.getOrDefault("dansSpatialPointScheme", defaultAttribute).getValue();
        String xs = attributes.getOrDefault("dansSpatialPointX", defaultAttribute).getValue();
        String ys = attributes.getOrDefault("dansSpatialPointY", defaultAttribute).getValue();
        var bounds = config.get(scheme);
        if (!NumberUtils.isParsable(xs) || !NumberUtils.isParsable(ys) || bounds == null)
            return format("dansSpatialPoint(x=%s, y=%s, scheme=%s) has an invalid number and/or the scheme is not one of %s", xs, ys, scheme, config.keySet());
        else {
            var x = NumberUtils.createNumber(xs).floatValue();
            var y = NumberUtils.createNumber(ys).floatValue();
            if (x < bounds.getMinX() || x > bounds.getMaxX() || y < bounds.getMinY() || y > bounds.getMaxY())
                return format("dansSpatialPoint(x=%s y=%s, scheme=%s) does not comply to %s", xs, ys, scheme, bounds);
            else
                return "";
        }
    }
}
