package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.AddImageEntryRequest;
import com.itsvitaliio.backend.dto.EditImageEntryRequest;
import com.itsvitaliio.backend.dto.DeleteImageEntryRequest;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
import com.itsvitaliio.backend.exceptions.NoteNotFoundException;
import com.itsvitaliio.backend.models.ImageNode;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.repositories.ImageNodeRepository;
import com.itsvitaliio.backend.repositories.NoteChildRepository;
import com.itsvitaliio.backend.repositories.NoteRepository;
import com.itsvitaliio.backend.repositories.UserNoteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class ImageNodeService {

    public static String UPLOAD_DIRECTORY = System.getProperty("user.dir") + "/images";
    private static final Logger logger = Logger.getLogger(ImageNodeService.class.getName());

    @Value("${image.upload.dir}")
    private String imageUploadDir;

    private final NoteRepository noteRepository;
    private final ImageNodeRepository imageNodeRepository;
    private final NoteChildRepository noteChildRepository;
    private final UserNoteRepository userNoteRepository;

    @Autowired
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

    private String saveImage(MultipartFile file) throws IOException {
        StringBuilder fileNames = new StringBuilder();
        Path fileNameAndPath = Paths.get(imageUploadDir, file.getOriginalFilename());
        fileNames.append(file.getOriginalFilename());
        Files.write(fileNameAndPath, file.getBytes());
        return "Saved";
    }

    @Transactional
    public void addImageEntry(String userId, AddImageEntryRequest request, MultipartFile file) throws IOException {
        String noteId = request.getNoteId();
        if (!userNoteRepository.existsByUserIdAndNoteId(userId, noteId)) {
            throw new NoteNotFoundException("User and note do not match");
        }

        if (isPositionDuplicate(noteId, request.getPosition())) {
            throw new InvalidEntryException("Position " + request.getPosition() + " is already taken for note " + noteId);
        }

        String imagePath = saveImage(file);

        ImageNode imageNode = new ImageNode();
        imageNode.setId(UUID.randomUUID().toString());
        imageNode.setImagePath(imagePath);
        imageNodeRepository.save(imageNode);

        NoteChild noteChild = new NoteChild();
        noteChild.setId(UUID.randomUUID().toString());
        noteChild.setNote(noteRepository.findById(noteId).orElseThrow(() -> new NoteNotFoundException("Note not found")));
        noteChild.setType("image");
        noteChild.setChildId(imageNode.getId());
        noteChild.setPosition(request.getPosition());
        noteChildRepository.save(noteChild);
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

        // Assuming the file is saved to a path and the path is updated in the imagePath field
        String newImagePath = saveFileAndGetPath(file);  // Implement this method to handle file saving
        imageNode.setImagePath(newImagePath);
        imageNodeRepository.save(imageNode);
    }

    private String saveFileAndGetPath(MultipartFile file) throws IOException {
        // Implement the logic to save the file and return its path
        // This is a placeholder implementation
        return "/path/to/image/" + file.getOriginalFilename();
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
