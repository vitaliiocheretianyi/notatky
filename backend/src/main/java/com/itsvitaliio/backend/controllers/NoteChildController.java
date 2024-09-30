package com.itsvitaliio.backend.controllers;

import com.itsvitaliio.backend.dto.BatchNoteChildRequest;
import com.itsvitaliio.backend.dto.NoteChildDto;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.services.NoteChildService;
import com.itsvitaliio.backend.utilities.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notatky/note-children")
public class NoteChildController {

    private final JwtUtil jwtUtil;
    private final NoteChildService noteChildService;

    @Autowired
    public NoteChildController(JwtUtil jwtUtil, NoteChildService noteChildService) {
        this.jwtUtil = jwtUtil;
        this.noteChildService = noteChildService;
    }

    // Endpoint to fetch all note children for a given note ID
    @GetMapping("/all/{noteId}")
    public ResponseEntity<List<NoteChildDto>> getAllNoteChildren(
            @PathVariable String noteId,
            HttpServletRequest request) {

        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }

        try {
            List<NoteChildDto> noteChildren = noteChildService.getAllNoteChildren(noteId);
            return ResponseEntity.ok(noteChildren);
        } catch (NoteNotFoundException e) {
            System.out.println("\nNote not found: " + e.getMessage());
            return ResponseEntity.status(404).body(null); // Note not found
        } catch (Exception e) {
            System.out.println("\nINTERNAL SERVER ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body(null); // Internal Server Error
        }
    }

    private String getUserIdFromToken(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);
            return jwtUtil.extractUserId(jwt);
        }
        return null;
    }

    @PostMapping("/sync/{noteId}")
public ResponseEntity<List<NoteChildDto>> syncNoteChildren(
        @PathVariable String noteId,
        @RequestBody BatchNoteChildRequest batchRequest,
        HttpServletRequest request) {

    String userId = getUserIdFromToken(request);
    if (userId == null) {
        return ResponseEntity.status(401).body(null); // Unauthorized
    }

    System.out.println("\nReceived a request from user with id: " + userId);
    System.out.println("\nNote in request: " + noteId);

    // Log each NoteChildDto from the incoming batch request
    batchRequest.getNoteChildren().forEach(noteChild -> {
        System.out.println("Note Child ID: " + noteChild.getId());
        System.out.println("Note Child Type: " + noteChild.getType());
        System.out.println("Note Child Position: " + noteChild.getPosition());
        if (noteChild.getTextNode() != null) {
            System.out.println("TextNode Content: " + noteChild.getTextNode().getContent());
        }
        if (noteChild.getImageNode() != null) {
            System.out.println("ImageNode Path: " + noteChild.getImageNode().getImagePath());
        }
        System.out.println("------------------------------------");
    });

    try {
        // Get the list of updated note children from the service
        List<NoteChildDto> updatedNoteChildren = noteChildService.syncNoteChildren(noteId, batchRequest.getNoteChildren());
        System.out.println("\nUPDATED NOTE CHILDREN:\n");
        updatedNoteChildren.forEach(noteChild -> {
            System.out.println("Note Child ID: " + noteChild.getId());
            System.out.println("Note Child Type: " + noteChild.getType());
            System.out.println("Note Child Position: " + noteChild.getPosition());
            if (noteChild.getTextNode() != null) {
                System.out.println("TextNode Content: " + noteChild.getTextNode().getContent());
            }
            if (noteChild.getImageNode() != null) {
                System.out.println("ImageNode Path: " + noteChild.getImageNode().getImagePath());
            }
            System.out.println("------------------------------------");
        });
        return ResponseEntity.ok(updatedNoteChildren); // Return the updated list to the frontend
    } catch (NoteNotFoundException e) {
        System.out.println("\nNote not found: " + e.getMessage());
        return ResponseEntity.status(404).body(null); // Note not found
    } catch (Exception e) {
        System.out.println("\nINTERNAL SERVER ERROR: " + e.getMessage());
        return ResponseEntity.status(500).body(null); // Internal Server Error
    }
}

}
