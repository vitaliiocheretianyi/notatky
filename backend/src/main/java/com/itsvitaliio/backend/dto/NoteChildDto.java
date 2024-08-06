package com.itsvitaliio.backend.dto;

import com.itsvitaliio.backend.models.TextNode;
import com.itsvitaliio.backend.models.ImageNode;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class NoteChildDto {
    private String id;
    private String type;
    private TextNode textNode;
    private ImageNode imageNode;
    private Integer position;
}
