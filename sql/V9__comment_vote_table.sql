CREATE TABLE comment_vote (
    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid(),
    comment_id VARCHAR NOT NULL,
    user_id VARCHAR  NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp,
    FOREIGN KEY (comment_id) REFERENCES comment (id)
);
