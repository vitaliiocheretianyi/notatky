package com.itsvitaliio.backend.service;

import com.itsvitaliio.backend.dto.UserDTO;
import com.itsvitaliio.backend.model.User;

import java.util.List;

public interface UserService {
    void createUser(User user);
    User getUserById(Long userId);
    List<User> getAllUsers();
    User login(String usernameOrEmail, String password);
    void updateUser(Long userId, UserDTO userDTO);
    void deleteUser(Long userId);
}
