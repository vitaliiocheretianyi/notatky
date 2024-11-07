CREATE DATABASE IF NOT EXISTS Notatky;
USE Notatky;

-- User table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Note table
CREATE TABLE IF NOT EXISTS notes (
    id VARCHAR(255) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    last_interacted_with TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- UserNote relationship table
CREATE TABLE IF NOT EXISTS user_notes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    note_id VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (note_id) REFERENCES notes(id)
);

-- TextNode table
CREATE TABLE IF NOT EXISTS text_nodes (
    id VARCHAR(255) PRIMARY KEY,
    content TEXT NOT NULL
);

-- ImageNode table
CREATE TABLE IF NOT EXISTS image_nodes (
    id VARCHAR(255) PRIMARY KEY,
    image_path VARCHAR(255) NOT NULL
);

-- NoteChild relationship table without polymorphic constraints
CREATE TABLE IF NOT EXISTS note_children (
    id VARCHAR(255) PRIMARY KEY,
    note_id VARCHAR(255) NOT NULL,
    type ENUM('text', 'image') NOT NULL,  -- Enum to specify type clearly
    child_id VARCHAR(255) NOT NULL,
    position INT NOT NULL,
    FOREIGN KEY (note_id) REFERENCES notes(id)
);

-- Create additional indexes or triggers for validation (pseudo-code)
-- Ensure validation is handled at the application layer to verify child_id
-- corresponds to the type specified in the 'type' column
