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
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static nl.knaw.dans.lib.util.CheckDigit.validateMod11Two;

public class IdentifiersCanBeResolved extends MetadataRule {
    private static final Logger log = LoggerFactory.getLogger(IdentifiersCanBeResolved.class);
    private static final HttpClient client = HttpClients.createDefault();

    private final Map<String, String> schemeToUrlFormat;

    public IdentifiersCanBeResolved(Map<String, String> config) {
        blockName = "citation";
        fieldName = "author";
        this.schemeToUrlFormat = config;
    }

    @Override
    public String verifySingleField(Map<String, SingleValueField> attributes) {
        String scheme = attributes.getOrDefault("authorIdentifierScheme", defaultValue).getValue();
        String identifier = attributes.getOrDefault("authorIdentifier", defaultValue).getValue();
        if (!schemeToUrlFormat.containsKey(scheme) || identifier == null)
            return "";
        else {
            if (!validateMod11Two(identifier.replaceAll("-","")))
                return String.format("%s is not a valid %s", identifier, scheme);
            String url = schemeToUrlFormat.get(scheme).replace("{id}", identifier);
            log.debug("resolving " + url);
            try {
                HttpGet request = new HttpGet(new URI(url));
                request.setHeader("Accept", "application/xml"); // to get 404 by ORCID, default/ignored by ISNI
                HttpResponse resp = client.execute(request);
                int statusCode = resp.getStatusLine().getStatusCode();
                log.debug(statusCode + " returned for " + url);
                if (statusCode == 200)
                    // look for <srw:numberOfRecords> not found: orcid==ok, not 1: none or multiple ISNI's found
                    return "";
                else if (statusCode == 404)
                    return "Not found " + url;
                else
                    return String.format("Not expected response code %d for %s", statusCode, url);
            }
            catch (URISyntaxException | IOException e) {
                return url + " caused " + e;
            }
        }
    }
}
