package com.itsvitaliio.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Entry {
    private Long entryId;
    private String entryType;
    private String content;
    private String url;

    // Getters and Setters
}