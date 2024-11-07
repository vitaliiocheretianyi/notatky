package com.itsvitaliio.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddImageEntryRequest {
    private String noteId; // Change to String
    private String imageUrl;
    private Integer position;

    public AddImageEntryRequest(String noteId, String imageUrl) {
        this.noteId = noteId;
        this.imageUrl = imageUrl;
    }
}
