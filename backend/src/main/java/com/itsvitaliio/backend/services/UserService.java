package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.ChangeEmailRequest;
import com.itsvitaliio.backend.dto.ChangePasswordRequest;
import com.itsvitaliio.backend.dto.ChangeUsernameRequest;
import com.itsvitaliio.backend.dto.LoginRequest;
import com.itsvitaliio.backend.dto.RegisterRequest;
import com.itsvitaliio.backend.dto.ServiceResponse;
import com.itsvitaliio.backend.exceptions.InvalidEntryException;
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
import com.itsvitaliio.backend.utilities.IdGenerator;
import com.itsvitaliio.backend.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;
    private final UserNoteRepository userNoteRepository;
    private final NoteChildRepository noteChildRepository;
    private final NoteRepository noteRepository;
    private final TextNodeRepository textNodeRepository;
    private final ImageNodeRepository imageNodeRepository;

    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";
    private static final Pattern passwordPattern = Pattern.compile(PASSWORD_PATTERN);

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtUtil jwtUtil, UserNoteRepository userNoteRepository, NoteChildRepository noteChildRepository, NoteRepository noteRepository, TextNodeRepository textNodeRepository, ImageNodeRepository imageNodeRepository) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;

        this.userNoteRepository = userNoteRepository;
        this.noteChildRepository = noteChildRepository;
        this.noteRepository = noteRepository;
        this.textNodeRepository = textNodeRepository;
        this.imageNodeRepository = imageNodeRepository;
    }


    @Transactional(readOnly = true)
    public User getUserById(String id) throws Exception {
        return userRepository.findById(id)
                .orElseThrow(() -> new Exception("User not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        return org.springframework.security.core.userdetails.User.withUsername(username)
                .password(user.getPassword())
                .roles(user.getRoles().toArray(new String[0]))
                .build();
    }

    public UserDetails loadUserById(String id) throws Exception {
        User user = getUserById(id);
        return org.springframework.security.core.userdetails.User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRoles().toArray(new String[0]))
                .build();
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public ServiceResponse<String> register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return new ServiceResponse<>(false, "Email already in use", null);
        }

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return new ServiceResponse<>(false, "Username already in use", null);
        }

        User user = new User();
        user.setId(IdGenerator.generateId());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getId());
        return new ServiceResponse<>(true, "User registered successfully", token);
    }

    public ServiceResponse<String> loginWithEmail(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty() || !bCryptPasswordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            return new ServiceResponse<>(false, "Invalid email or password", null);
        }
        String token = jwtUtil.generateToken(userOptional.get().getId());
        return new ServiceResponse<>(true, "Login successful", token);
    }

    public ServiceResponse<String> loginWithUsername(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty() || !bCryptPasswordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            return new ServiceResponse<>(false, "Invalid username or password", null);
        }
        String token = jwtUtil.generateToken(userOptional.get().getId());
        return new ServiceResponse<>(true, "Login successful", token);
    }

    @Transactional
    public void changeUsername(String userId, ChangeUsernameRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new InvalidEntryException("User not found");
        }
        User user = userOptional.get();
        user.setUsername(request.getNewUsername());
        userRepository.save(user);
    }

    @Transactional
    public void changeEmail(String userId, ChangeEmailRequest request) {
        if (!emailPattern.matcher(request.getNewEmail()).matches()) {
            throw new InvalidEntryException("Invalid email format");
        }

        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new InvalidEntryException("User not found");
        }
        User user = userOptional.get();
        user.setEmail(request.getNewEmail());
        userRepository.save(user);
    }

    @Transactional
    public String changePassword(String userId, ChangePasswordRequest request) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new InvalidEntryException("User not found");
        }
        User user = userOptional.get();
        if (!passwordPattern.matcher(request.getNewPassword()).matches()) {
            throw new InvalidEntryException("New password must contain at least one uppercase letter, one lowercase letter, one number, and one special character");
        }
        user.setPassword(bCryptPasswordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Generate a new JWT token
        return jwtUtil.generateToken(user.getId());
    }

    @Transactional
    public void deleteUserAccount(String userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new InvalidEntryException("User not found");
        }

        User user = userOptional.get();
        System.out.println(user);

        // Step 1: Extract all the IDs of the notes associated with the user
        List<UserNote> userNotes = userNoteRepository.findByUserId(userId);
        List<String> noteIds = userNotes.stream()
                .map(UserNote::getNote)
                .map(Note::getId)
                .collect(Collectors.toList());

        // Step 2: Extract all the IDs of note children associated with those notes
        List<NoteChild> noteChildren = noteChildRepository.findByNoteIdIn(noteIds);
        List<String> textNodeIds = noteChildren.stream()
                .filter(noteChild -> noteChild.getType().equals("text"))
                .map(NoteChild::getChildId)
                .collect(Collectors.toList());
        List<String> imageNodeIds = noteChildren.stream()
                .filter(noteChild -> noteChild.getType().equals("image"))
                .map(NoteChild::getChildId)
                .collect(Collectors.toList());

        // Step 3: Delete all entries from user_notes and note_children
        userNoteRepository.deleteAll(userNotes);
        noteChildRepository.deleteAll(noteChildren);

        // Step 4: Delete text_nodes and image_nodes
        textNodeRepository.deleteAllById(textNodeIds);
        imageNodeRepository.deleteAllById(imageNodeIds);

        // Step 5: Delete notes
        noteRepository.deleteAllById(noteIds);

        // Step 6: Delete the user itself
        userRepository.delete(user);
    }
}