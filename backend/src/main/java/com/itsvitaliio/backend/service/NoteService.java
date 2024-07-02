package com.itsvitaliio.backend.service;

import java.util.List;
import com.itsvitaliio.backend.model.Note;

public interface NoteService {
    void createNote(Note note);
    Note getNoteById(Long noteId);
    List<Note> getAllNotes();
    void updateNote(Note note);
    void deleteNote(Long noteId);
}