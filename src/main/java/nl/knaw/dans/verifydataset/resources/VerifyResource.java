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
package nl.knaw.dans.verifydataset.resources;

import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.verifydataset.api.VerifyRequest;
import nl.knaw.dans.verifydataset.api.RuleResponse;
import nl.knaw.dans.verifydataset.core.CmdiChecker;
import nl.knaw.dans.verifydataset.core.config.VerifyDatasetConfig;
import nl.knaw.dans.verifydataset.core.rule.MetadataRule;
import org.apache.commons.lang3.StringUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class VerifyResource {
    private static final Logger log = LoggerFactory.getLogger(VerifyResource.class);
    private final DataverseClient dataverse;
    private final CmdiChecker cmdiChecker;
    private final Map<String, MetadataRule> rules;

    public VerifyResource(DataverseClient dataverse, VerifyDatasetConfig config) {
        this.dataverse = dataverse;
        cmdiChecker = new CmdiChecker(dataverse);
        rules = MetadataRule.configureRules(config);
    }

    @POST
    @Path("/verify/check-cmdi")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response verifyCMDI(VerifyRequest req) {
        if (StringUtils.isBlank(req.getDatasetPid()))
            return missingPidResponse();
        try {
            log.info("Looking for CMDI file(s) " + req);
            var files = cmdiChecker.find(req.getDatasetPid());
            return Response.ok(files).build();
        }
        catch (DataverseException e) {
            return responseFromDataverseException(e);
        }
        catch (Exception e) {
            throw new InternalServerErrorException(e);
        }
    }

    @POST
    @Path("/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public Response verify(VerifyRequest req) {
        if (StringUtils.isBlank(req.getDatasetPid()))
            return missingPidResponse();
        try {
            log.info("Verifying " + req);
            var blocks = dataverse
                .dataset(req.getDatasetPid())
                .getVersion().getData().getMetadataBlocks();
            HashMap<String, List<String>> messages = new HashMap<>();
            rules.forEach((name, rule) -> messages.put(name, rule.verify(blocks)));
            // ok->accepted when we change to asynchronous
            return Response.ok(new RuleResponse(messages)).build();
        }
        catch (IOException e) {
            throw new InternalServerErrorException(e);
        }
        catch (DataverseException e) {
            return responseFromDataverseException(e);
        }
    }

    private Response responseFromDataverseException(DataverseException e) {
        if (e.getStatus() == 404)
            throw new NotFoundException();
        else
            throw new InternalServerErrorException(e);
    }

    private Response missingPidResponse() {
        return Response
            .status(Response.Status.BAD_REQUEST)
            .entity("Field 'datasetPid' is mandatory")
            .build();
    }
}
