package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.models.UserNote;
import com.itsvitaliio.backend.repositories.UserNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserNoteService {
    private final UserNoteRepository userNoteRepository;

    @Autowired
    public UserNoteService(UserNoteRepository userNoteRepository) {
        this.userNoteRepository = userNoteRepository;
    }

    public List<UserNote> findByUserId(String userId) {
        return userNoteRepository.findByUserId(userId);
    }

    public boolean existsByUserIdAndNoteId(String userId, String noteId) {
        return userNoteRepository.existsByUserIdAndNoteId(userId, noteId);
    }
}
