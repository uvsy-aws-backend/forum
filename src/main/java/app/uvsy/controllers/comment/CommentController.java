package app.uvsy.controllers.comment;

import app.uvsy.controllers.comment.payload.CreateCommentPayload;
import app.uvsy.controllers.comment.payload.UpdateCommentPayload;
import app.uvsy.response.PaginatedResponse;
import app.uvsy.response.Response;
import app.uvsy.service.CommentService;
import org.github.serverless.api.annotations.HttpMethod;
import org.github.serverless.api.annotations.handler.Handler;
import org.github.serverless.api.annotations.parameters.BodyParameter;
import org.github.serverless.api.annotations.parameters.PathParameter;
import org.github.serverless.api.annotations.parameters.QueryParameter;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    public CommentController() {
        this.commentService = new CommentService();
    }

    @Handler(method = HttpMethod.GET, resource = "/v1/comments")
    public PaginatedResponse getComments(@QueryParameter(name = "publicationId") String publicationId,
                                         @QueryParameter(name = "limit", required = false) Integer limit,
                                         @QueryParameter(name = "offset", required = false) Integer offset,
                                         @QueryParameter(name = "sort", required = false) List<String> sortBy,
                                         @QueryParameter(name = "includeAlias", required = false) Boolean includeAlias,
                                         @QueryParameter(name = "includeVoteForUserId", required = false) String userId) {
        return PaginatedResponse.of(commentService.getComments(
                publicationId,
                Optional.ofNullable(limit).orElse(10),
                Optional.ofNullable(offset).orElse(0),
                Optional.ofNullable(sortBy).orElse(Collections.emptyList()),
                Optional.ofNullable(includeAlias).orElse(Boolean.FALSE),
                Optional.ofNullable(userId).orElse("")
        ));
    }

    @Handler(method = HttpMethod.POST, resource = "/v1/comments")
    public void createComment(@BodyParameter CreateCommentPayload payload) {
        commentService.createComment(
                payload.getPublicationId(),
                payload.getUserId(),
                payload.getContent()
        );
    }

    @Handler(method = HttpMethod.GET, resource = "/v1/comments/{id}")
    public Response getComment(@PathParameter(name = "id") String commentId) {
        return Response.of(commentService.getComment(commentId));
    }

    @Handler(method = HttpMethod.PUT, resource = "/v1/comments/{id}")
    public void updateComment(@PathParameter(name = "id") String commentId,
                                  @BodyParameter UpdateCommentPayload payload) {
        commentService.updateComment(commentId, payload.getContent());
    }

    @Handler(method = HttpMethod.DELETE, resource = "/v1/comments/{id}")
    public void deleteComment(@PathParameter(name = "id") String commentId) {
        commentService.deleteComment(commentId);
    }
}
