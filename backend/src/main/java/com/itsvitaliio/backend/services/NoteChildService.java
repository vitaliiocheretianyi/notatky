package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.AddTextEntryRequest;
import com.itsvitaliio.backend.dto.NoteChildDto;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.models.TextNode;
import com.itsvitaliio.backend.models.ImageNode;
import com.itsvitaliio.backend.repositories.NoteChildRepository;
import com.itsvitaliio.backend.repositories.TextNodeRepository;
import com.itsvitaliio.backend.repositories.ImageNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NoteChildService {

    private final NoteChildRepository noteChildRepository;
    private final TextNodeRepository textNodeRepository;
    private final ImageNodeRepository imageNodeRepository;

    @Autowired
    public NoteChildService(NoteChildRepository noteChildRepository, 
                            TextNodeRepository textNodeRepository, 
                            ImageNodeRepository imageNodeRepository) {
        this.noteChildRepository = noteChildRepository;
        this.textNodeRepository = textNodeRepository;
        this.imageNodeRepository = imageNodeRepository;
    }

    @Transactional
    public void addTextEntry(String userId, AddTextEntryRequest request) {
        if (noteChildRepository.existsByNoteIdAndPosition(request.getNoteId(), request.getPosition())) {
            shiftPositionsUpward(request.getNoteId(), request.getPosition());
        }

        TextNode textNode = new TextNode();
        textNode.setContent(request.getText());
        textNode = textNodeRepository.save(textNode);

        NoteChild noteChild = new NoteChild();
        noteChild.setNoteId(request.getNoteId());
        noteChild.setChildId(textNode.getId());
        noteChild.setType("text");
        noteChild.setPosition(request.getPosition());
        noteChildRepository.save(noteChild);
    }

    public void shiftPositionsUpward(String noteId, int startPosition) {
        List<NoteChild> noteChildrenToShift = noteChildRepository.findByNoteIdAndPositionGreaterThanEqual(noteId, startPosition);

        for (NoteChild noteChild : noteChildrenToShift) {
            noteChild.setPosition(noteChild.getPosition() + 1);
        }

        noteChildRepository.saveAll(noteChildrenToShift);
    }

    @Transactional
    public List<NoteChildDto> getNoteChildrenByNoteId(String noteId) {
        List<NoteChild> noteChildren = noteChildRepository.findByNoteId(noteId);

        return noteChildren.stream()
                .sorted((nc1, nc2) -> Integer.compare(nc1.getPosition(), nc2.getPosition()))
                .map(noteChild -> {
                    NoteChildDto dto = new NoteChildDto();
                    dto.setId(noteChild.getId());
                    dto.setType(noteChild.getType());
                    dto.setPosition(noteChild.getPosition());

                    if ("text".equals(noteChild.getType())) {
                        TextNode textNode = textNodeRepository.findById(noteChild.getChildId())
                                .orElseThrow(() -> new IllegalArgumentException("Text node not found"));
                        dto.setTextNode(textNode);
                    } else if ("image".equals(noteChild.getType())) {
                        ImageNode imageNode = imageNodeRepository.findById(noteChild.getChildId())
                                .orElseThrow(() -> new IllegalArgumentException("Image node not found"));
                        dto.setImageNode(imageNode);
                    }

                    return dto;
                }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteNoteChild(String noteId, String childId) {
        NoteChild noteChildToDelete = noteChildRepository.findById(childId)
                .orElseThrow(() -> new InvalidEntryException("Note child not found"));

        int deletedPosition = noteChildToDelete.getPosition();

        noteChildRepository.delete(noteChildToDelete);

        updatePositionsAfterDeletion(noteId, deletedPosition);
    }

    private void updatePositionsAfterDeletion(String noteId, int deletedPosition) {
        List<NoteChild> noteChildrenToUpdate = noteChildRepository.findByNoteIdAndPositionGreaterThanEqual(noteId, deletedPosition);

        for (NoteChild noteChild : noteChildrenToUpdate) {
            noteChild.setPosition(noteChild.getPosition() - 1);
        }

        noteChildRepository.saveAll(noteChildrenToUpdate);
    }
}
