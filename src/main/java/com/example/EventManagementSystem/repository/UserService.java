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
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private AuthenticationManager authenticationManager;

    public Users register(Users user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public String login(String email, String rawPassword) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, rawPassword));
        return auth.isAuthenticated() ? "Success" : "Failure";
    }


    public String loginManual(String email, String rawPassword) {
        Users user = repo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));


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