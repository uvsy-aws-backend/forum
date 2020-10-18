package app.uvsy.service;

import app.uvsy.database.DBConnection;
import app.uvsy.database.exceptions.DBException;
import app.uvsy.model.Publication;
import app.uvsy.model.PublicationTag;
import app.uvsy.model.Tag;
import app.uvsy.service.exceptions.RecordNotFoundException;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class PublicationService {


    public Publication getPublication(String publicationId) {
        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Publication, String> publicationsDao = DaoManager.createDao(conn, Publication.class);
            return Optional.ofNullable(publicationsDao.queryForId(publicationId))
                    .orElseThrow(() -> new RecordNotFoundException(publicationId));
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    public List<Publication> getPublications(String programId,
                                             Integer limit,
                                             Integer offset,
                                             List<String> tags,
                                             String tagOperator,
                                             List<String> sortBy, Boolean includeTags) {


        try (ConnectionSource conn = DBConnection.create()) {
            Dao<Publication, String> publicationsDao = DaoManager.createDao(conn, Publication.class);

            QueryBuilder<Publication, String> publicationsQuery = publicationsDao.queryBuilder();

            if (!programId.isEmpty()) {
                publicationsQuery.where().eq(Publication.PROGRAM_ID_FIELD, programId);
            }

            if (!tags.isEmpty()) {
                QueryBuilder<PublicationTag, String> publicationTagQuery = getTagQuery(conn, tags);
                publicationsQuery.join(publicationTagQuery);

                if (tagOperator.equals("AND")) {
                    publicationsQuery.groupBy("id")
                            .having("COUNT(publication.id) = " + tags.size());
                } else {
                    publicationsQuery.distinct();
                }
            }

            // Limit
            if (limit > 0) {
                publicationsQuery.limit(limit.longValue());
            }

            // After
            if (offset > 0) {
                publicationsQuery.offset(offset.longValue());
            }

            // Sorting
            sortBy.forEach((s -> publicationsQuery.orderBy(s, true)));

            List<Publication> publications = publicationsQuery.query();

            if (!publications.isEmpty() && includeTags) {
                Map<String, List<String>> publicationTags = getTags(conn, publications);
                publications.forEach(
                        p -> p.setTags(publicationTags.getOrDefault(p.getId(), new ArrayList<>()))
                );
            }
            return publications;
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    private Map<String, List<String>> getTags(ConnectionSource conn, List<Publication> publications) throws SQLException {
        return DaoManager.createDao(conn, PublicationTag.class)
                .queryBuilder()
                .where()
                .in(
                        PublicationTag.PUBLICATION_ID_FIELD,
                        publications.stream()
                                .map(Publication::getId)
                                .collect(Collectors.toList())
                )
                .query()
                .stream()
                .collect(
                        Collectors.groupingBy(PublicationTag::getPublicationId)
                )
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue()
                                .stream()
                                .map(PublicationTag::getTag)
                                .map(Tag::getDescription)
                                .collect(Collectors.toList())
                ));
    }

    private QueryBuilder<PublicationTag, String> getTagQuery(ConnectionSource conn, List<String> tags) throws SQLException {
        // Prepare query for tags table
        // This steps gets the id of the tags in the query
        Dao<Tag, String> tagDao = DaoManager.createDao(conn, Tag.class);
        QueryBuilder<Tag, String> tagsQuery = tagDao.queryBuilder();
        tagsQuery.where().in(Tag.DESCRIPTION_FIELD, tags).prepare();

        // Prepare query for publication<->tags table
        // This steps gets the id of the publications
        Dao<PublicationTag, String> pTagDao = DaoManager.createDao(conn, PublicationTag.class);
        QueryBuilder<PublicationTag, String> publicationTagQuery = pTagDao.queryBuilder();
        publicationTagQuery.join(tagsQuery);
        return publicationTagQuery;
    }

    public void createPublication(String title, String description, String programId, String userId, List<String> tags) {

        try (ConnectionSource conn = DBConnection.create()) {


            // Store publication
            Dao<Publication, String> publicationsDao = DaoManager.createDao(conn, Publication.class);

            Publication publication = new Publication();
            publication.setId(UUID.randomUUID().toString());
            publication.setTitle(title);
            publication.setDescription(description);
            publication.setProgramId(programId);
            publication.setUserId(userId);
            publication.setVotes(0);
            publicationsDao.create(publication);

            if (! tags.isEmpty()) {
                insertTags(tags, conn, publication);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            throw new DBException(e);
        }
    }

    private void insertTags(List<String> tags, ConnectionSource conn, Publication publication) throws SQLException {
        // Get Existing tag
        Dao<Tag, String> tagsDao = DaoManager.createDao(conn, Tag.class);
        List<Tag> existingTags = tagsDao.queryBuilder()
                .where()
                .eq(Tag.DESCRIPTION_FIELD, tags)
                .query();

        // Create non existing tags
        HashSet<String> tagSet = new HashSet<>(tags);
        tagSet.removeAll(existingTags
                .stream()
                .map(Tag::getDescription)
                .collect(Collectors.toList()));

        List<Tag> newTags = tagSet.stream()
                .map((d) -> new Tag(UUID.randomUUID().toString(), d))
                .collect(Collectors.toList());

        tagsDao.create(newTags);


        // Associate publication with tags
        List<Tag> totalTags = new ArrayList<>();
        totalTags.addAll(newTags);
        totalTags.addAll(existingTags);

        Dao<PublicationTag, String> publicationTagsDao = DaoManager.createDao(conn, PublicationTag.class);
        publicationTagsDao.create(
                totalTags.stream()
                        .map(t -> new PublicationTag(t.getId(), publication.getId()))
                        .collect(Collectors.toList())
        );
    }
}
