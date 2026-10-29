package com.foodexpiry.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // REGISTER API
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody Map<String, String> data) {

        String name = data.get("name");
        String email = data.get("email");
        String password = data.get("password");
        String role = data.get("role");

        if (name == null || name.isBlank()
                || email == null || email.isBlank()
                || password == null || password.length() < 6
                || role == null
                || !(role.equalsIgnoreCase("customer")
                || role.equalsIgnoreCase("seller"))) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Enter valid details. Password must contain at least 6 characters."
                    ));
        }

        email = email.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Email is already registered."
                    ));
        }

        User user = new User(
                name.trim(),
                email,
                passwordEncoder.encode(password),
                role.toLowerCase()
        );

        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "Registration successful. Please log in."
                ));
    }

    // LOGIN API
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> data) {

        String email = data.get("email");
        String password = data.get("password");
        String role = data.get("role");

        if (email == null || email.isBlank()
                || password == null || password.isBlank()
                || role == null || role.isBlank()) {

            return ResponseEntity.badRequest()
                    .body(Map.of(
                            "message",
                            "Please fill in all fields."
                    ));
        }

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (user == null
                || !passwordEncoder.matches(
                        password, user.getPassword())
                || !user.getRole().equalsIgnoreCase(role)) {

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email, password, or role."
                    ));
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Login successful.",
                        "name", user.getName(),
                        "role", user.getRole()
                )
        );
    }
}