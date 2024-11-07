package com.itsvitaliio.backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class BatchNoteChildRequest {
    private List<NoteChildDto> noteChildren; // Contains all children to be created, updated, or deleted
}
