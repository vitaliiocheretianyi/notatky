package com.itsvitaliio.backend.repositories;

import com.itsvitaliio.backend.models.Note;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NoteRepository extends JpaRepository<Note, String> {
    //Optional<Note> findByIdAndUserId(String id, String userId);

}