package app.uvsy.controllers.votes;

import app.uvsy.controllers.votes.payload.CreatePublicationVotePayload;
import app.uvsy.response.Response;
import app.uvsy.service.PublicationVoteService;
import org.github.serverless.api.annotations.HttpMethod;
import org.github.serverless.api.annotations.handler.Handler;
import org.github.serverless.api.annotations.parameters.BodyParameter;
import org.github.serverless.api.annotations.parameters.PathParameter;
import org.github.serverless.api.annotations.parameters.QueryParameter;

import java.util.Optional;

public class PublicationVoteController {

    private final PublicationVoteService voteService;


    public PublicationVoteController(PublicationVoteService voteService) {
        this.voteService = voteService;
    }

    public PublicationVoteController() {
        this(new PublicationVoteService());
    }

    @Handler(method = HttpMethod.GET, resource = "/v1/votes/publications")
    public Response getPublicationVotes(@QueryParameter(name = "publicationId") String publicationId,
                                        @QueryParameter(name = "userId", required = false) String userId) {

        if (Optional.ofNullable(userId).isPresent()) {
            return Response.of(voteService.getPublicationVotesForUser(publicationId, userId));
        }
        return Response.of(voteService.getPublicationVotes(publicationId));
    }

    @Handler(method = HttpMethod.POST, resource = "/v1/votes/publications")
    public void getPublicationVotes(@BodyParameter CreatePublicationVotePayload payload) {
        voteService.createPublicationVote(payload.getPublicationId(), payload.getUserId());
    }

    @Handler(method = HttpMethod.DELETE, resource = "/v1/votes/publications/{id}")
    public void getPublicationVotes(@PathParameter(name = "id") String voteId) {
        voteService.deleteVote(voteId);
    }
}
