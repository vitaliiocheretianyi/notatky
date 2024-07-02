package com.itsvitaliio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteEntry {
    private Long noteEntryId;
    private Long noteId;
    private Long entryId;

    // Getters and Setters
}