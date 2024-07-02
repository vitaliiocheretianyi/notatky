package com.itsvitaliio.backend.dao;

import java.util.List;
import com.itsvitaliio.backend.model.Note;

public interface NoteDao {
    void createNote(Note note);
    Note getNoteById(Long noteId);
    List<Note> getAllNotes();
    void updateNote(Note note);
    void deleteNote(Long noteId);
}