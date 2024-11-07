package com.itsvitaliio.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddTextEntryRequest {
    private String noteId;
    private String text;
    private Integer position;  // Rename field to position
}
