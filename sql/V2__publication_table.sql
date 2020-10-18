CREATE TABLE publication (
    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR NOT NULL,
    description VARCHAR  NOT NULL,
    user_id VARCHAR  NOT NULL,
    program_id VARCHAR  NOT NULL,
    votes integer  NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp
);
