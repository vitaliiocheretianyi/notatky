package com.itsvitaliio.backend.models;

import lombok.Data;
import jakarta.persistence.*;

import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    private String id;  // Update the id type to String

    private String username;
    private String email;
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles;

    public Set<String> getRoles() {
        return roles;
    }
}
