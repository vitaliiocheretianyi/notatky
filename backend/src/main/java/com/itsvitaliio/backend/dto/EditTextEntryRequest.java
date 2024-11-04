package com.itsvitaliio.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditTextEntryRequest {
    private String noteId;
    private String textEntryId;
    private String newText;
    private Integer position;  // Rename field to position
}
