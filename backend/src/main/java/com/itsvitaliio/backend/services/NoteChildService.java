package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.NoteChildDto;
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
    private final NoteService noteService;
    private final TextNodeRepository textNodeRepository;
    private final ImageNodeRepository imageNodeRepository;

    @Autowired
    public NoteChildService(NoteChildRepository noteChildRepository, NoteService noteService,
                            TextNodeRepository textNodeRepository, ImageNodeRepository imageNodeRepository) {
        this.noteChildRepository = noteChildRepository;
        this.noteService = noteService;
        this.textNodeRepository = textNodeRepository;
        this.imageNodeRepository = imageNodeRepository;
    }

    @Transactional
    public List<NoteChildDto> getNoteChildrenByNoteId(String noteId) {
        List<NoteChild> noteChildren = noteChildRepository.findByNoteId(noteId);
        
        // Sort the children by position before converting to DTOs
        return noteChildren.stream()
                .sorted((nc1, nc2) -> Integer.compare(nc1.getPosition(), nc2.getPosition()))
                .map(noteChild -> {
                    NoteChildDto dto = new NoteChildDto();
                    dto.setId(noteChild.getId());
                    dto.setType(noteChild.getType());
                    
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
}
