package com.itsvitaliio.backend.models;

import java.util.UUID;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image_nodes")
public class ImageNode {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Override
    public String toString() {
        return "ImageNode{" +
                "id='" + id + '\'' +
                ", imagePath='" + imagePath + '\'' +
                '}';
    }
}
