package com.itsvitaliio.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteTextEntryRequest {
    private String noteId;
    private String textEntryId;
}
