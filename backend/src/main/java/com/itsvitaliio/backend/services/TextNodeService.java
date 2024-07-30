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

    @Autowired
    public TextNodeService(NoteRepository noteRepository, TextNodeRepository textNodeRepository,
                           NoteChildRepository noteChildRepository, UserNoteRepository userNoteRepository) {
        this.noteRepository = noteRepository;
        this.textNodeRepository = textNodeRepository;
        this.noteChildRepository = noteChildRepository;
        this.userNoteRepository = userNoteRepository;
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
            throw new InvalidEntryException("Position " + request.getPosition() + " is already taken for note " + noteId);
        }

        TextNode textNode = new TextNode();
        textNode.setId(UUID.randomUUID().toString());
        textNode.setContent(request.getText());
        textNodeRepository.save(textNode);

        NoteChild noteChild = new NoteChild();
        noteChild.setId(UUID.randomUUID().toString());
        noteChild.setNote(noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException("Note not found")));
        noteChild.setType("text");
        noteChild.setChildId(textNode.getId());
        noteChild.setPosition(request.getPosition());
        noteChildRepository.save(noteChild);
    }

    @Transactional
    public void editTextEntry(String userId, EditTextEntryRequest request) throws NoteNotFoundException, InvalidEntryException {
        Optional<TextNode> textNodeOptional = textNodeRepository.findById(request.getTextEntryId());
        if (textNodeOptional.isEmpty()) {
            throw new NoteNotFoundException("Text entry not found");
        }

        TextNode textNode = textNodeOptional.get();
        if (!noteChildRepository.existsByNoteIdAndChildId(request.getNoteId(), request.getTextEntryId())) {
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

        NoteChild noteChild = noteChildRepository.findByNoteIdAndChildId(noteId, textEntryId)
                .orElseThrow(() -> new InvalidEntryException("Text entry not found in the note"));

        textNodeRepository.deleteById(textEntryId);
        noteChildRepository.delete(noteChild);
    }
}
