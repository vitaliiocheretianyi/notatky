package com.itsvitaliio.backend.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.itsvitaliio.backend.model.Note;

import java.util.List;

@Repository
public class NoteDaoImpl implements NoteDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void createNote(Note note) {
        String sql = "INSERT INTO notes (title) VALUES (?)";
        jdbcTemplate.update(sql, note.getTitle());
    }

    @Override
    public Note getNoteById(Long noteId) {
        String sql = "SELECT * FROM notes WHERE note_id = ?";
        return jdbcTemplate.queryForObject(sql, new Object[]{noteId}, (rs, rowNum) ->
                new Note(
                        rs.getLong("note_id"),
                        rs.getString("title")
                ));
    }

    @Override
    public List<Note> getAllNotes() {
        String sql = "SELECT * FROM notes";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Note(
                        rs.getLong("note_id"),
                        rs.getString("title")
                ));
    }

    @Override
    public void updateNote(Note note) {
        String sql = "UPDATE notes SET title = ? WHERE note_id = ?";
        jdbcTemplate.update(sql, note.getTitle(), note.getNoteId());
    }

    @Override
    public void deleteNote(Long noteId) {
        String sql = "DELETE FROM notes WHERE note_id = ?";
        jdbcTemplate.update(sql, noteId);
    }
}
