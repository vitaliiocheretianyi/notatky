package com.itsvitaliio.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUploadRequest {
    private String noteId;
    private String noteChildId;
    private MultipartFile file;
}
