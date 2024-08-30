package com.itsvitaliio.backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsvitaliio.backend.dto.*;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.Note;
import com.itsvitaliio.backend.services.ImageNodeService;
import com.itsvitaliio.backend.services.NoteChildService;
import com.itsvitaliio.backend.services.NoteService;
import com.itsvitaliio.backend.services.TextNodeService;
import com.itsvitaliio.backend.utilities.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    @Autowired
    public NoteController(JwtUtil jwtUtil, NoteService noteService, TextNodeService textNodeService,
                          ImageNodeService imageNodeService, NoteChildService noteChildService,
                          ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.noteService = noteService;
        this.textNodeService = textNodeService;
        this.imageNodeService = imageNodeService;
        this.noteChildService = noteChildService;
        this.objectMapper = objectMapper;
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            return jwtUtil.extractUserId(jwt);
        }
        return null;
    }

    @PostMapping("/image/create")
    public ResponseEntity<NoteChildDto> uploadImage(HttpServletRequest request, @ModelAttribute ImageUploadRequest imageUploadRequest) {
        try {
            String userId = getUserIdFromToken(request);
            System.out.println("Received request with the following details:");
            System.out.println("User ID: " + userId);
            System.out.println("Note ID: " + imageUploadRequest.getNoteId());
            System.out.println("Note Child ID: " + imageUploadRequest.getNoteChildId());

            NoteChildDto noteChildDto = imageNodeService.addImageEntry(
                    userId,
                    imageUploadRequest.getNoteId(),
                    imageUploadRequest.getNoteChildId(),
                    imageUploadRequest.getFile()
            );
            return ResponseEntity.ok(noteChildDto);
        } catch (NoteNotFoundException e) {
            System.out.println("Note not found: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (InvalidEntryException e) {
            System.out.println("Invalid entry: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            System.out.println("Error processing image upload: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
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

    @GetMapping("/note/{noteId}/children")
    public ResponseEntity<?> getNoteChildrenByNoteId(HttpServletRequest request, @PathVariable String noteId) {
        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        try {
            System.out.println("Retrieving children for Note ID: " + noteId + " by User ID: " + userId);
            List<NoteChildDto> noteChildren = noteChildService.getNoteChildrenByNoteId(noteId);
            return ResponseEntity.ok(noteChildren);
        } catch (Exception e) {
            System.out.println("Error retrieving note children: " + e.getMessage());
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
            System.out.println("Creating text node for Note ID: " + textEntryRequest.getNoteId() + " by User ID: " + userId);
            noteChildService.addTextEntry(userId, textEntryRequest);
            return ResponseEntity.ok("Created Text Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error creating text node: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
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
            System.out.println("Editing text node with ID: " + textEntryRequest.getTextEntryId() + " for Note ID: " + textEntryRequest.getNoteId());
            textNodeService.editTextEntry(userId, textEntryRequest);
            return ResponseEntity.ok("Edited Text Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error editing text node: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
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
            System.out.println("Deleting text node with ID: " + deleteTextEntryRequest.getTextEntryId() + " from Note ID: " + deleteTextEntryRequest.getNoteId());
            textNodeService.deleteTextEntry(userId, deleteTextEntryRequest);
            return ResponseEntity.ok("Deleted Text Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error deleting text node: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(imageUploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                System.out.println("Image not found: " + filename);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.out.println("Error serving image: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
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
            System.out.println("Editing image node for Note ID: " + imageEntryRequest.getNoteId() + " by User ID: " + userId);
            imageNodeService.editImageEntry(userId, imageEntryRequest, file);
            return ResponseEntity.ok("Edited Image Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error editing image node: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
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
            System.out.println("Deleting image node with ID: " + deleteImageEntryRequest.getImageEntryId() + " from Note ID: " + deleteImageEntryRequest.getNoteId());
            imageNodeService.deleteImageEntry(userId, deleteImageEntryRequest);
            return ResponseEntity.ok("Deleted Image Entry Successfully");
        } catch (NoteNotFoundException | InvalidEntryException e) {
            System.out.println("Error deleting image node: " + e.getMessage());
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            System.out.println("Error processing request: " + e.getMessage());
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