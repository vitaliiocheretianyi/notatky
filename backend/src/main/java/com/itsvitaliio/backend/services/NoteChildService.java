package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.NoteChildDto;
import com.itsvitaliio.backend.models.ImageNode;
import com.itsvitaliio.backend.models.NoteChild;
import com.itsvitaliio.backend.models.TextNode;
import com.itsvitaliio.backend.repositories.ImageNodeRepository;
import com.itsvitaliio.backend.repositories.NoteChildRepository;
import com.itsvitaliio.backend.repositories.TextNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoteChildService {


    @Value("${image.upload.dir}") // Inject the directory path from application.properties
    private String imageUploadDir;
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

    @Transactional(readOnly = true)
    public List<NoteChildDto> getAllNoteChildren(String noteId) {
        try {
            // Fetch note children from the repository
            List<NoteChild> noteChildren = noteChildRepository.findByNoteId(noteId);
            
            // Sort the noteChildren by position before mapping them to DTOs
            return noteChildren.stream()
                    .sorted(Comparator.comparingInt(NoteChild::getPosition)) // Sorting by position in ascending order
                    .map(noteChild -> {
                        NoteChildDto dto = new NoteChildDto();
                        dto.setId(noteChild.getId());
                        dto.setNoteId(noteChild.getNoteId());
                        dto.setType(noteChild.getType());
                        dto.setPosition(noteChild.getPosition());

                        // Set the correct child (text or image) in the DTO
                        if ("text".equalsIgnoreCase(noteChild.getType())) {
                            textNodeRepository.findById(noteChild.getChildId()).ifPresent(dto::setTextNode);
                        } else if ("image".equalsIgnoreCase(noteChild.getType())) {
                            imageNodeRepository.findById(noteChild.getChildId()).ifPresent(dto::setImageNode);
                        }
                        return dto;
                    })
                    .collect(Collectors.toList()); // Collect sorted results into a list
        } catch (Exception e) {
            System.err.println("getAllNoteChildren - Error fetching note children: " + e.getMessage());
            throw e; // Re-throw the exception if needed for further handling
        }
    }


    @Transactional
    public List<NoteChildDto> syncNoteChildren(String noteId, List<NoteChildDto> incomingDtos) {
        List<NoteChildDto> updatedDtos = incomingDtos.stream()
            .peek(dto -> {
                // Ensure each incoming DTO has the correct NoteId
                if (dto.getNoteId() == null || dto.getNoteId().isEmpty()) {
                    dto.setNoteId(noteId);
                }

                // Log any DTO that still lacks a NoteId for further debugging
                if (dto.getNoteId() == null || dto.getNoteId().isEmpty()) {
                    System.err.println("syncNoteChildren - Incoming DTO with missing NoteId: " + dto);
                }
            }).collect(Collectors.toList());

        try {
            List<NoteChild> existingNoteChildren = noteChildRepository.findByNoteId(noteId);
            Map<String, NoteChild> existingMap = existingNoteChildren.stream()
                    .collect(Collectors.toMap(NoteChild::getId, child -> child));

            // Iterate through the incoming DTOs
            for (NoteChildDto dto : updatedDtos) {
                NoteChild existingChild = existingMap.get(dto.getId());

                if (existingChild == null) {
                    System.out.println("Creating a new note child: " + dto.toString());
                    // Create new note child if not found in existing ones
                    handleCreateOperation(dto);
                } else {
                    // Update existing note child and ensure position updates are handled
                    System.out.println("Updating existing note child: " + dto.getId());
                    handleUpdateOperation(dto, existingChild);
                    existingMap.remove(dto.getId());
                }
            }

            // Handle deletion of any remaining items that were not included in the update
            existingMap.values().forEach(this::handleDeleteOperation);
        } catch (Exception e) {
            System.err.println("syncNoteChildren - Error syncing note children: " + e.getMessage());
            throw e;
        }

        // Return the updated list of DTOs back to the frontend
        return updatedDtos;
    }

    private void handleUpdateOperation(NoteChildDto dto, NoteChild existingChild) {
        try {
            // Always update the position
            existingChild.setPosition(dto.getPosition());

            // Save the updated NoteChild to persist the position change
            noteChildRepository.save(existingChild);

            // Update text nodes if the type is text
            if ("text".equalsIgnoreCase(dto.getType())) {
                textNodeRepository.findById(existingChild.getChildId()).ifPresent(textNode -> {
                    if (!textNode.getContent().equals(dto.getTextNode().getContent())) {
                        textNode.setContent(dto.getTextNode().getContent());
                        textNodeRepository.save(textNode);
                    }
                });
            } 
            // Update image nodes if the type is image
            else if ("image".equalsIgnoreCase(dto.getType())) {
                imageNodeRepository.findById(existingChild.getChildId()).ifPresent(imageNode -> {
                    if (!imageNode.getImagePath().equals(dto.getImageNode().getImagePath())) {
                        imageNode.setImagePath(dto.getImageNode().getImagePath());
                        imageNodeRepository.save(imageNode);
                    }
                });
            }
        } catch (Exception e) {
            System.err.println("handleUpdateOperation - Error updating note child: " + e.getMessage());
            throw e;
        }
    }


    @Transactional
    private void handleCreateOperation(NoteChildDto dto) {
        try {
            // Generate IDs if they are missing
            String generatedId = dto.getId() != null ? dto.getId() : UUID.randomUUID().toString();
            dto.setId(generatedId);

            String childId = null;
            String type = dto.getType(); // Should be "text" or "image"

            // Handle TextNode creation
            if ("text".equalsIgnoreCase(type) && dto.getTextNode() != null) {
                String textNodeId = dto.getTextNode().getId() != null
                        ? dto.getTextNode().getId()
                        : UUID.randomUUID().toString();

                TextNode textNode = new TextNode(
                        textNodeId,
                        dto.getTextNode().getContent() != null ? dto.getTextNode().getContent() : ""
                );
                textNodeRepository.save(textNode);

                // Set the childId to the generated textNodeId for the NoteChild
                childId = textNodeId;

                // Update the DTO with the correct TextNode ID
                dto.getTextNode().setId(textNodeId);
            }

            // Handle ImageNode creation
            else if ("image".equalsIgnoreCase(type) && dto.getImageNode() != null) {
                String imageNodeId = dto.getImageNode().getId() != null
                        ? dto.getImageNode().getId()
                        : UUID.randomUUID().toString();

                ImageNode imageNode = new ImageNode(
                        imageNodeId,
                        dto.getImageNode().getImagePath() != null ? dto.getImageNode().getImagePath() : ""
                );
                imageNodeRepository.save(imageNode);

                // Set the childId to the generated imageNodeId for the NoteChild
                childId = imageNodeId;

                // Update the DTO with the correct ImageNode ID
                dto.getImageNode().setId(imageNodeId);
            }

            // Ensure that the NoteChild has the correct type and childId set
            if (childId != null && type != null) {
                // Create and save the NoteChild with the correct type and childId
                NoteChild noteChild = new NoteChild(
                        UUID.randomUUID().toString(),
                        dto.getNoteId(),
                        childId, // Set the childId to the generated TextNode or ImageNode ID
                        type, // Set the type correctly (text or image)
                        dto.getPosition()
                );
                noteChildRepository.save(noteChild);

                // Update the DTO with the correct NoteChild ID
                dto.setId(noteChild.getId());
            } else {
                throw new IllegalArgumentException("Failed to create NoteChild: missing childId or type");
            }

        } catch (Exception e) {
            System.err.println("handleCreateOperation - Error creating note child: " + e.getMessage());
            throw e;
        }
    }

    private void handleDeleteOperation(NoteChild existingChild) {
        try {
            noteChildRepository.delete(existingChild);
            if ("text".equalsIgnoreCase(existingChild.getType())) {
                textNodeRepository.deleteById(existingChild.getChildId());
            } else if ("image".equalsIgnoreCase(existingChild.getType())) {
                imageNodeRepository.deleteById(existingChild.getChildId());
            }
        } catch (Exception e) {
            System.err.println("handleDeleteOperation - Error deleting note child: " + e.getMessage());
            throw e;
        }
    }

    @Transactional
    public List<NoteChildDto> uploadImage(String noteId, String noteChildId, MultipartFile imageFile) {
        try {
            // Ensure the directory exists, create it if it doesn't
            Path directoryPath = Paths.get(imageUploadDir);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            // Generate a unique filename or use the file's original name
            String imageFileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

            // Save the image to the filesystem
            Path imagePath = directoryPath.resolve(imageFileName);
            Files.write(imagePath, imageFile.getBytes());

            // Find the existing NoteChild by ID
            NoteChild noteChild = noteChildRepository.findById(noteChildId)
                    .orElseThrow(() -> new RuntimeException("NoteChild not found"));

            // If the NoteChild is a text node, remove the associated TextNode
            if ("text".equalsIgnoreCase(noteChild.getType())) {
                textNodeRepository.deleteById(noteChild.getChildId());
            }

            // Create a new ImageNode and save it in the database
            ImageNode imageNode = new ImageNode(UUID.randomUUID().toString(), imagePath.toString());
            imageNodeRepository.save(imageNode);

            // Update the NoteChild to point to the new ImageNode
            noteChild.setType("image");
            noteChild.setChildId(imageNode.getId());
            noteChildRepository.save(noteChild);

            // Return the updated list of note children
            return getAllNoteChildren(noteId);
        } catch (IOException e) {
            System.err.println("\nERROR handling image upload: " + e.getMessage());
            throw new RuntimeException(e);
        } catch (Exception e) {
            System.err.println("\nERROR handling image upload: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
