package app.uvsy;

import app.uvsy.controllers.publication.PublicationController;
import org.github.serverless.api.ServerlessApiHandler;

import java.util.List;

public class ForumAPI extends ServerlessApiHandler {

    @Override
    protected void registerControllers(List<Object> controllers) {
        controllers.add(new PublicationController());
    }
}
