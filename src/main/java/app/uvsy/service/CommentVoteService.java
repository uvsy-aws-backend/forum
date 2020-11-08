package app.uvsy.service;

import app.uvsy.database.DBConnection;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.model.db.CommentDB;
import app.uvsy.model.db.CommentVoteDB;
import app.uvsy.service.exceptions.RecordConflictException;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class CommentVoteService {

    public List<CommentVoteDB> getCommentVotesForUser(String publicationId, String userId) {
        try (ConnectionSource conn = DBConnection.create()) {

            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);
            QueryBuilder<CommentDB, String> commentQuery = commentsDao.queryBuilder();
            List<String> comments = commentQuery
                    .where()
                    .eq(CommentDB.PUBLICATION_ID_FIELD, publicationId)
                    .query()
                    .stream()
                    .map(CommentDB::getId)
                    .collect(Collectors.toList());

            if (comments.isEmpty()){
                return Collections.emptyList();
            }

            return DaoManager.createDao(conn, CommentVoteDB.class)
                    .queryBuilder()
                    .where()
                    .in(CommentVoteDB.COMMENT_ID_FIELD, comments)
                    .and()
                    .eq(CommentVoteDB.USER_ID_FIELD, userId)
                    .query();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void createCommentVote(String commentId, String userId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<CommentDB, String> commentDao = DaoManager.createDao(conn, CommentDB.class);

            CommentDB comment = Optional.ofNullable(commentDao.queryForId(commentId))
                    .orElseThrow(() -> new RecordNotFoundException(commentId));

            Dao<CommentVoteDB, String> voteDao = DaoManager.createDao(conn, CommentVoteDB.class);
            long commentVotes = voteDao.queryBuilder()
                    .where()
                    .eq(CommentVoteDB.COMMENT_ID_FIELD, commentId)
                    .and()
                    .eq(CommentVoteDB.USER_ID_FIELD, userId)
                    .countOf();

            if (commentVotes == 0) {
                comment.upvote();
                CommentVoteDB vote = new CommentVoteDB(commentId, userId);
                TransactionManager.callInTransaction(conn, () -> {
                            voteDao.create(vote);
                            commentDao.update(comment);
                            return null;
                        }
                );
            } else {
                throw new RecordConflictException("Already voted");
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void deleteVote(String voteId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<CommentDB, String> commentDao = DaoManager.createDao(conn, CommentDB.class);

            Dao<CommentVoteDB, String> voteDao = DaoManager.createDao(conn, CommentVoteDB.class);
            Optional<CommentVoteDB> commentVoteDB = Optional.ofNullable(voteDao.queryForId(voteId));

            if (commentVoteDB.isPresent()) {
                CommentVoteDB vote = commentVoteDB.get();

                Optional<CommentDB> commentDB = Optional.ofNullable(commentDao.queryForId(vote.getCommentId()));

                Callable<Void> transaction;
                if (commentDB.isPresent()) {
                    CommentDB comment = commentDB.get();
                    comment.downvote();
                    transaction = () -> {
                        voteDao.delete(vote);
                        commentDao.update(comment);
                        return null;
                    };
                } else {
                    transaction = () -> {
                        voteDao.delete(vote);
                        return null;
                    };
                }
                TransactionManager.callInTransaction(conn, transaction);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }
}
