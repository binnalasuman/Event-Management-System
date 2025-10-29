package com.suman.eventmanagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "users",
        indexes = {
                @Index(name = "uk_users_email", columnList = "email", unique = true)
        })
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // matches bigint auto_increment
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    @NotBlank
    private String name;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    @Email
    @NotBlank
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    @NotBlank
    private String password;

    @Column(name = "role", length = 255) // NULL allowed in DB; make optional
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "Users{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
