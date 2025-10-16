package com.pms.authservice.mapper;

import com.pms.authservice.enums.UserRole;
import com.pms.authservice.model.User;

public class UserMapper {

    public static User toUserModel(
            String email,
            String password,
            UserRole role
    ) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
