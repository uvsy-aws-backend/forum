package app.uvsy.model;

import app.uvsy.model.db.PublicationDB;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Publication {
    private String id;
    private String title;
    private String description;
    private String userId;
    private String userAlias;
    private String programId;
    private Integer votes;
    private Long comments;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<String> tags;

    public static Publication from(PublicationDB publicationDB) {
        Publication p = new Publication();
        p.setId(publicationDB.getId());
        p.setTitle(publicationDB.getTitle());
        p.setDescription(publicationDB.getDescription());
        p.setUserId(publicationDB.getUserId());
        p.setProgramId(publicationDB.getProgramId());
        p.setVotes(publicationDB.getVotes());
        p.setCreatedAt(publicationDB.getCreatedAt());
        p.setUpdatedAt(publicationDB.getUpdatedAt());
        return p;
    }
}
