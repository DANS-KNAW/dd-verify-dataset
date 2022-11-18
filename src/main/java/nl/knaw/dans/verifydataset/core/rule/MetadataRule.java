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
import nl.knaw.dans.verifydataset.core.config.VerifyDatasetConfig;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class MetadataRule {
    String blockName;
    String fieldName;

    protected static final PrimitiveSingleValueField defaultAttribute = new PrimitiveSingleValueField();
    private static final List<String> stringLinkedList = new LinkedList<>();

    protected abstract String verifySingleField(Map<String, SingleValueField> attributes, int n);

    public final List<String> verify(Map<String, MetadataBlock> mdBlocks) {
        int n = 0;
        if (!mdBlocks.containsKey(blockName))
            return stringLinkedList;
        else {
            AtomicInteger index = new AtomicInteger();
            return mdBlocks.get(blockName)
                .getFields().stream()
                .filter(f -> f.getTypeName().equals(fieldName))
                .filter(f -> f instanceof CompoundField)
                .flatMap(f -> (((CompoundField) f).getValue()).stream())
                .map(f -> verifySingleField(f, index.incrementAndGet()))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        }
    }

    public static Map<String, MetadataRule> configureRules(VerifyDatasetConfig config) {
        HashMap<String, MetadataRule> rules = new HashMap<>();
        rules.put("coordinatesWithinBounds", new CoordinatesWithinBounds(config.getCoordinatesWithinBounds()));
        rules.put("identifierHasValidMod11", new IdentifierHasValidMod11(config.getIdentifierHasValidMod11()));
        rules.put("authorNameFormatOk", new AuthorNameFormatOk(config.getAuthorNameFormatOk()));
        return rules;
    }
}
