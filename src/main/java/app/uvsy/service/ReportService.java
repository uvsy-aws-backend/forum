package app.uvsy.service;

import app.uvsy.database.DBConnection;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.model.db.CommentDB;
import app.uvsy.model.db.PublicationDB;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import javax.mail.MessagingException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class ReportService {

    private final EmailService emailService;

    public ReportService() {
        this(new EmailService());
    }

    public ReportService(EmailService emailService) {
        this.emailService = emailService;
    }


    public void reportPublication(String publicationId, String userId) {
        updatePublicationReportedState(publicationId);
        sendPublicationReportedEmail(publicationId, userId);
    }


    public void reportComment(String commentId, String userId) {
        updateCommentReportedState(commentId);
        sendCommentReportedEmail(commentId, userId);
    }

    private void updatePublicationReportedState(String publicationId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);

            PublicationDB publicationDB = Optional.ofNullable(publicationsDao.queryForId(publicationId))
                    .orElseThrow(() -> new RecordNotFoundException(publicationId));

            if (!publicationDB.isReported()) {
                publicationDB.setReported(Boolean.TRUE);
                publicationsDao.update(publicationDB);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    private void updateCommentReportedState(String commentId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);

            CommentDB commentDB = Optional.ofNullable(commentsDao.queryForId(commentId))
                    .orElseThrow(() -> new RecordNotFoundException(commentId));

            if (!commentDB.isReported()) {
                commentDB.setReported(Boolean.TRUE);
                commentsDao.update(commentDB);
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    private void sendPublicationReportedEmail(String publicationId, String userId) {
        try {
            // Why spanish? It was decided for demo purposes. It should be english, as everything in
            // software is. Its painful to write, painful to read, and it conflicts with the native language
            // on which java is written. Suffer with me in the following lines.
            String subject = String.format("Publicación reportada %s", System.currentTimeMillis());
            String content = String.format(
                    "La publicación %s fue reportada por el usuario %s",
                    publicationId, userId
            );
            emailService.sendEmail(subject, content);
        } catch (MessagingException e) {
            // Why not throw the exception or at least wrap it?
            // Well, its a priority that the DB is updated so the publication is not shown.
            // If this part of the logic fails, it will leave an stacktrace that some day
            // an observability system will capture.
            e.printStackTrace();
        }
    }

    private void sendCommentReportedEmail(String commentId, String userId) {
        try {
            // Why spanish? It was decided for demo purposes. It should be english, as everything in
            // software is. Its painful to write, painful to read, and it conflicts with the native language
            // on which java is written. Suffer with me in the following lines.
            String subject = String.format("Comentario reportado %s", System.currentTimeMillis());
            String content = String.format(
                    "El comentario %s fue reportado por el usuario %s",
                    commentId, userId
            );
            emailService.sendEmail(subject, content);
        } catch (MessagingException e) {
            // Why not throw the exception or at least wrap it?
            // Well, its a priority that the DB is updated so the publication is not shown.
            // If this part of the logic fails, it will leave an stacktrace that some day
            // an observability system will capture.
            e.printStackTrace();
        }
    }
}
