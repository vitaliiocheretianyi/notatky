package com.itsvitaliio.backend.repositories;

import com.itsvitaliio.backend.models.NoteChild;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteChildRepository extends JpaRepository<NoteChild, String> {
    Optional<NoteChild> findByNoteIdAndChildId(String noteId, String childId);
    boolean existsByNoteIdAndChildId(String noteId, String childId); 
    List<NoteChild> findByNoteIdIn(List<String> noteIds);

    // Corrected method
    boolean existsByNoteIdAndPosition(String noteId, int position);
    List<NoteChild> findByNoteIdAndPositionGreaterThanEqual(String noteId, int position);
    List<NoteChild> findByNoteId(String noteId);
    
}
