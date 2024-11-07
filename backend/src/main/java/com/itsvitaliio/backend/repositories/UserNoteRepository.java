package com.itsvitaliio.backend.repositories;

import com.itsvitaliio.backend.models.UserNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNoteRepository extends JpaRepository<UserNote, Long> {
    List<UserNote> findByUserId(String userId);
    Optional<UserNote> findByUserIdAndNoteId(String userId, String noteId);
    boolean existsByUserIdAndNoteId(String userId, String noteId);
    Optional<UserNote> findByNoteIdAndUserId(String noteId, String userId);

}
