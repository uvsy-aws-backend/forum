package app.uvsy;

import app.uvsy.controllers.comment.CommentController;
import app.uvsy.controllers.publication.PublicationController;
import app.uvsy.controllers.report.ReportController;
import app.uvsy.controllers.votes.CommentVoteController;
import app.uvsy.controllers.votes.PublicationVoteController;
import org.github.serverless.api.ServerlessApiHandler;

import java.util.List;

public class ForumAPI extends ServerlessApiHandler {

    @Override
    protected void registerControllers(List<Object> controllers) {
        controllers.add(new PublicationController());
        controllers.add(new CommentController());
        controllers.add(new PublicationVoteController());
        controllers.add(new CommentVoteController());
        controllers.add(new ReportController());
    }
}
