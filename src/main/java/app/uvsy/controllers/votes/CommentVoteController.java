package app.uvsy.controllers.votes;

import app.uvsy.controllers.votes.payload.CreateCommentVotePayload;
import app.uvsy.response.Response;
import app.uvsy.service.CommentVoteService;
import org.github.serverless.api.annotations.HttpMethod;
import org.github.serverless.api.annotations.handler.Handler;
import org.github.serverless.api.annotations.parameters.BodyParameter;
import org.github.serverless.api.annotations.parameters.PathParameter;
import org.github.serverless.api.annotations.parameters.QueryParameter;


public class CommentVoteController {

    private final CommentVoteService voteService;


    public CommentVoteController(CommentVoteService voteService) {
        this.voteService = voteService;
    }

    public CommentVoteController() {
        this(new CommentVoteService());
    }

    @Handler(method = HttpMethod.GET, resource = "/v1/votes/comments")
    public Response getCommentVotes(@QueryParameter(name = "publicationId") String publicationId,
                                    @QueryParameter(name = "userId") String userId) {

        return Response.of(voteService.getCommentVotesForUser(publicationId, userId));
    }

    @Handler(method = HttpMethod.POST, resource = "/v1/votes/comments")
    public void getCommentVotes(@BodyParameter CreateCommentVotePayload payload) {
        voteService.createCommentVote(payload.getCommentId(), payload.getUserId());
    }

    @Handler(method = HttpMethod.DELETE, resource = "/v1/votes/comments/{id}")
    public void getCommentVotes(@PathParameter(name = "id") String voteId) {
        voteService.deleteVote(voteId);
    }
}
