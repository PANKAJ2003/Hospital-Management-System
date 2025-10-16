package com.pms.authservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignUpResponseDTO {
    private String token;
    private UserDTO user;
}
