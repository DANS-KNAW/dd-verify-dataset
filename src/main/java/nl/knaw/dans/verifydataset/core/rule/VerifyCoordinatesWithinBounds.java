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

import nl.knaw.dans.lib.dataverse.model.dataset.CompoundField;
import nl.knaw.dans.lib.dataverse.model.dataset.MetadataBlock;
import nl.knaw.dans.lib.dataverse.model.dataset.PrimitiveSingleValueField;
import nl.knaw.dans.lib.dataverse.model.dataset.SingleValueField;
import nl.knaw.dans.verifydataset.core.config.CoordinatesWithinBoundsConfig;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class VerifyCoordinatesWithinBounds implements VerifyDatasetMetadata {
    private static final PrimitiveSingleValueField defaultValue = new PrimitiveSingleValueField();

    private final Map<String, CoordinatesWithinBoundsConfig> config;

    public VerifyCoordinatesWithinBounds(Map<String, CoordinatesWithinBoundsConfig> config) {
        this.config = new HashMap<>();
        if (!config.keySet().containsAll(Set.of("RD", "latlon")))
            throw new IllegalStateException(String.format("Expecting at least schemes 'RD' and 'latlon' but got %s", config.keySet()));
        this.config.put("longitude/latitude (degrees)", config.get("latlon")); // TODO confusion between latlon and longitude/latitude
        this.config.put("RD (in m.)", config.get("RD"));
        this.config.putAll(config);
    }

    public List<String> verify(Map<String, MetadataBlock> mdBlocks) {
        List<String> messages = new LinkedList<>();
        var mdBlock = mdBlocks.get("dansTemporalSpatial");
        mdBlock.getFields().stream()
            .filter(f -> f.getTypeName().equals("dansSpatialPoint"))
            .filter(f -> f instanceof CompoundField)
            .map(f -> ((CompoundField) f).getValue())
            .forEach(p -> messages.addAll(verifyPoints(p)));
        return messages;
    }

    private List<String> verifyPoints(List<Map<String, SingleValueField>> points) {
        return points.stream()
            .map(this::verifySinglePoint)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }

    private String verifySinglePoint(Map<String, SingleValueField> attributes) {
        String scheme = attributes.getOrDefault("dansSpatialPointScheme", defaultValue).getValue();
        String xs = attributes.getOrDefault("dansSpatialPointX", defaultValue).getValue();
        String ys = attributes.getOrDefault("dansSpatialPointY", defaultValue).getValue();
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
