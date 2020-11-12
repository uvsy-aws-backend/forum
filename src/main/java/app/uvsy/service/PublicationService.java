package app.uvsy.service;

import app.uvsy.aggregates.Sorting;
import app.uvsy.apis.exceptions.APIClientException;
import app.uvsy.apis.students.StudentsAPI;
import app.uvsy.apis.students.model.UserAlias;
import app.uvsy.database.DBConnection;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.environment.Environment;
import app.uvsy.model.Publication;
import app.uvsy.model.db.CommentDB;
import app.uvsy.model.db.CommentVoteDB;
import app.uvsy.model.db.PublicationDB;
import app.uvsy.model.db.PublicationTagDB;
import app.uvsy.model.db.PublicationVoteDB;
import app.uvsy.model.db.TagDB;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.misc.TransactionManager;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PublicationService {
    private static final Map<String, String> SORT_FIELD_MAPPING = new HashMap<String, String>() {{
        put("votes", PublicationDB.VOTES_FIELD);
        put("creation", PublicationDB.CREATED_AT_FIELD);
    }};

    private final Sorting sorting;

    public PublicationService() {
        sorting = new Sorting(SORT_FIELD_MAPPING);
    }


    public List<Publication> getPublications(String programId, Integer limit, Integer offset,
                                             List<String> tags, String tagOperator, List<String> sortBy,
                                             Boolean includeTags,
                                             Boolean includeAlias,
                                             String userId) {

        List<Publication> publications;
        try (ConnectionSource conn = DBConnection.create()) {
            QueryBuilder<PublicationDB, String> publicationsQuery = createPublicationsQuery(conn, programId,
                    limit, offset, sortBy);

            if (!tags.isEmpty()) {
                addFilterOnTags(conn, publicationsQuery, tags, tagOperator);
            }

            publications = publicationsQuery
                    .query()
                    .stream()
                    .map(Publication::from)
                    .collect(Collectors.toList());

            if (!publications.isEmpty() && includeTags) {
                Map<String, List<String>> publicationTags = getTags(conn, publications);
                publications.forEach(
                        p -> p.setTags(publicationTags.getOrDefault(p.getId(), new ArrayList<>()))
                );
            }

            if (!publications.isEmpty()) {
                Map<String, Long> commentCount = getCommentCount(conn, publications);
                publications.forEach(
                        p -> p.setComments(commentCount.getOrDefault(p.getId(), 0L))
                );
            }

            if (!userId.isEmpty()) {
                Map<String, String> publicationVotes = getVotesForPublications(conn, publications, userId);
                publications.forEach(
                        p -> p.setUserVoteId(publicationVotes.getOrDefault(p.getId(), null))
                );
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }

        if (!publications.isEmpty() && includeAlias) {
            Map<String, String> publicationsAlias = getAlias(publications);
            publications = publications
                    .stream()
                    .peek(p -> p.setUserAlias(publicationsAlias.get(p.getUserId())))
                    .filter(p -> Objects.nonNull(p.getUserAlias()))
                    .collect(Collectors.toList());
        }
        return publications;
    }

    public Publication getPublication(String publicationId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);
            return Optional.ofNullable(publicationsDao.queryForId(publicationId))
                    .map(Publication::from)
                    .orElseThrow(() -> new RecordNotFoundException(publicationId));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }


    public void updatePublication(String publicationId, String title, String description, List<String> tags) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);
            PublicationDB publicationDB = Optional.ofNullable(publicationsDao.queryForId(publicationId))
                    .orElseThrow(() -> new RecordNotFoundException(publicationId));

            publicationDB.setTitle(title);
            publicationDB.setDescription(description);

            TransactionManager.callInTransaction(conn, () -> {
                publicationsDao.update(publicationDB);
                insertTags(conn, publicationDB, tags);
                return null;
            });

        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void createPublication(String title, String description, String programId, String userId, List<String> tags) {

        try (ConnectionSource conn = DBConnection.create()) {

            // Store publication
            Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);

            PublicationDB publication = new PublicationDB();
            publication.setId(UUID.randomUUID().toString());
            publication.setTitle(title);
            publication.setDescription(description);
            publication.setProgramId(programId);
            publication.setUserId(userId);
            publication.setVotes(0);
            publicationsDao.create(publication);

            if (!tags.isEmpty()) {
                insertTags(conn, publication, tags);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void deletePublication(String publicationId) {
        try (ConnectionSource conn = DBConnection.create()) {

            // DAOs
            Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);
            Dao<PublicationTagDB, String> publicationTagDAO = DaoManager.createDao(conn, PublicationTagDB.class);
            Dao<PublicationVoteDB, String> publicationVoteDAO = DaoManager.createDao(conn, PublicationVoteDB.class);
            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);
            Dao<CommentVoteDB, String> commentVoteDao = DaoManager.createDao(conn, CommentVoteDB.class);

            // Query Builders
            DeleteBuilder<PublicationTagDB, String> publicationTagDelete = publicationTagDAO.deleteBuilder();
            publicationTagDelete.where().eq(PublicationTagDB.PUBLICATION_ID_FIELD, publicationId);

            DeleteBuilder<PublicationVoteDB, String> publicationVoteDelete = publicationVoteDAO.deleteBuilder();
            publicationVoteDelete.where().eq(PublicationVoteDB.PUBLICATION_ID_FIELD, publicationId);

            QueryBuilder<CommentDB, String> commentQuery = commentsDao.queryBuilder();
            List<CommentDB> comments = commentQuery
                    .where()
                    .eq(CommentDB.PUBLICATION_ID_FIELD, publicationId)
                    .query();


            DeleteBuilder<CommentVoteDB, String> commentVoteDelete = commentVoteDao.deleteBuilder();

            if (!comments.isEmpty()) {
                commentVoteDelete.where().in(
                        CommentVoteDB.COMMENT_ID_FIELD,
                        comments.stream().map(CommentDB::getId).collect(Collectors.toList())
                );
            }

            // Transaction
            TransactionManager.callInTransaction(conn, () -> {
                if (!comments.isEmpty()) commentVoteDelete.delete();
                commentsDao.delete(comments);
                publicationTagDelete.delete();
                publicationsDao.deleteById(publicationId);
                return null; // Required by the interface
            });


        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    // Queries
    private QueryBuilder<PublicationDB, String> createPublicationsQuery(ConnectionSource conn, String programId, Integer limit,
                                                                        Integer offset, List<String> sortBy) throws SQLException {
        Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);

        QueryBuilder<PublicationDB, String> publicationsQuery = publicationsDao.queryBuilder();

        if (!programId.isEmpty()) {
            publicationsQuery.where().eq(PublicationDB.PROGRAM_ID_FIELD, programId);
        }

        // Pagination
        if (limit > 0) publicationsQuery.limit(limit.longValue());
        if (offset > 0) publicationsQuery.offset(offset.longValue());

        // Sorting
        sorting.parse(sortBy).forEach(publicationsQuery::orderBy);

        return publicationsQuery;
    }

    private void addFilterOnTags(ConnectionSource conn, QueryBuilder<PublicationDB, String> publicationsQuery,
                                 List<String> tags, String tagOperator) throws SQLException {
        QueryBuilder<PublicationTagDB, String> publicationTagQuery = getTagQuery(conn, tags);
        publicationsQuery.join(publicationTagQuery);

        if (tagOperator.equals("AND")) {
            publicationsQuery.groupBy("id")
                    .having("COUNT(publication.id) = " + tags.size());
        } else {
            publicationsQuery.distinct();
        }
    }

    private Map<String, List<String>> getTags(ConnectionSource conn, List<Publication> publications) throws SQLException {
        return DaoManager.createDao(conn, PublicationTagDB.class)
                .queryBuilder()
                .where()
                .in(
                        PublicationTagDB.PUBLICATION_ID_FIELD,
                        publications.stream()
                                .map(Publication::getId)
                                .collect(Collectors.toList())
                )
                .query()
                .stream()
                .collect(
                        Collectors.groupingBy(PublicationTagDB::getPublicationId)
                )
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()
                                .stream()
                                .map(PublicationTagDB::getTag)
                                .map(TagDB::getDescription)
                                .collect(Collectors.toList())
                ));
    }

    private void insertTags(ConnectionSource conn, PublicationDB publication, List<String> tags) throws SQLException {
        // Create tags
        Set<TagDB> tagsSet = createTags(conn, tags);

        Dao<PublicationTagDB, String> publicationTagsDao = DaoManager.createDao(conn, PublicationTagDB.class);

        // Delete Publication Tags
        DeleteBuilder<PublicationTagDB, String> deleteBuilder = publicationTagsDao.deleteBuilder();
        deleteBuilder.where().eq(PublicationTagDB.PUBLICATION_ID_FIELD, publication.getId());
        deleteBuilder.delete();

        // Create Publication Tags
        publicationTagsDao.create(
                tagsSet.stream()
                        .map(t -> new PublicationTagDB(t.getId(), publication.getId()))
                        .collect(Collectors.toList())
        );
    }

    private Set<TagDB> createTags(ConnectionSource conn, List<String> tags) throws SQLException {
        Dao<TagDB, String> tagsDao = DaoManager.createDao(conn, TagDB.class);
        List<TagDB> existingTagDBS = tagsDao.queryBuilder()
                .where()
                .in(TagDB.DESCRIPTION_FIELD, tags)
                .query();

        // Create non existing tags
        HashSet<String> tagSet = new HashSet<>(tags);
        tagSet.removeAll(existingTagDBS
                .stream()
                .map(TagDB::getDescription)
                .collect(Collectors.toList()));

        List<TagDB> newTagDBS = tagSet.stream()
                .map((d) -> new TagDB(UUID.randomUUID().toString(), d))
                .collect(Collectors.toList());

        tagsDao.create(newTagDBS);

        Set<TagDB> totalTags = new HashSet<>(existingTagDBS);
        totalTags.addAll(newTagDBS);
        return totalTags;
    }

    private QueryBuilder<PublicationTagDB, String> getTagQuery(ConnectionSource conn, List<String> tags) throws SQLException {
        // Prepare query for tags table
        // This steps gets the id of the tags in the query
        Dao<TagDB, String> tagDao = DaoManager.createDao(conn, TagDB.class);
        QueryBuilder<TagDB, String> tagsQuery = tagDao.queryBuilder();
        tagsQuery.where().in(TagDB.DESCRIPTION_FIELD, tags).prepare();

        // Prepare query for publication<->tags table
        // This steps gets the id of the publications
        Dao<PublicationTagDB, String> pTagDao = DaoManager.createDao(conn, PublicationTagDB.class);
        QueryBuilder<PublicationTagDB, String> publicationTagQuery = pTagDao.queryBuilder();
        publicationTagQuery.join(tagsQuery);
        return publicationTagQuery;
    }

    private Map<String, Long> getCommentCount(ConnectionSource conn, List<Publication> publications) throws SQLException {
        String ids = publications.stream()
                .map(Publication::getId)
                .distinct()
                .map(s -> String.format("'%s'", s))
                .collect(Collectors.joining(","));


        String query = "select publication_id,count(*) " +
                "from comment " +
                "where publication_id in (" + ids + ") " +
                "group by publication_id";
        DataType[] dataTypes = {DataType.STRING, DataType.LONG};

        return DaoManager.createDao(conn, CommentDB.class).queryRaw(query, dataTypes)
                .getResults()
                .stream()
                .collect(
                        Collectors.toMap(
                                row -> (String) row[0],
                                row -> (Long) row[1]
                        )
                );
    }

    private Map<String, String> getVotesForPublications(ConnectionSource conn, List<Publication> publications, String userId) throws SQLException {
        return DaoManager.createDao(conn, PublicationVoteDB.class)
                .queryBuilder()
                .where()
                .in(PublicationVoteDB.PUBLICATION_ID_FIELD, publications
                        .stream()
                        .map(Publication::getId)
                        .collect(Collectors.toList()))
                .and()
                .eq(PublicationVoteDB.USER_ID_FIELD, userId)
                .query()
                .stream()
                .collect(Collectors.toMap(
                        PublicationVoteDB::getPublicationId,
                        PublicationVoteDB::getId,
                        (x, y) -> x
                ));
    }

    // Alias
    private Map<String, String> getAlias(List<Publication> publications) throws APIClientException {
        StudentsAPI studentsAPI = new StudentsAPI(Environment.getStage());
        return studentsAPI
                .postAliasQuery(publications.stream()
                        .map(Publication::getUserId)
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
