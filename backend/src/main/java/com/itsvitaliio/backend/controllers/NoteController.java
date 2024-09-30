package com.itsvitaliio.backend.controllers;

import com.itsvitaliio.backend.dto.*;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.Note;
import com.itsvitaliio.backend.services.NoteService;
import com.itsvitaliio.backend.utilities.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notatky")
public class NoteController {

    private final JwtUtil jwtUtil;
    private final NoteService noteService;

    @Autowired
    public NoteController(JwtUtil jwtUtil, NoteService noteService) {
        this.jwtUtil = jwtUtil;
        this.noteService = noteService;
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            return jwtUtil.extractUserId(jwt);
        }
        return null;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createNote(HttpServletRequest request, @RequestBody CreateNoteRequest noteRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            Note createdNote = noteService.createNoteWithUserAssociation(userId, noteRequest);
            System.out.println("Created Note ID: " + createdNote.getId());
            return ResponseEntity.ok(createdNote);
        } catch (Exception e) {
            System.out.println("Error creating note: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @GetMapping("/notes")
    public ResponseEntity<?> getAllNotes(HttpServletRequest request) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        try {
            List<Note> notes = noteService.getAllNotesForUser(userId);
            return ResponseEntity.ok().body(notes);
        } catch (Exception e) {
            System.out.println("Error retrieving notes: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editNoteTitle(HttpServletRequest request, @RequestBody EditNoteTitleRequest editNoteTitleRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        System.out.println("\n\n\n\nReceived request from user: " + userId);
        System.out.println("Note in question: " + editNoteTitleRequest.getNoteId());
        System.out.println("New title: " + editNoteTitleRequest.getTitle());
        try {
            System.out.println("Editing note title for Note ID: " + editNoteTitleRequest.getNoteId() + " by User ID: " + userId);
            noteService.editNoteTitle(userId, editNoteTitleRequest);
            return ResponseEntity.ok("Edited Note Title Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error editing note title: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteNote(HttpServletRequest request, @RequestBody DeleteNoteRequest deleteNoteRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            System.out.println("Deleting note with ID: " + deleteNoteRequest.getNoteId() + " by User ID: " + userId);
            noteService.deleteNote(userId, deleteNoteRequest);
            return ResponseEntity.ok("Deleted Note Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error deleting note: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
