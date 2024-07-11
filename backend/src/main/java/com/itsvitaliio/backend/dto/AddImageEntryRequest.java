package com.itsvitaliio.backend.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddImageEntryRequest {
    private Long noteId;
    private String imageUrl;
}
