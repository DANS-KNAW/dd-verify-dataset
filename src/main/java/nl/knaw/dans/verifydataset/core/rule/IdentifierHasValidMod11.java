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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.knaw.dans.lib.util.CheckDigit.validateMod11Two;

public class IdentifierHasValidMod11 extends MetadataRule {
    private final List<String> schemes;

    public IdentifierHasValidMod11(String[] config) {
        blockName = "citation";
        fieldName = "author";
        this.schemes = Arrays.stream(config).collect(Collectors.toList());
    }

    @Override
    public String verifySingleField(Map<String, SingleValueField> attributes, int fieldNr) {
        String scheme = attributes.getOrDefault("authorIdentifierScheme", defaultAttribute).getValue();
        String identifier = attributes.getOrDefault("authorIdentifier", defaultAttribute).getValue();
        if (!schemes.contains(scheme))
            return "";
        else if (identifier == null || !validateMod11Two(identifier.replaceAll("-", "")))
            return String.format("%s[%d] (%s) is not a valid %s", fieldName, fieldNr, identifier, scheme);
        else
            return "";
    }
}
