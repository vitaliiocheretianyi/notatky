// TextNode.java
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
    private String id; // We will handle ID assignment in the constructor or service

    @Column(name = "content", nullable = false)
    private String content;

    // Add a method to set the ID if it's null
    @PrePersist
    public void ensureId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString(); // Generate ID if not set
        }
    }

    @Override
    public String toString() {
        return "TextNode{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
