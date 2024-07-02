package com.itsvitaliio.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.itsvitaliio.backend.model.Note;
import com.itsvitaliio.backend.service.NoteService;

import java.util.List;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @PostMapping
    public void createNote(@RequestBody Note note) {
        noteService.createNote(note);
    }

    @GetMapping("/{id}")
    public Note getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }

    @GetMapping
    public List<Note> getAllNotes() {
        return noteService.getAllNotes();
    }

    @PutMapping("/{id}")
    public void updateNote(@PathVariable Long id, @RequestBody Note note) {
        note.setNoteId(id);
        noteService.updateNote(note);
    }

    @DeleteMapping("/{id}")
    public void deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
    }
}