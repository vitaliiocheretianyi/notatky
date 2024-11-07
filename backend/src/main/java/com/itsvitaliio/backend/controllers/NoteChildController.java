package com.itsvitaliio.backend.controllers;

import com.itsvitaliio.backend.dto.BatchNoteChildRequest;
import com.itsvitaliio.backend.dto.NoteChildDto;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.ImageNode;
import com.itsvitaliio.backend.repositories.ImageNodeRepository;
import com.itsvitaliio.backend.services.NoteChildService;
import com.itsvitaliio.backend.utilities.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/notatky/note-children")
@CrossOrigin(origins = "http://localhost:4200")  // Replace with your frontend URL
public class NoteChildController {

    private final JwtUtil jwtUtil;
    private final NoteChildService noteChildService;
    private final ImageNodeRepository imageNodeRepository;

    @Autowired
    public NoteChildController(JwtUtil jwtUtil, NoteChildService noteChildService, ImageNodeRepository imageNodeRepository) {
        this.jwtUtil = jwtUtil;
        this.noteChildService = noteChildService;
        this.imageNodeRepository = imageNodeRepository;
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
        try {
            // Get the list of updated note children from the service
            List<NoteChildDto> updatedNoteChildren = noteChildService.syncNoteChildren(noteId, batchRequest.getNoteChildren());
            return ResponseEntity.ok(updatedNoteChildren); // Return the updated list to the frontend
        } catch (NoteNotFoundException e) {
            System.out.println("\nNote not found: " + e.getMessage());
            return ResponseEntity.status(404).body(null); // Note not found
        } catch (Exception e) {
            System.out.println("\nINTERNAL SERVER ERROR: " + e.getMessage());
            return ResponseEntity.status(500).body(null); // Internal Server Error
        }
    }

    @PostMapping("/upload/{noteId}/{noteChildId}")
    public ResponseEntity<List<NoteChildDto>> uploadImage(
            @PathVariable String noteId,
            @PathVariable String noteChildId,
            @RequestParam("image") MultipartFile imageFile,
            HttpServletRequest request) {

        System.out.println("Image upload request received for Note ID: " + noteId);

        String userId = getUserIdFromToken(request);
        if (userId == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }

        try {
            // Call the service to handle the image upload
            List<NoteChildDto> updatedNoteChildren = noteChildService.uploadImage(noteId, noteChildId, imageFile);
            return ResponseEntity.ok(updatedNoteChildren);
        } catch (Exception e) {
            System.err.println("Error uploading image: " + e.getMessage());
            return ResponseEntity.status(500).body(null); // Internal Server Error
        }
    }


    // Endpoint to retrieve an image by imageNodeId
    @GetMapping("/images/{imageNodeId}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageNodeId) {
        // Log that the request is received
        System.err.println("\nReceived request to get image\n");
        try {
            // Fetch the ImageNode from the repository to get the file path
            ImageNode imageNode = imageNodeRepository.findById(imageNodeId)
                    .orElseThrow(() -> new RuntimeException("Image not found"));

            // Convert the file path from the ImageNode into a Path object
            Path imagePath = Paths.get(imageNode.getImagePath());

            // Load the image as a resource
            Resource imageResource = new UrlResource(imagePath.toUri());

            // If the image does not exist or is not readable, return 404
            if (!imageResource.exists() || !imageResource.isReadable()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            
            // Determine the content type (e.g., image/jpeg, image/png)
            MediaType mediaType = MediaType.IMAGE_JPEG; // default, adjust if necessary
            String contentType = Files.probeContentType(imagePath);
            if (contentType != null) {
                mediaType = MediaType.parseMediaType(contentType);
            }

            // Return the image as a response entity
            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + imageNode.getImagePath() + "\"")
                    .body(imageResource);

        } catch (Exception e) {
            System.err.println("\nError retrieving image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
