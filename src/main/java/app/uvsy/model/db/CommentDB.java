package app.uvsy.model.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.sql.Timestamp;


@Data
@DatabaseTable(tableName = "comment")
public class CommentDB {

    public static final String PUBLICATION_ID_FIELD = "publication_id";

    @DatabaseField(columnName = "id", id = true, readOnly = true)
    private String id;

    @DatabaseField(columnName = "user_id")
    private String userId;

    @DatabaseField(columnName = "publication_id")
    private String publicationId;

    @DatabaseField(columnName = "content")
    private String content;

    @DatabaseField(columnName = "votes")
    private Integer votes;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;
}
