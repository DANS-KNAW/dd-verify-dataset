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
import nl.knaw.dans.verifydataset.core.config.AuthorNameFormatOkConfig;

import java.util.Map;

public class AuthorNameFormatOk extends MetadataRule {
    String regexp;

    public AuthorNameFormatOk(AuthorNameFormatOkConfig authorNameFormatOkConfig) {
        blockName = "citation";
        fieldName = "author";
        regexp = authorNameFormatOkConfig.getRegexp();
    }

    @Override
    protected String verifySingleField(Map<String, SingleValueField> attributes) {
        String name = attributes.getOrDefault("authorName", defaultAttribute).getValue();
        if (name == null || name.matches(regexp))
            return "";
        else
            return String.format("author name '%s' does not match %s", name, regexp);
    }
}
