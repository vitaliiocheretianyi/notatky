package com.itsvitaliio.backend.service;

import com.itsvitaliio.backend.dao.UserDao;
import com.itsvitaliio.backend.model.User;
import com.itsvitaliio.backend.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    @Override
    public void createUser(User user) {
        userDao.save(user);
    }

    @Override
    public User getUserById(Long userId) {
        return userDao.findById(userId).orElse(null);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public User login(String usernameOrEmail, String password) {
        User user = userDao.findByEmail(usernameOrEmail);
        if (user == null) {
            user = userDao.findByName(usernameOrEmail);
        }
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        } else {
            throw new RuntimeException("Invalid username/email or password");
        }
    }

    @Override
    public void updateUser(Long userId, UserDTO userDTO) {
        User user = userDao.findById(userId).orElse(null);
        if (user != null) {
            user.setName(userDTO.getName());
            user.setEmail(userDTO.getEmail());
            user.setPasswordHash(userDTO.getPassword());
            userDao.save(user);
        } else {
            throw new RuntimeException("User not found");
        }
    }

    @Override
    public void deleteUser(Long userId) {
        userDao.deleteById(userId);
    }
}
