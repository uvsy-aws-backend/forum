CREATE TABLE publication_vote (
    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid(),
    publication_id VARCHAR NOT NULL,
    user_id VARCHAR  NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp,
    FOREIGN KEY (publication_id) REFERENCES publication (id)
);
