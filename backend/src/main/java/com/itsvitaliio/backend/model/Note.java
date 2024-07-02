package com.itsvitaliio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Note {
    private Long noteId;
    private String title;

    // Getters and Setters
}