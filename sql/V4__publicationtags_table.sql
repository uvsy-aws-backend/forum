CREATE TABLE publicationtag (
    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid(),
    publication_id VARCHAR  NOT NULL,
    tag_id VARCHAR  NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp,
    FOREIGN KEY (publication_id) REFERENCES publication (id),
    FOREIGN KEY (tag_id) REFERENCES tag (id)
);
