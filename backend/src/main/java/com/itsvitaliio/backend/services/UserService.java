package com.itsvitaliio.backend.services;

import com.itsvitaliio.backend.dto.LoginRequest;
import com.itsvitaliio.backend.dto.RegisterRequest;
import com.itsvitaliio.backend.dto.ServiceResponse;
import com.itsvitaliio.backend.models.User;
import com.itsvitaliio.backend.repositories.UserRepository;
import com.itsvitaliio.backend.utilities.IdGenerator;
import com.itsvitaliio.backend.utilities.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.jwtUtil = jwtUtil;
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
        String token = jwtUtil.generateToken(userOptional.get().getUsername());
        return new ServiceResponse<>(true, "Login successful", token);
    }

    public ServiceResponse<String> loginWithUsername(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isEmpty() || !bCryptPasswordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            return new ServiceResponse<>(false, "Invalid username or password", null);
        }
        String token = jwtUtil.generateToken(userOptional.get().getUsername());
        return new ServiceResponse<>(true, "Login successful", token);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        UserBuilder builder = org.springframework.security.core.userdetails.User.withUsername(username);
        builder.password(user.getPassword());
        builder.roles(user.getRoles().toArray(new String[0]));
        return builder.build();
    }
}
