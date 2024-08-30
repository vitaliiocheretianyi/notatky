package com.itsvitaliio.backend.services;

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
    private final NoteChildService noteChildService;

    public ImageNodeService(NoteRepository noteRepository, ImageNodeRepository imageNodeRepository,
                            NoteChildRepository noteChildRepository, UserNoteRepository userNoteRepository,
                            NoteChildService noteChildService) {
        this.noteRepository = noteRepository;
        this.imageNodeRepository = imageNodeRepository;
        this.noteChildRepository = noteChildRepository;
        this.userNoteRepository = userNoteRepository;
        this.noteChildService = noteChildService;
    }

    private boolean isPositionDuplicate(String noteId, int position) {
        return noteChildRepository.existsByNoteIdAndPosition(noteId, position);
    }

    private String generateRandomString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public String saveFileAndGetPath(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IOException("File must have an original filename");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String randomFileName = generateRandomString() + fileExtension;
        Path fileNameAndPath = Paths.get(imageUploadDir, randomFileName);
        Files.write(fileNameAndPath, file.getBytes());

        return fileNameAndPath.getFileName().toString(); // Return filename only
    }

    @Transactional
    public NoteChildDto addImageEntry(String userId, String noteId, String noteChildId, MultipartFile file) throws IOException {
        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        if (!noteChildRepository.existsById(noteChildId)) {
            throw new InvalidEntryException("Note child does not exist");
        }

        String imagePath = saveFileAndGetPath(file);

        ImageNode imageNode = new ImageNode();
        imageNode.setId(UUID.randomUUID().toString());
        imageNode.setImagePath(imagePath);
        imageNodeRepository.save(imageNode);

        NoteChild noteChild = noteChildRepository.findById(noteChildId)
                .orElseThrow(() -> new InvalidEntryException("Note child not found"));

        noteChild.setChildId(imageNode.getId());
        noteChild.setType("image");
        noteChildRepository.save(noteChild);

        NoteChildDto dto = new NoteChildDto();
        dto.setId(noteChild.getId());
        dto.setType(noteChild.getType());
        dto.setImageNode(imageNode);
        dto.setPosition(noteChild.getPosition());

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

        String newImagePath = saveFileAndGetPath(file);
        imageNode.setImagePath(newImagePath);
        imageNodeRepository.save(imageNode);
    }

    @Transactional
    public void deleteImageEntry(String userId, DeleteImageEntryRequest request) {
        String noteId = request.getNoteId();
        String noteChildId = request.getImageEntryId(); // This is actually the noteChildId

        // Verify that the user has access to this note
        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        // Find the note child entry by noteId and noteChildId
        NoteChild noteChildToDelete = noteChildRepository.findById(noteChildId)
                .orElseThrow(() -> new InvalidEntryException("Note child not found"));

        // Ensure the note child is associated with the provided note ID
        if (!noteChildToDelete.getNoteId().equals(noteId)) {
            throw new InvalidEntryException("Note child is not associated with the provided note ID");
        }

        // Locate the associated image node ID
        String imageNodeId = noteChildToDelete.getChildId();

        // Delete the NoteChild entry
        noteChildService.deleteNoteChild(noteId, noteChildId);

        // Delete the associated ImageNode entry
        imageNodeRepository.deleteById(imageNodeId);
    }



}
