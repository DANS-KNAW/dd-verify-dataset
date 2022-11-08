package nl.knaw.dans.verifydataset.resource;

import nl.knaw.dans.lib.dataverse.DataverseClient;
import nl.knaw.dans.lib.dataverse.DataverseException;
import nl.knaw.dans.verifydataset.api.VerifyRequest;
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
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
    public List<String> verify(VerifyRequest req) {
        try {
            var blocks = dataverse
                .dataset(req.getDatasetPid())
                .getVersion().getData().getMetadataBlocks();
            return rules.stream()
                .flatMap(rule -> rule.verify(blocks))
                .collect(Collectors.toList());
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
