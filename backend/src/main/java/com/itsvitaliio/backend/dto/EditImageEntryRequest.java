package com.itsvitaliio.backend.dto;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EditImageEntryRequest {
    private Long imageEntryId;
    private String newImageUrl;
}
