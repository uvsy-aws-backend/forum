package app.uvsy.model.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;


@Data
@DatabaseTable(tableName = "publication")
public class PublicationDB {

    public static final String PROGRAM_ID_FIELD = "program_id";

    @DatabaseField(columnName = "id", id = true)
    private String id;

    @DatabaseField(columnName = "title")
    private String title;

    @DatabaseField(columnName = "description")
    private String description;

    @DatabaseField(columnName = "user_id")
    private String userId;

    @DatabaseField(columnName = "program_id")
    private String programId;

    @DatabaseField(columnName = "votes")
    private Integer votes;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;
}
