package app.uvsy.model.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
@DatabaseTable(tableName = "comment_vote")
public class CommentVoteDB {
    public static final String COMMENT_ID_FIELD = "comment_id";
    public static final String USER_ID_FIELD = "user_id";

    @DatabaseField(columnName = "id", id = true, readOnly = true)
    private String id;

    @DatabaseField(columnName = "user_id")
    private String userId;

    @DatabaseField(columnName = "comment_id")
    private String commentId;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;

    public CommentVoteDB() {
    }

    public CommentVoteDB(String commentId, String userId) {
        this.userId = userId;
        this.commentId = commentId;
    }
}
