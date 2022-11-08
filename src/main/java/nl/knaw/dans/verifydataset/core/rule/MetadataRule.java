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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class MetadataRule {
    String blockName;
    String fieldName;

    protected static final PrimitiveSingleValueField defaultAttribute = new PrimitiveSingleValueField();

    protected abstract String verifySingleField(Map<String, SingleValueField> attributes);

    public final List<String> verify(Map<String, MetadataBlock> mdBlocks) {
        return mdBlocks.get(blockName)
            .getFields().stream()
            .filter(f -> f.getTypeName().equals(fieldName))
            .filter(f -> f instanceof CompoundField)
            .flatMap(f -> (((CompoundField) f).getValue()).stream())
            .map(this::verifySingleField)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
    }
}
