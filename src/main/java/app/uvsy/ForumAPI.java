package app.uvsy;

import app.uvsy.controllers.publication.PublicationController;
import app.uvsy.database.DBConnection;
import app.uvsy.model.Publication;
import app.uvsy.model.PublicationTag;
import app.uvsy.model.Tag;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import org.github.serverless.api.ServerlessApiHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class ForumAPI extends ServerlessApiHandler {

    @Override
    protected void registerControllers(List<Object> controllers) {
        controllers.add(new PublicationController());
    }


    public static void main(String[] args) {

        //getPostsByTag();
    }

    private static void getPostsByTag() {
        List<String> tags = Arrays.asList("MY_TAG", "TAG_2");

        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Publication, String> publicationsDao = DaoManager.createDao(conn, Publication.class);
            Dao<Tag, String> tagDao = DaoManager.createDao(conn, Tag.class);
            Dao<PublicationTag, String> pTagDao = DaoManager.createDao(conn, PublicationTag.class);


            QueryBuilder<Tag, String> tagQueryBuilder = tagDao.queryBuilder();

            tagQueryBuilder.where().in("description", tags).prepare();

            QueryBuilder<PublicationTag, String> publicationTagQueryBuilder = pTagDao.queryBuilder();
            publicationTagQueryBuilder.join(tagQueryBuilder);

            QueryBuilder<Publication, String> builder = publicationsDao.queryBuilder();
            builder.where().eq("program_id", "blabla");
            builder.join(publicationTagQueryBuilder).distinct();

            List<Publication> query = builder.query();
            query.forEach(System.out::println);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

}
