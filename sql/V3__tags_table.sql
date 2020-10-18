CREATE TABLE tag (
    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid(),
    description VARCHAR  NOT NULL,
    created_at TIMESTAMP DEFAULT current_timestamp,
    updated_at TIMESTAMP DEFAULT current_timestamp
);
