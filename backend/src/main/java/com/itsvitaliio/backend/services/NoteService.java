package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.*;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.ImageNode;
import com.itsvitaliio.backend.models.Note;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.models.TextNode;
import com.itsvitaliio.backend.models.User;
import com.itsvitaliio.backend.models.UserNote;
import com.itsvitaliio.backend.repositories.ImageNodeRepository;
import com.itsvitaliio.backend.repositories.NoteChildRepository;
import com.itsvitaliio.backend.repositories.NoteRepository;
import com.itsvitaliio.backend.repositories.TextNodeRepository;
import com.itsvitaliio.backend.repositories.UserNoteRepository;
import com.itsvitaliio.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteService {

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final UserNoteRepository userNoteRepository;
    private final UserNoteService userNoteService;
    private final NoteChildRepository noteChildRepository;
    private final TextNodeRepository textNodeRepository;
    private final ImageNodeRepository imageNodeRepository;

    @Autowired
    public NoteService(NoteRepository noteRepository, UserRepository userRepository, UserNoteRepository userNoteRepository, UserNoteService userNoteService, NoteChildRepository noteChildRepository, TextNodeRepository textNodeRepository, ImageNodeRepository imageNodeRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.userNoteRepository = userNoteRepository;
        this.userNoteService = userNoteService;
        this.noteChildRepository = noteChildRepository;
        this.textNodeRepository = textNodeRepository;
        this.imageNodeRepository = imageNodeRepository;
    }

    @Transactional
    public Note createNoteWithUserAssociation(String userId, CreateNoteRequest noteRequest) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new NoteNotFoundException("User not found");
        }

        User user = userOptional.get();
        Note note = new Note();
        note.setId(UUID.randomUUID().toString());
        note.setTitle(noteRequest.getTitle());
        note.setLastInteractedWith(LocalDateTime.now());
        noteRepository.save(note);

        UserNote userNote = new UserNote();
        userNote.setUser(user);
        userNote.setNote(note);
        userNoteRepository.save(userNote);

        return note;
    }

    public List<Note> getAllNotesForUser(String userId) {
        List<UserNote> userNotes = userNoteService.findByUserId(userId);
        return userNotes.stream()
                .map(UserNote::getNote)
                .collect(Collectors.toList());
    }

    @Transactional
    public void editNoteTitle(String userId, EditNoteTitleRequest request) {
        String noteId = request.getNoteId();
        Optional<UserNote> userNoteOptional = userNoteRepository.findByUserIdAndNoteId(userId, noteId);
        if (userNoteOptional.isEmpty()) {
            throw new NoteNotFoundException("User and note do not match");
        }

        Note note = userNoteOptional.get().getNote();
        note.setTitle(request.getTitle());
        note.setLastInteractedWith(LocalDateTime.now());
        noteRepository.save(note);
    }


    @Transactional
    public void deleteNote(String userId, DeleteNoteRequest request) {
        String noteId = request.getNoteId();
        Optional<UserNote> userNoteOptional = userNoteRepository.findByUserIdAndNoteId(userId, noteId);
        if (userNoteOptional.isEmpty()) {
            throw new NoteNotFoundException("User and note do not match");
        }

        // Delete all NoteChild entries related to the note
        List<NoteChild> noteChildren = noteChildRepository.findByNoteId(noteId);
        for (NoteChild noteChild : noteChildren) {
            String childId = noteChild.getChildId();
            if (noteChild.getType().equals("text")) {
                textNodeRepository.deleteById(childId);
            } else if (noteChild.getType().equals("image")) {
                imageNodeRepository.deleteById(childId);
            }
        }
        noteChildRepository.deleteAll(noteChildren);

        // Delete the UserNote entry
        userNoteRepository.delete(userNoteOptional.get());

        // Delete the note itself
        noteRepository.deleteById(noteId);
    }
}
