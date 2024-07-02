CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE notes (
    note_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL
);

CREATE TABLE user_notes (
    user_note_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    note_id BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (note_id) REFERENCES notes(note_id)
);

CREATE TABLE entries (
    entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    entry_type VARCHAR(255) NOT NULL,
    content TEXT,
    url VARCHAR(255)
);

CREATE TABLE note_entries (
    note_entry_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    note_id BIGINT NOT NULL,
    entry_id BIGINT NOT NULL,
    FOREIGN KEY (note_id) REFERENCES notes(note_id),
    FOREIGN KEY (entry_id) REFERENCES entries(entry_id)
);
