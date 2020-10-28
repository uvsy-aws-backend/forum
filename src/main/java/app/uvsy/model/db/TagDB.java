package app.uvsy.model.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Objects;


@Data
@DatabaseTable(tableName = "tag")
public class TagDB {

    public static final String DESCRIPTION_FIELD = "description";

    @DatabaseField(columnName = "id", id = true)
    private String id;

    @DatabaseField(columnName = "description")
    private String description;

    @DatabaseField(columnName = "created_at", readOnly = true)
    private Timestamp createdAt;

    @DatabaseField(columnName = "updated_at", readOnly = true)
    private Timestamp updatedAt;


    public TagDB() {
    }

    public TagDB(String id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagDB tagDB = (TagDB) o;
        return Objects.equals(id, tagDB.id) &&
                Objects.equals(description, tagDB.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description);
    }
}
