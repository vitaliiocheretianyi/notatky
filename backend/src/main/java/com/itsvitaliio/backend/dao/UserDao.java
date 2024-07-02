package com.itsvitaliio.backend.dao;

import com.itsvitaliio.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByName(String name);
}
