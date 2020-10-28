CREATE TABLE comment (
    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id VARCHAR NOT NULL,
    publication_id VARCHAR  NOT NULL,
    content VARCHAR NOT NULL,
    votes INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp,
    FOREIGN KEY (publication_id) REFERENCES publication (id)
);
