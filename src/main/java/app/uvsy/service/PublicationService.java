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
import app.uvsy.model.db.PublicationDB;
import app.uvsy.model.db.PublicationTagDB;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PublicationService {


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

    public List<Publication> getPublications(String programId, Integer limit, Integer offset,
                                             List<String> tags, String tagOperator, List<String> sortBy,
                                             Boolean includeTags, Boolean includeAlias) {

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

            if (!publications.isEmpty()){
                Map<String, Long> commentCount = getCommentCount(conn, publications);
                publications.forEach(
                        p -> p.setComments(commentCount.getOrDefault(p.getId(), 0L))
                );
            }


        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }

        if (!publications.isEmpty() && includeAlias) {
            Map<String, String> publicationsAlias = getAliasFromPublications(publications);
            publications = publications
                    .stream()
                    .peek(p -> p.setUserAlias(publicationsAlias.get(p.getUserId())))
                    .filter(p -> Objects.nonNull(p.getUserAlias()))
                    .collect(Collectors.toList());
        }
        return publications;
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
                insertTags(tags, conn, publication);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public void deletePublication(String publicationId) {
        try (ConnectionSource conn = DBConnection.create()) {

            Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);
            Dao<PublicationTagDB, String> publicationTagDAO = DaoManager.createDao(conn, PublicationTagDB.class);
            Dao<CommentDB, String> commentsDao = DaoManager.createDao(conn, CommentDB.class);


            DeleteBuilder<CommentDB, String> commentDelete = commentsDao.deleteBuilder();
            commentDelete.where().eq(CommentDB.PUBLICATION_ID_FIELD, publicationId);


            DeleteBuilder<PublicationTagDB, String> publicationTagDelete = publicationTagDAO.deleteBuilder();
            publicationTagDelete.where().eq(PublicationTagDB.PUBLICATION_ID_FIELD, publicationId);

            TransactionManager.callInTransaction(conn, () -> {
                commentDelete.delete();
                publicationTagDelete.delete();
                publicationsDao.deleteById(publicationId);
                return null; // Required by the interface
            });


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
                commentDao.create(commentDB);
            } else {
                throw new RecordNotFoundException(publicationId);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public List<Comment> getComments(String publicationId, Integer limit, Integer offset,
                                     List<String> sortBy, Boolean includeAlias) {
        try (ConnectionSource conn = DBConnection.create()) {

            if (publicationExists(conn, publicationId)) {
                QueryBuilder<CommentDB, String> commentQuery = getCommentQuery(conn, publicationId,
                        limit, offset, sortBy);

                List<Comment> comments = commentQuery.query()
                        .stream()
                        .map(Comment::from)
                        .collect(Collectors.toList());

                if (!comments.isEmpty() && includeAlias) {
                    Map<String, String> publicationsAlias = getAliasFromComments(comments);
                    comments = comments
                            .stream()
                            .peek(p -> p.setUserAlias(publicationsAlias.get(p.getUserId())))
                            .filter(p -> Objects.nonNull(p.getUserAlias()))
                            .collect(Collectors.toList());
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
        sortBy.forEach((s -> publicationsQuery.orderBy(s, true)));

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

    private void insertTags(List<String> tags, ConnectionSource conn, PublicationDB publication) throws SQLException {
        // Get Existing tag
        Dao<TagDB, String> tagsDao = DaoManager.createDao(conn, TagDB.class);
        List<TagDB> existingTagDBS = tagsDao.queryBuilder()
                .where()
                .eq(TagDB.DESCRIPTION_FIELD, tags)
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


        // Associate publication with tags
        List<TagDB> totalTagDBS = new ArrayList<>();
        totalTagDBS.addAll(newTagDBS);
        totalTagDBS.addAll(existingTagDBS);

        Dao<PublicationTagDB, String> publicationTagsDao = DaoManager.createDao(conn, PublicationTagDB.class);
        publicationTagsDao.create(
                totalTagDBS.stream()
                        .map(t -> new PublicationTagDB(t.getId(), publication.getId()))
                        .collect(Collectors.toList())
        );
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

    private boolean publicationExists(ConnectionSource conn, String publicationId) throws SQLException {
        Dao<PublicationDB, String> publicationsDao = DaoManager.createDao(conn, PublicationDB.class);
        return Optional.ofNullable(publicationsDao.queryForId(publicationId)).isPresent();
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

    // Alias
    private Map<String, String> getAlias(List<String> userIds) throws APIClientException {
        StudentsAPI studentsAPI = new StudentsAPI(Environment.getStage());
        return studentsAPI
                .postAliasQuery(userIds)
                .stream()
                .collect(
                        Collectors.toMap(
                                UserAlias::getUserId,
                                UserAlias::getAlias
                        )
                );
    }

    private Map<String, String> getAliasFromPublications(List<Publication> publications) throws APIClientException {
        return getAlias(publications.stream()
                .map(Publication::getUserId)
                .distinct()
                .collect(Collectors.toList())
        );
    }

    private Map<String, String> getAliasFromComments(List<Comment> comments) throws APIClientException {
        return getAlias(comments.stream()
                .map(Comment::getUserId)
                .distinct()
                .collect(Collectors.toList())
        );
    }
}
