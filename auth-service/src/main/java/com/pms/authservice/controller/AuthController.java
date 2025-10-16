package com.pms.authservice.controller;

import com.pms.authservice.dto.LoginRequestDTO;
import com.pms.authservice.dto.LoginResponseDTO;
import com.pms.authservice.dto.SignUpRequestDTO;
import com.pms.authservice.dto.SignUpResponseDTO;
import com.pms.authservice.enums.UserRole;
import com.pms.authservice.service.AuthService;
import com.pms.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Operation(summary = "Generate token for user login")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);

        return tokenOptional.map(s -> ResponseEntity.ok(userService.getPatientByUserId(s, loginRequestDTO)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());

    }

    @Operation(summary = "Validate Jwt token")
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            token = token.replace("Bearer ", "");
            if (!authService.validateToken(token)) {
                throw new Exception("Invalid token");
            }
            Map<String, Object> claims = authService.extractAllClaims(token);

            Map<String, Object> response = new HashMap<>();
            response.put("valid", true);
            response.put("email", claims.get("sub")); // subject
            response.put("role", claims.get("role"));
            response.put("userId", claims.get("userId"));
            response.put("patientId", claims.get("patientId"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("valid", false);
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @Operation(summary = "Signup user and create")
    @PostMapping("/signup")
    public ResponseEntity<SignUpResponseDTO> createUserPatient(@Valid @RequestBody SignUpRequestDTO signUpRequestDTO) {
        signUpRequestDTO.setRegisteredDate(LocalDate.now());
        SignUpResponseDTO userAccount = userService.createUserAccount(signUpRequestDTO, UserRole.PATIENT);
        return ResponseEntity.status(HttpStatus.CREATED).body(userAccount);
    }
}