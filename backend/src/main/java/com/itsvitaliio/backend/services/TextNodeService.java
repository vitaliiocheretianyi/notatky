package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.AddTextEntryRequest;
import com.itsvitaliio.backend.dto.EditTextEntryRequest;
import com.itsvitaliio.backend.dto.DeleteTextEntryRequest;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.models.TextNode;
import com.itsvitaliio.backend.repositories.NoteChildRepository;
import com.itsvitaliio.backend.repositories.NoteRepository;
import com.itsvitaliio.backend.repositories.TextNodeRepository;
import com.itsvitaliio.backend.repositories.UserNoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class TextNodeService {

    private final NoteRepository noteRepository;
    private final TextNodeRepository textNodeRepository;
    private final NoteChildRepository noteChildRepository;
    private final UserNoteRepository userNoteRepository;
    private final NoteChildService noteChildService;

    @Autowired
    public TextNodeService(NoteRepository noteRepository, TextNodeRepository textNodeRepository,
                           NoteChildRepository noteChildRepository, UserNoteRepository userNoteRepository,
                           NoteChildService noteChildService) {
        this.noteRepository = noteRepository;
        this.textNodeRepository = textNodeRepository;
        this.noteChildRepository = noteChildRepository;
        this.userNoteRepository = userNoteRepository;
        this.noteChildService = noteChildService;
    }

    private boolean isPositionDuplicate(String noteId, int position) {
        return noteChildRepository.existsByNoteIdAndPosition(noteId, position);
    }

    @Transactional
    public void addTextEntry(String userId, AddTextEntryRequest request) {
        String noteId = request.getNoteId();
        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        if (isPositionDuplicate(noteId, request.getPosition())) {
            // If the position is already taken, shift other entries upward
            noteChildService.shiftPositionsUpward(noteId, request.getPosition());
        }

        TextNode textNode = new TextNode();
        textNode.setId(UUID.randomUUID().toString());
        textNode.setContent(request.getText());
        textNodeRepository.save(textNode);

        NoteChild noteChild = new NoteChild();
        noteChild.setId(UUID.randomUUID().toString());
        noteChild.setNoteId(noteId);  // Set the noteId directly
        noteChild.setType("text");
        noteChild.setChildId(textNode.getId());  // Use the TextNode ID here
        noteChild.setPosition(request.getPosition());
        noteChildRepository.save(noteChild);
    }

    @Transactional
    public void editTextEntry(String userId, EditTextEntryRequest request) throws NoteNotFoundException, InvalidEntryException {
        NoteChild noteChild = noteChildRepository.findById(request.getTextEntryId())
                .orElseThrow(() -> new InvalidEntryException("Note child not found"));

        String textNodeId = noteChild.getChildId();

        TextNode textNode = textNodeRepository.findById(textNodeId)
                .orElseThrow(() -> new NoteNotFoundException("Text entry not found"));

        if (!noteChildRepository.existsByNoteIdAndChildId(request.getNoteId(), textNodeId)) {
            throw new InvalidEntryException("Text entry does not belong to the specified note");
        }

        textNode.setContent(request.getNewText());
        textNodeRepository.save(textNode);
    }

    @Transactional
    public void deleteTextEntry(String userId, DeleteTextEntryRequest request) {
        String noteId = request.getNoteId();
        String textEntryId = request.getTextEntryId();

        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        NoteChild noteChild = noteChildRepository.findById(textEntryId)
                .orElseThrow(() -> new InvalidEntryException("Note child not found"));

        String textNodeId = noteChild.getChildId();

        noteChildService.deleteNoteChild(noteId, textEntryId);

        textNodeRepository.deleteById(textNodeId);
    }
}
