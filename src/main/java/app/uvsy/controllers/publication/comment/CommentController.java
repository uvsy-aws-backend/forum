package app.uvsy.controllers.publication.comment;

import app.uvsy.controllers.publication.payload.CreateCommentPayload;
import app.uvsy.response.PaginatedResponse;
import app.uvsy.response.Response;
import app.uvsy.service.PublicationService;
import org.github.serverless.api.annotations.HttpMethod;
import org.github.serverless.api.annotations.handler.Handler;
import org.github.serverless.api.annotations.parameters.BodyParameter;
import org.github.serverless.api.annotations.parameters.PathParameter;
import org.github.serverless.api.annotations.parameters.QueryParameter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class CommentController {

    private final PublicationService publicationService;

    public CommentController() {
        this.publicationService = new PublicationService();
    }

    public CommentController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

/*    @Handler(method = HttpMethod.GET, resource = "/v1/publications/{id}")
    public Response getPublication(@PathParameter(name = "id") String publicationId) {
        return Response.of(publicationService.getPublication(publicationId));
    }

    @Handler(method = HttpMethod.GET, resource = "/v1/publications")
    public PaginatedResponse getPublications(@QueryParameter(name = "programId", required = false) String programId,
                                             @QueryParameter(name = "limit", required = false) Integer limit,
                                             @QueryParameter(name = "offset", required = false) Integer offset,
                                             @QueryParameter(name = "tags", required = false) List<String> tags,
                                             @QueryParameter(name = "tagsOperator", required = false) String tagsOperator,
                                             @QueryParameter(name = "sort", required = false) List<String> sortBy,
                                             @QueryParameter(name = "includeTags", required = false) Boolean includeTags,
                                             @QueryParameter(name = "includeAlias", required = false) Boolean includeAlias) {
        return PaginatedResponse.of(publicationService.getPublications(
                Optional.ofNullable(programId).orElse(""),
                Optional.ofNullable(limit).orElse(10),
                Optional.ofNullable(offset).orElse(0),
                Optional.ofNullable(tags).orElse(Collections.emptyList()),
                Optional.ofNullable(tagsOperator).orElse("OR"),
                Optional.ofNullable(sortBy).orElse(Collections.emptyList()),
                Optional.ofNullable(includeTags).orElse(Boolean.FALSE),
                Optional.ofNullable(includeAlias).orElse(Boolean.FALSE)
        ));
    }

    @Handler(method = HttpMethod.POST, resource = "/v1/publications")
    public void createPublication(@BodyParameter CreateCommentPayload payload) {
        publicationService.createPublication(
                payload.getTitle(),
                payload.getDescription(),
                payload.getProgramId(),
                payload.getUserId(),
                Optional.ofNullable(payload.getTags()).orElseGet(ArrayList::new)
        );
    }*/

}
