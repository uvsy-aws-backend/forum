package app.uvsy.model;

import app.uvsy.model.db.CommentDB;
import app.uvsy.model.db.CommentVoteDB;
import app.uvsy.model.db.PublicationVoteDB;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.sql.Timestamp;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Comment {

    private String id;
    private String userId;
    private String userAlias;
    private String publicationId;
    private String content;
    private Integer votes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private CommentVoteDB vote;


    public static Comment from(CommentDB commentDB) {
        Comment c = new Comment();
        c.setId(commentDB.getId());
        c.setPublicationId(commentDB.getPublicationId());
        c.setUserId(commentDB.getUserId());
        c.setContent(commentDB.getContent());
        c.setVotes(commentDB.getVotes());
        c.setCreatedAt(commentDB.getCreatedAt());
        c.setUpdatedAt(commentDB.getUpdatedAt());
        return c;
    }
}
