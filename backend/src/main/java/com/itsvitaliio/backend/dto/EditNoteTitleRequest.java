package com.itsvitaliio.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditNoteTitleRequest {
    private String noteId;
    private String title;
}
