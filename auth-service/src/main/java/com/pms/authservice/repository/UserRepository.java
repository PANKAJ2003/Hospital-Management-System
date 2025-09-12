package com.pms.authservice.repository;

import com.pms.authservice.model.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends Repository<User, UUID> {
    Optional<User> findByEmail(String email);
}