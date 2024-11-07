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
@Table(name = "note_children")
public class NoteChild {
    @Id
    private String id = UUID.randomUUID().toString();

    @Column(name = "note_id", nullable = false)
    private String noteId;

    @Column(name = "child_id", nullable = false)
    private String childId;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "position", nullable = false)
    private int position;

    // Assuming you're not using a relationship to the Note entity directly
    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }
}
