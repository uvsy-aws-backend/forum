package app.uvsy.model.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
@DatabaseTable(tableName = "publication_vote")
public class PublicationVoteDB {
    public static final String PUBLICATION_ID_FIELD = "publication_id";
    public static final String USER_ID_FIELD = "user_id";

    @DatabaseField(columnName = "id", id = true, readOnly = true)
    private String id;

    @DatabaseField(columnName = "user_id")
    private String user_id;

    @DatabaseField(columnName = "publication_id")
    private String publicationId;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;

    public PublicationVoteDB() {
    }

    public PublicationVoteDB(String publicationId, String user_id) {
        this.user_id = user_id;
        this.publicationId = publicationId;
    }
}
