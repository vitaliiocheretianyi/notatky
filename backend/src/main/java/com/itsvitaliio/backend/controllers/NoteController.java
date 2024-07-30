package com.itsvitaliio.backend.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsvitaliio.backend.dto.*;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.Note;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.services.ImageNodeService;
import com.itsvitaliio.backend.services.NoteChildService;
import com.itsvitaliio.backend.services.NoteService;
import com.itsvitaliio.backend.services.TextNodeService;
import com.itsvitaliio.backend.utilities.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/notatky")
public class NoteController {

    private final JwtUtil jwtUtil;
    private final NoteService noteService;
    private final TextNodeService textNodeService;
    private final ImageNodeService imageNodeService;
    private final NoteChildService noteChildService;
    private final ObjectMapper objectMapper;

    public NoteController(JwtUtil jwtUtil, NoteService noteService, TextNodeService textNodeService, ImageNodeService imageNodeService, NoteChildService noteChildService, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.noteService = noteService;
        this.textNodeService = textNodeService;
        this.imageNodeService = imageNodeService;
        this.objectMapper = objectMapper;
        this.noteChildService = noteChildService;
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
            return ResponseEntity.ok(createdNote);
        } catch (Exception e) {
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
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @GetMapping("/note/{noteId}/children")
    public ResponseEntity<?> getNoteChildrenByNoteId(HttpServletRequest request, @PathVariable String noteId) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            List<NoteChildDto> noteChildren = noteChildService.getNoteChildrenByNoteId(noteId);
            return ResponseEntity.ok(noteChildren);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PostMapping("/text/create")
    public ResponseEntity<?> createTextNode(HttpServletRequest request, @RequestBody AddTextEntryRequest textEntryRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            textNodeService.addTextEntry(userId, textEntryRequest);
            return ResponseEntity.ok("Created Text Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping("/text/edit")
    public ResponseEntity<?> editTextNode(HttpServletRequest request, @RequestBody EditTextEntryRequest textEntryRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            textNodeService.editTextEntry(userId, textEntryRequest);
            return ResponseEntity.ok("Edited Text Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @DeleteMapping("/text/delete")
    public ResponseEntity<?> deleteTextNode(HttpServletRequest request, @RequestBody DeleteTextEntryRequest deleteTextEntryRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            textNodeService.deleteTextEntry(userId, deleteTextEntryRequest);
            return ResponseEntity.ok("Deleted Text Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PostMapping(value = "/image/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createImageNode(HttpServletRequest request, 
                                             @RequestPart("file") MultipartFile file, 
                                             @RequestPart("data") String data) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            AddImageEntryRequest imageEntryRequest = objectMapper.readValue(data, AddImageEntryRequest.class);
            imageNodeService.addImageEntry(userId, imageEntryRequest, file);
            return ResponseEntity.ok("Created Image Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping(value = "/image/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> editImageNode(HttpServletRequest request, 
                                           @RequestPart("file") MultipartFile file, 
                                           @RequestPart("data") String data) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            EditImageEntryRequest imageEntryRequest = objectMapper.readValue(data, EditImageEntryRequest.class);
            imageNodeService.editImageEntry(userId, imageEntryRequest, file);
            return ResponseEntity.ok("Edited Image Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @DeleteMapping("/image/delete")
    public ResponseEntity<?> deleteImageNode(HttpServletRequest request, @RequestBody DeleteImageEntryRequest deleteImageEntryRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            imageNodeService.deleteImageEntry(userId, deleteImageEntryRequest);
            return ResponseEntity.ok("Deleted Image Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @PutMapping("/edit")
    public ResponseEntity<?> editNoteTitle(HttpServletRequest request, @RequestBody EditNoteTitleRequest editNoteTitleRequest) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            noteService.editNoteTitle(userId, editNoteTitleRequest);
            return ResponseEntity.ok("Edited Note Title Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
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
            noteService.deleteNote(userId, deleteNoteRequest);
            return ResponseEntity.ok("Deleted Note Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
