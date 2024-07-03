-- User table
CREATE TABLE IF NOT EXISTS User (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Note table
CREATE TABLE IF NOT EXISTS Note (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

-- UserNote relationship table
CREATE TABLE IF NOT EXISTS UserNote (
    user_id INT,
    note_id INT,
    PRIMARY KEY (user_id, note_id),
    FOREIGN KEY (user_id) REFERENCES User(id),
    FOREIGN KEY (note_id) REFERENCES Note(id)
);

-- TextNode table
CREATE TABLE IF NOT EXISTS TextNode (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL
);

-- ImageNode table
CREATE TABLE IF NOT EXISTS ImageNode (
    id INT AUTO_INCREMENT PRIMARY KEY,
    image_path VARCHAR(255) NOT NULL
);

-- NoteChild relationship table
CREATE TABLE IF NOT EXISTS NoteChild (
    note_id INT,
    child_id INT,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY (note_id, child_id),
    FOREIGN KEY (note_id) REFERENCES Note(id)
);

-- Additional constraints to enforce polymorphic relationships in NoteChild
ALTER TABLE NoteChild
ADD CONSTRAINT fk_textnode FOREIGN KEY (child_id)
REFERENCES TextNode(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_imagenode FOREIGN KEY (child_id)
REFERENCES ImageNode(id) ON DELETE CASCADE;
