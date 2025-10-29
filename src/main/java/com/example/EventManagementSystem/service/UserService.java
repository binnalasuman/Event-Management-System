package com.suman.eventmanagement.service;

import com.suman.eventmanagement.entity.Users;
import com.suman.eventmanagement.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepo repo;

    @Autowired
    private PasswordEncoder passwordEncoder;  // Use the shared bean from SecurityConfig

    @Autowired(required = false)
    private AuthenticationManager authenticationManager; // Optional if using JWT manually

    /** Register a new user (encode password before saving) **/
    public Users register(Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // Encrypt password before saving
        return repo.save(user);
    }

    /** Login using Spring AuthenticationManager (if configured) **/
    public String login(String email, String rawPassword) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword));
        return auth.isAuthenticated() ? "Success" : "Failure";
    }

    /** Manual login check with full debugging information **/
    public String loginManual(String email, String rawPassword) {
        Users user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        // Debugging section
        System.out.println("==== LOGIN DEBUG START ====");
        System.out.println("Email being checked: " + email);
        System.out.println("User found in database: " + user.getEmail());
        System.out.println("Database password (hashed): " + user.getPassword());
        System.out.println("Raw password input: " + rawPassword);

        boolean match = passwordEncoder.matches(rawPassword, user.getPassword());
        System.out.println("Password match? " + match);
        System.out.println("==== LOGIN DEBUG END ====");

        if (!match) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return "Success";
    }
}

