package app.uvsy;

import app.uvsy.controllers.publication.PublicationController;
import app.uvsy.database.DBConnection;
import app.uvsy.model.db.PublicationDB;
import app.uvsy.model.db.PublicationTagDB;
import app.uvsy.model.db.TagDB;
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
}
