package com.suman.eventmanagement.controller;

import com.suman.eventmanagement.auth.JwtService;
import com.suman.eventmanagement.repository.UserRepo;
import com.suman.eventmanagement.entity.Users; 
import com.suman.eventmanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;      
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
    @RestController
    @RequestMapping("/api/users")
    public class TestLoginController {

        private final UserService userService;

        public TestLoginController(UserService userService) {
            this.userService = userService;
        }


        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginDto req) {
            try {
                String result = service.loginManual(req.getEmail(), req.getPassword());

                if ("Success".equals(result)) {
                    var user = userRepo.findByEmail(req.getEmail()).orElseThrow();
                    String token = jwtService.generateToken(user.getEmail(), List.of("ROLE_" + user.getRole()));
                    return ResponseEntity.ok(Map.of("token", token, "role", user.getRole()));
                } else {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid email or password"));
            }
        }
    }
}



