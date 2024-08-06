package com.itsvitaliio.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itsvitaliio.backend.dto.AddImageEntryRequest;
import com.itsvitaliio.backend.dto.DeleteImageEntryRequest;
import com.itsvitaliio.backend.dto.EditImageEntryRequest;
import com.itsvitaliio.backend.dto.NoteChildDto;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.ImageNode;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.repositories.ImageNodeRepository;
import com.itsvitaliio.backend.repositories.NoteChildRepository;
import com.itsvitaliio.backend.repositories.NoteRepository;
import com.itsvitaliio.backend.repositories.UserNoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ImageNodeService {

    private static final Logger logger = Logger.getLogger(ImageNodeService.class.getName());

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private final NoteRepository noteRepository;
    private final ImageNodeRepository imageNodeRepository;
    private final NoteChildRepository noteChildRepository;
    private final UserNoteRepository userNoteRepository;

    public ImageNodeService(NoteRepository noteRepository, ImageNodeRepository imageNodeRepository,
                            NoteChildRepository noteChildRepository, UserNoteRepository userNoteRepository) {
        this.noteRepository = noteRepository;
        this.imageNodeRepository = imageNodeRepository;
        this.noteChildRepository = noteChildRepository;
        this.userNoteRepository = userNoteRepository;
    }

    private boolean isPositionDuplicate(String noteId, int position) {
        return noteChildRepository.existsByNoteIdAndPosition(noteId, position);
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private String saveFileAndGetPath(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File must have an original filename");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String randomFileName = generateRandomString() + fileExtension;
        Path fileNameAndPath = Paths.get(imageUploadDir, randomFileName);
        Files.write(fileNameAndPath, file.getBytes());

        return randomFileName;
    }

    @Transactional
    public NoteChildDto addImageEntry(String userId, AddImageEntryRequest request, MultipartFile file) throws IOException {
        String noteId = request.getNoteId();
        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        if (isPositionDuplicate(noteId, request.getPosition())) {
            throw new InvalidEntryException("Position " + request.getPosition() + " is already taken for note " + noteId);
        }

        String imagePath = saveFileAndGetPath(file);

        ImageNode imageNode = new ImageNode();
        imageNode.setId(UUID.randomUUID().toString());
        imageNode.setImagePath(imagePath);  // Set the new image path here
        imageNodeRepository.save(imageNode);

        NoteChild noteChild = new NoteChild();
        noteChild.setId(UUID.randomUUID().toString());
        noteChild.setNote(noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException("Note not found")));
        noteChild.setType("image");
        noteChild.setChildId(imageNode.getId());
        noteChild.setPosition(request.getPosition());
        noteChildRepository.save(noteChild);

        NoteChildDto dto = new NoteChildDto();
        dto.setId(noteChild.getId());
        dto.setType(noteChild.getType());
        dto.setImageNode(imageNode);
        dto.setPosition(noteChild.getPosition());  // Set position

        return dto;
    }

    @Transactional
    public void editImageEntry(String userId, EditImageEntryRequest request, MultipartFile file) throws NoteNotFoundException, InvalidEntryException, IOException {
        Optional<ImageNode> imageNodeOptional = imageNodeRepository.findById(request.getImageEntryId());
        if (imageNodeOptional.isEmpty()) {
            throw new NoteNotFoundException("Image entry not found");
        }

        ImageNode imageNode = imageNodeOptional.get();
        if (!noteChildRepository.existsByNoteIdAndChildId(request.getNoteId(), request.getImageEntryId())) {
            throw new InvalidEntryException("Image entry does not belong to the specified note");
        }

        String newImagePath = saveFileAndGetPath(file);  // Save the file and get the new path
        imageNode.setImagePath(newImagePath);
        imageNodeRepository.save(imageNode);
    }

    @Transactional
    public void deleteImageEntry(String userId, DeleteImageEntryRequest request) {
        String noteId = request.getNoteId();
        String imageEntryId = request.getImageEntryId();

        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        NoteChild noteChild = noteChildRepository.findByNoteIdAndChildId(noteId, imageEntryId)
                .orElseThrow(() -> new InvalidEntryException("Image entry not found in the note"));

        imageNodeRepository.deleteById(imageEntryId);
        noteChildRepository.delete(noteChild);
    }
}
