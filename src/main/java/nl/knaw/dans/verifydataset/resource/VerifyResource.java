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
package nl.knaw.dans.verifydataset.resource;

import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.verifydataset.api.VerifyRequest;
import nl.knaw.dans.verifydataset.api.VerifyResponse;
import nl.knaw.dans.verifydataset.core.config.VerifyDatasetConfig;
import nl.knaw.dans.verifydataset.core.rule.MetadataRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class VerifyResource {
    private static final Logger log = LoggerFactory.getLogger(VerifyResource.class);
    private final DataverseClient dataverse;
    private final List<MetadataRule> rules;

    public VerifyResource(DataverseClient dataverse, VerifyDatasetConfig config) {
        this.dataverse = dataverse;
        rules = MetadataRule.configureRules(config);
    }

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response verify(VerifyRequest req) {
        try {
            log.info("Verifying " + req);
            var blocks = dataverse
                .dataset(req.getDatasetPid())
                .getVersion().getData().getMetadataBlocks();
            var messages = rules.stream()
                .flatMap(rule -> rule.verify(blocks))
                .collect(Collectors.toList());
            // ok->accepted when we change to asynchronous
            return Response.ok(new VerifyResponse(messages)).build();
        }
        catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
        catch (DataverseException e) {
            if (e.getStatus() == 404)
                throw new NotFoundException();
            else
                throw new InternalServerErrorException(e);
        }
    }
}
