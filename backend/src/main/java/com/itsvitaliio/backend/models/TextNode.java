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
@Table(name = "text_nodes")
public class TextNode {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "content", nullable = false)
    private String content;

    @Override
    public String toString() {
        return "TextNode{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
