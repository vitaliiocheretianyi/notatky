package com.itsvitaliio.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.itsvitaliio.backend.dao.NoteDao;
import com.itsvitaliio.backend.model.Note;

import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    @Autowired
    private NoteDao noteDao;

    @Override
    public void createNote(Note note) {
        noteDao.createNote(note);
    }

    @Override
    public Note getNoteById(Long noteId) {
        return noteDao.getNoteById(noteId);
    }

    @Override
    public List<Note> getAllNotes() {
        return noteDao.getAllNotes();
    }

    @Override
    public void updateNote(Note note) {
        noteDao.updateNote(note);
    }

    @Override
    public void deleteNote(Long noteId) {
        noteDao.deleteNote(noteId);
    }
}
