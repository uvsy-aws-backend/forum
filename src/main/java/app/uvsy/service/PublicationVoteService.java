package app.uvsy.service;

import app.uvsy.database.DBConnection;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.model.db.PublicationDB;
import app.uvsy.model.db.PublicationVoteDB;
import app.uvsy.service.exceptions.RecordConflictException;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.support.ConnectionSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class PublicationVoteService {

    public List<PublicationVoteDB> getPublicationVotes(String publicationId) {
        try (ConnectionSource conn = DBConnection.create()) {
            return DaoManager.createDao(conn, PublicationVoteDB.class)
                    .queryBuilder()
                    .where()
                    .eq(PublicationVoteDB.PUBLICATION_ID_FIELD, publicationId)
                    .query();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public List<PublicationVoteDB> getPublicationVotesForUser(String publicationId, String userId) {
        try (ConnectionSource conn = DBConnection.create()) {
            return DaoManager.createDao(conn, PublicationVoteDB.class)
                    .queryBuilder()
                    .where()
                    .eq(PublicationVoteDB.PUBLICATION_ID_FIELD, publicationId)
                    .and()
                    .eq(PublicationVoteDB.USER_ID_FIELD, userId)
                    .query();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void createPublicationVote(String publicationId, String userId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<PublicationDB, String> publicationDao = DaoManager.createDao(conn, PublicationDB.class);

            PublicationDB publication = Optional.ofNullable(publicationDao.queryForId(publicationId))
                    .orElseThrow(() -> new RecordNotFoundException(publicationId));

            Dao<PublicationVoteDB, String> voteDao = DaoManager.createDao(conn, PublicationVoteDB.class);
            long publicationVotes = voteDao.queryBuilder()
                    .where()
                    .eq(PublicationVoteDB.PUBLICATION_ID_FIELD, publicationId)
                    .and()
                    .eq(PublicationVoteDB.USER_ID_FIELD, userId)
                    .countOf();

            if (publicationVotes == 0) {
                publication.upvote();
                PublicationVoteDB vote = new PublicationVoteDB(publicationId, userId);
                TransactionManager.callInTransaction(conn, () -> {
                            voteDao.create(vote);
                            publicationDao.update(publication);
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
            Dao<PublicationDB, String> publicationDao = DaoManager.createDao(conn, PublicationDB.class);

            Dao<PublicationVoteDB, String> voteDao = DaoManager.createDao(conn, PublicationVoteDB.class);
            Optional<PublicationVoteDB> publicationVoteDB = Optional.ofNullable(voteDao.queryForId(voteId));

            if (publicationVoteDB.isPresent()) {
                PublicationVoteDB vote = publicationVoteDB.get();

                Optional<PublicationDB> publicationDB = Optional.ofNullable(publicationDao.queryForId(vote.getPublicationId()));

                Callable<Void> transaction;
                if (publicationDB.isPresent()) {
                    PublicationDB publication = publicationDB.get();
                    publication.downvote();
                    transaction = () -> {
                        voteDao.delete(vote);
                        publicationDao.update(publication);
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
