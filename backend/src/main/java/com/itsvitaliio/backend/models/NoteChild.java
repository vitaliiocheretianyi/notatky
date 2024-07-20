package com.itsvitaliio.backend.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "note_children")
public class NoteChild {
    @Id
    private String id = UUID.randomUUID().toString();

    @ManyToOne
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "child_id", nullable = false)
    private String childId;

    @Column(name = "position", nullable = false)  // Rename column to position
    private Integer position;
    
    @Override
    public String toString() {
        return "NoteChild{" +
                "id='" + id + '\'' +
                ", note=" + note +
                ", type='" + type + '\'' +
                ", childId='" + childId + '\'' +
                ", position=" + position +
                '}';
    }
}
