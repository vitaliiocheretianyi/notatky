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

-- NoteChild relationship table
CREATE TABLE IF NOT EXISTS note_children (
    id VARCHAR(255) PRIMARY KEY,
    note_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    child_id VARCHAR(255) NOT NULL,
    position INT NOT NULL,  // Rename column to position
    FOREIGN KEY (note_id) REFERENCES notes(id),
    FOREIGN KEY (child_id) REFERENCES text_nodes(id) ON DELETE CASCADE,
    FOREIGN KEY (child_id) REFERENCES image_nodes(id) ON DELETE CASCADE
);


-- Additional constraints to enforce polymorphic relationships in NoteChild
ALTER TABLE note_children
ADD CONSTRAINT fk_textnode FOREIGN KEY (child_id)
REFERENCES text_nodes(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_imagenode FOREIGN KEY (child_id)
REFERENCES image_nodes(id) ON DELETE CASCADE;
