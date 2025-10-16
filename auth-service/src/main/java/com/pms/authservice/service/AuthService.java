package com.pms.authservice.service;

import com.pms.authservice.dto.LoginRequestDTO;
import com.pms.authservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public Optional<String> authenticate(LoginRequestDTO loginRequestDTO) {
        return userService
                .findByEmail(loginRequestDTO.getEmail())
                .filter(u -> passwordEncoder.matches(loginRequestDTO.getPassword(), u.getPassword()))
                .flatMap(u -> {
                    String patientId = null;
                    if ("PATIENT".equalsIgnoreCase(u.getRole().name())) {
                        patientId = userService.getPatientIdByUserId(u.getId().toString(), u.getRole().name());
                        if (patientId == null) {
                            return Optional.empty(); // patient not found → authentication fails
                        }
                    }
                    String token = jwtUtil.generateToken(
                            u.getEmail(),
                            u.getRole().name(),
                            u.getId().toString(),
                            patientId
                    );
                    return Optional.of(token);
                });
    }

    public boolean validateToken(String token) {
        try {
            jwtUtil.validateToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public Map<String, Object> extractAllClaims(String token) {
        return jwtUtil.extractAllClaims(token);
    }
}
