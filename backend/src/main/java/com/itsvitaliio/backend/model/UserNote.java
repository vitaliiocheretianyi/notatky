package com.itsvitaliio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNote {
    private Long userNoteId;
    private Long userId;
    private Long noteId;

    // Getters and Setters
}