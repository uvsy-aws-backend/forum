package app.uvsy.model.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.sql.Timestamp;


@Data
@DatabaseTable(tableName = "publicationtag")
public class PublicationTagDB {

    public static final String PUBLICATION_ID_FIELD = "publication_id";


    @DatabaseField(columnName = "id", id = true, readOnly = true)
    private String id;

    @DatabaseField(columnName = "tag_id")
    private String tagId;

    @DatabaseField(columnName = "publication_id")
    private String publicationId;

    @DatabaseField(foreign = true, foreignColumnName = "id", columnName = "publication_id", readOnly = true)
    private PublicationDB publication;

    @DatabaseField(foreign = true, foreignColumnName = "id", columnName = "tag_id", readOnly = true)
    private TagDB tag;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;


    public PublicationTagDB() {
    }

    public PublicationTagDB(String tagId, String publicationId) {
        this.tagId = tagId;
        this.publicationId = publicationId;
    }
}
