package com.itsvitaliio.backend.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itsvitaliio.backend.dto.AddImageEntryRequest;
import com.itsvitaliio.backend.dto.AddTextEntryRequest;
import com.itsvitaliio.backend.dto.CreateNoteRequest;
import com.itsvitaliio.backend.dto.EditImageEntryRequest;
import com.itsvitaliio.backend.dto.EditTextEntryRequest;

@RestController
@RequestMapping("/notatky/note")
public class NoteController {

    @PostMapping("/create")
    public ResponseEntity<?> createNote(@RequestBody CreateNoteRequest request) {
        // Create note logic
        return null;
    }

    @GetMapping("/open/{noteId}")
    public ResponseEntity<?> openNote(@PathVariable Long noteId) {
        // Open note logic
        return null;
    }

    @DeleteMapping("/delete/{noteId}")
    public ResponseEntity<?> deleteNote(@PathVariable Long noteId) {
        // Delete note logic
        return null;
    }

    @PostMapping("/text-entry/add")
    public ResponseEntity<?> addTextEntry(@RequestBody AddTextEntryRequest request) {
        // Add text entry to a note logic
        return null;
    }

    @PutMapping("/text-entry/edit/{textEntryId}")
    public ResponseEntity<?> editTextEntry(@PathVariable Long textEntryId, @RequestBody EditTextEntryRequest request) {
        // Edit text entry of a note logic
        return null;
    }

    @DeleteMapping("/text-entry/delete/{textEntryId}")
    public ResponseEntity<?> deleteTextEntry(@PathVariable Long textEntryId) {
        // Delete text entry of a note logic
        return null;
    }

    @PostMapping("/image-entry/add")
    public ResponseEntity<?> addImageEntry(@RequestBody AddImageEntryRequest request) {
        // Add image entry to a note logic
        return null;
    }

    @PutMapping("/image-entry/edit/{imageEntryId}")
    public ResponseEntity<?> editImageEntry(@PathVariable Long imageEntryId, @RequestBody EditImageEntryRequest request) {
        // Edit image entry of a note logic
        return null;
    }

    @DeleteMapping("/image-entry/delete/{imageEntryId}")
    public ResponseEntity<?> deleteImageEntry(@PathVariable Long imageEntryId) {
        // Delete image entry of a note logic
        return null;
    }
}
