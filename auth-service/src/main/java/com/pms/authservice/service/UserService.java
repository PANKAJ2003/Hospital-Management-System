package com.pms.authservice.service;

import com.pms.authservice.dto.*;
import com.pms.authservice.enums.UserRole;
import com.pms.authservice.exception.UserCreationException;
import com.pms.authservice.grpc.PatientAuthServiceGrpcClient;
import com.pms.authservice.mapper.AuthPatientResponseMapper;
import com.pms.authservice.mapper.UserMapper;
import com.pms.authservice.model.User;
import com.pms.authservice.repository.UserRepository;
import com.pms.authservice.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import patient.AuthPatientResponse;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final PatientAuthServiceGrpcClient patientServiceGrpcClient;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, PatientAuthServiceGrpcClient patientServiceGrpcClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.patientServiceGrpcClient = patientServiceGrpcClient;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public SignUpResponseDTO createUserAccount(SignUpRequestDTO request, UserRole role) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        log.info("Creating user account for email: {}", request.getEmail());

        // Step 1: Create user
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = UserMapper.toUserModel(request.getEmail(), encodedPassword, role);
        User savedUser = userRepository.save(user);

        String patientId = null;

        try {
            // Step 2: Create patient
            AuthPatientResponse patientResponse = patientServiceGrpcClient.createPatient(request, savedUser.getId().toString());
            if (!patientResponse.getSuccess()) {
                log.error("Patient creation failed for email: {}", request.getEmail());
                throw new RuntimeException("Patient creation failed");
            }
            patientId = patientResponse.getPatientId();
            log.info("Created patient with id: {}", patientId);
            log.info("Patient created successfully for email: {}", request.getEmail());

            UserDTO userDTO = AuthPatientResponseMapper.toUserDTO(patientResponse);

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail(), role.name(), user.getId().toString(), patientId);

            SignUpResponseDTO response = new SignUpResponseDTO();
            response.setToken(token);
            response.setUser(userDTO);

            return response;

        } catch (Exception ex) {
            // Compensation: Rollback everything
            log.error("Failed to create complete user account, rolling back...", ex);
            // Delete user
            userRepository.delete(savedUser);
            throw new UserCreationException("Failed to create user account: " + ex.getMessage(), ex);
        }
    }

    public LoginResponseDTO getPatientByUserId(String token, LoginRequestDTO request) {
        try {
            User user = userRepository.findByEmail(request.getEmail()).orElse(null);
            if (user == null) {
                log.warn("Cannot get patient by userId: user not found");
                return null;
            }
            if (user.getRole().equals(UserRole.PATIENT)) {
                AuthPatientResponse patientResponse = patientServiceGrpcClient.getPatientByUserId(user.getId().toString());
                if (!patientResponse.getSuccess()) {
                    throw new RuntimeException("Patient not found for userId: " + user.getId());
                }

                log.info("Found patient with id: {}", patientResponse.getPatientId());

                return new LoginResponseDTO(token,
                        AuthPatientResponseMapper.toUserDTO(patientResponse));
            }
            return null; // Add return for non-PATIENT roles
        } catch (Exception e) {
            log.error("Cannot get patient by userId: {}", request.getEmail(), e);
            return null;
        }
    }

    public String getPatientIdByUserId(String userId, String role) {
        if (!"PATIENT".equalsIgnoreCase(role)) return null;
        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("UserId cannot be null");
        }
        return patientServiceGrpcClient.getPatientIdByUserId(userId);
    }
}
