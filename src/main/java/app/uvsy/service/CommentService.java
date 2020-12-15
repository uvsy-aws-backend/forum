package app.uvsy.service;

import app.uvsy.apis.exceptions.APIClientException;
import app.uvsy.apis.students.StudentsAPI;
import app.uvsy.apis.students.model.UserAlias;
import app.uvsy.database.DBConnection;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.environment.Environment;
import app.uvsy.model.Comment;
import app.uvsy.model.Publication;
import app.uvsy.model.db.CommentDB;
import app.uvsy.model.db.CommentVoteDB;
import app.uvsy.model.db.PublicationDB;
import app.uvsy.model.db.PublicationVoteDB;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommentService {


    public List<Comment> getComments(String publicationId, Integer limit, Integer offset,
                                     List<String> sortBy, Boolean includeAlias, String userId) {
        try (ConnectionSource conn = DBConnection.create()) {

            if (publicationExists(conn, publicationId)) {
                QueryBuilder<CommentDB, String> commentQuery = getCommentQuery(conn, publicationId,
                        limit, offset, sortBy);

                List<Comment> comments = commentQuery.query()
                        .stream()
                        .map(Comment::from)
                        .collect(Collectors.toList());

                if (!comments.isEmpty() && includeAlias) {
                    Map<String, String> publicationsAlias = getAlias(comments);
                    comments = comments
                            .stream()
                            .peek(p -> p.setUserAlias(publicationsAlias.get(p.getUserId())))
                            .filter(p -> Objects.nonNull(p.getUserAlias()))
                            .collect(Collectors.toList());
                }

                if (!userId.isEmpty()) {
                    Map<String, String> commentVotes = getVotesForComments(conn, comments, userId);
                    comments.forEach(
                            c -> c.setUserVoteId(commentVotes.getOrDefault(c.getId(), null))
                    );
                }

                return comments;
            } else {
                throw new RecordNotFoundException(publicationId);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void createComment(String publicationId, String userId, String content) {

        try (ConnectionSource conn = DBConnection.create()) {

            if (publicationExists(conn, publicationId)) {
                Dao<CommentDB, String> commentDao = DaoManager.createDao(conn, CommentDB.class);

                CommentDB commentDB = new CommentDB();
                commentDB.setPublicationId(publicationId);
                commentDB.setUserId(userId);
                commentDB.setContent(content);
                commentDB.setVotes(0);
                commentDB.setReported(Boolean.FALSE);
                commentDao.create(commentDB);
            } else {
                throw new RecordNotFoundException(publicationId);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public Comment getComment(String commentId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);
            return Optional.ofNullable(commentsDao.queryForId(commentId))
                    .map(Comment::from)
                    .orElseThrow(() -> new RecordNotFoundException(commentId));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void updateComment(String commentId, String content) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);
            CommentDB comment = Optional.ofNullable(commentsDao.queryForId(commentId))
                    .orElseThrow(() -> new RecordNotFoundException(commentId));

            comment.setContent(content);
            commentsDao.update(comment);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }


    }

    public void deleteComment(String commentId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);
            Dao<CommentVoteDB, String> commentsVoteDao = DaoManager.createDao(conn, CommentVoteDB.class);

            DeleteBuilder<CommentVoteDB, String> commentVoteDelete = commentsVoteDao.deleteBuilder();
            commentVoteDelete.where().eq(CommentVoteDB.COMMENT_ID_FIELD, commentId);

            TransactionManager.callInTransaction(conn, () -> {
                        commentVoteDelete.delete();
                        commentsDao.deleteById(commentId);
                        return null;
                    }
            );
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }


    // Query
    private boolean publicationExists(ConnectionSource conn, String publicationId) throws SQLException {
        Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);
        return Optional.ofNullable(publicationsDao.queryForId(publicationId)).isPresent();
    }


    private QueryBuilder<CommentDB, String> getCommentQuery(ConnectionSource conn, String publicationId, Integer limit, Integer offset, List<String> sortBy) throws SQLException {
        Dao<CommentDB, String> commentDao = DaoManager.createDao(conn, CommentDB.class);

        QueryBuilder<CommentDB, String> commentQuery = commentDao.queryBuilder();

        commentQuery.where().eq(CommentDB.PUBLICATION_ID_FIELD, publicationId);

        // Pagination
        if (limit > 0) commentQuery.limit(limit.longValue());
        if (offset > 0) commentQuery.offset(offset.longValue());

        // Sorting
        sortBy.forEach((s -> commentQuery.orderBy(s, true)));
        return commentQuery;
    }

    private Map<String, String> getVotesForComments(ConnectionSource conn, List<Comment> comments, String userId) throws SQLException {
        if (comments.isEmpty()) return Collections.emptyMap();
        return DaoManager.createDao(conn, CommentVoteDB.class)
                .queryBuilder()
                .where()
                .in(CommentVoteDB.COMMENT_ID_FIELD, comments
                        .stream()
                        .map(Comment::getId)
                        .collect(Collectors.toList()))
                .and()
                .eq(CommentVoteDB.USER_ID_FIELD, userId)
                .query()
                .stream()
                .collect(Collectors.toMap(
                        CommentVoteDB::getCommentId,
                        CommentVoteDB::getId,
                        (x, y) -> x
                ));
    }


    // Alias
    private Map<String, String> getAlias(List<Comment> comments) throws APIClientException {
        StudentsAPI studentsAPI = new StudentsAPI(Environment.getStage());
        return studentsAPI
                .postAliasQuery(comments.stream()
                        .map(Comment::getUserId)
                        .distinct()
                        .collect(Collectors.toList()))
                .stream()
                .collect(
                        Collectors.toMap(
                                UserAlias::getUserId,
                                UserAlias::getAlias
                        )
                );
    }


}
