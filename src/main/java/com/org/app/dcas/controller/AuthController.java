package com.org.app.dcas.controller;

import com.org.app.dcas.model.Company;
import com.org.app.dcas.repository.CompanyRepository;
import com.org.app.dcas.config.JwtUtil;
import com.org.app.dcas.dto.LoginRequest;
import com.org.app.dcas.dto.LoginResponse;
import com.org.app.dcas.model.Users;
import com.org.app.dcas.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CompanyRepository companyRepository;

    public AuthController(UsersRepository usersRepository, PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil, CompanyRepository companyRepository) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.companyRepository = companyRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        log.info("login - attempt for username='{}'", request.getUsername());

        if (request.getUsername() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username and password are required"));
        }

        Users user = usersRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null) {
            log.warn("login - username='{}' not found", request.getUsername());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            log.warn("login - username='{}' account is inactive", request.getUsername());
            return ResponseEntity.status(403).body(Map.of("error", "Account is inactive"));
        }

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("login - invalid password for username='{}'", request.getUsername());
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getUserId());
        log.info("login - successful for username='{}', userId={}", user.getUsername(), user.getUserId());

        Company company = user.getCompany();
        String companyName = (company != null) ? company.getName() : null;

        return ResponseEntity.ok(new LoginResponse(
                token,
                user.getUserId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                companyName
        ));
    }

    /**
     * Logged-in user changes their own password.
     * POST /api/auth/change-password
     * Headers: Authorization: Bearer <token>
     * Body: { "currentPassword": "...", "newPassword": "..." }
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {

        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");

        if (currentPassword == null || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "currentPassword and newPassword are required"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "New password must be at least 6 characters"));
        }

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }

        String username = jwtUtil.getUsername(token);
        Users user = usersRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            log.warn("change-password - wrong current password for username='{}'", username);
            return ResponseEntity.badRequest().body(Map.of("error", "Current password is incorrect"));
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
        log.info("change-password - password updated for username='{}'", username);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    /**
     * Developer utility: generate a BCrypt hash for any plain-text value.
     * Use the returned hash in a direct SQL UPDATE to change a password.
     *
     * POST /api/auth/generate-hash
     * Body: { "value": "MyNewPassword123" }
     * Returns: { "hash": "$2a$10$..." }
     *
     * Then run:
     *   UPDATE users SET password_hash = '<hash>' WHERE username = 'someuser';
     */
    @PostMapping("/generate-hash")
    public ResponseEntity<?> generateHash(@RequestBody Map<String, String> body) {
        String value = body.get("value");
        if (value == null || value.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "value is required"));
        }
        String hash = passwordEncoder.encode(value);
        log.info("generate-hash - hash generated (value not logged for security)");
        return ResponseEntity.ok(Map.of("hash", hash));
    }

    /**
     * Admin utility: create a new user mapped to an existing company.
     * No UI needed — call from Postman/curl.
     *
     * POST /api/auth/create-user
     * Body: {
     *   "firstName": "John",
     *   "lastName":  "Doe",
     *   "email":     "john@acme.com",
     *   "username":  "john.doe",
     *   "password":  "SecurePass@1",
     *   "companyId": 2
     * }
     */
    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody Map<String, Object> body) {
        String firstName    = (String) body.get("firstName");
        String lastName     = (String) body.get("lastName");
        String email        = (String) body.get("email");
        String username     = (String) body.get("username");
        String password     = (String) body.get("password");
        Object companyIdObj = body.get("companyId");

        if (username == null || username.isBlank() || password == null || password.isBlank() || companyIdObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "username, password and companyId are required"));
        }
        if (password.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "password must be at least 6 characters"));
        }
        if (usersRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username already exists: " + username));
        }

        Long companyId = Long.valueOf(companyIdObj.toString());
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "companyId not found: " + companyId));
        }

        Users user = new Users();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsActive(true);
        user.setCompany(company);
        Users saved = usersRepository.save(user);

        log.info("create-user - created userId={}, username='{}', companyId={}", saved.getUserId(), username, companyId);
        return ResponseEntity.ok(Map.of(
                "message", "User created successfully",
                "userId", saved.getUserId(),
                "username", username,
                "companyId", companyId
        ));
    }

    /**
     * GET /api/auth/profile
     * Returns the logged-in user's profile including company details.
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        String username = jwtUtil.getUsername(token);
        Users user = usersRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }
        Company company = user.getCompany();
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("userId", user.getUserId());
        profile.put("username", user.getUsername());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("email", user.getEmail());
        profile.put("phoneNumber", user.getPhoneNumber());
        if (company != null) {
            profile.put("companyId", company.getCompanyId());
            profile.put("companyName", company.getName());
            profile.put("companyAddress", company.getAddress());
            profile.put("companyEmail", company.getEmail());
            profile.put("companyPhone", company.getPhoneNumber());
        }
        return ResponseEntity.ok(profile);
    }

    /**
     * PUT /api/auth/profile
     * Updates the logged-in user's personal and company details.
     * Body: { firstName, lastName, email, phoneNumber, companyName, companyAddress, companyEmail, companyPhone }
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> body) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        if (!jwtUtil.isValid(token)) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
        }
        String username = jwtUtil.getUsername(token);
        Users user = usersRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        if (body.containsKey("firstName")) user.setFirstName(body.get("firstName"));
        if (body.containsKey("lastName"))  user.setLastName(body.get("lastName"));
        if (body.containsKey("email"))     user.setEmail(body.get("email"));
        if (body.containsKey("phoneNumber")) user.setPhoneNumber(body.get("phoneNumber"));
        usersRepository.save(user);

        Company company = user.getCompany();
        if (company != null) {
            String newCompanyName = body.get("companyName");
            if (newCompanyName != null && !newCompanyName.isBlank()) company.setName(newCompanyName);
            if (body.containsKey("companyAddress")) company.setAddress(body.get("companyAddress"));
            if (body.containsKey("companyEmail"))   company.setEmail(body.get("companyEmail"));
            if (body.containsKey("companyPhone"))   company.setPhoneNumber(body.get("companyPhone"));
            companyRepository.save(company);
        }

        log.info("update-profile - updated userId={}", user.getUserId());
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }
}
