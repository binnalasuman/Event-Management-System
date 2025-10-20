package com.example.EventManagementSystem.entity;

import com.example.EventManagementSystem.auth.JwtService;
import com.example.EventManagementSystem.repository.UserRepo;
import com.example.EventManagementSystem.entity.Users; 
import com.example.EventManagementSystem.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;           // if you have one
    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepo;
    private final JwtService jwtService;

    public UserController(UserService service,
                          AuthenticationManager authenticationManager,
                          UserRepo userRepo,
                          JwtService jwtService) {
        this.service = service;
        this.authenticationManager = authenticationManager;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public Users register(@Valid @RequestBody Users user) {
        return service.register(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginDto req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

        var user = userRepo.findByEmail(req.getEmail()).orElseThrow();
        String token = jwtService.generateToken(user.getEmail(), List.of("ROLE_" + user.getRole()));
        return Map.of("token", token, "role", user.getRole());

    }
}

