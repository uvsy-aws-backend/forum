package app.uvsy.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.sql.Timestamp;


@Data
@DatabaseTable(tableName = "tag")
public class Tag {

    public static final String DESCRIPTION_FIELD = "description";

    @DatabaseField(columnName = "id", id = true)
    private String id;

    @DatabaseField(columnName = "description")
    private String description;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;


    public Tag() {
    }

    public Tag(String id, String description) {
        this.id = id;
        this.description = description;
    }


}
