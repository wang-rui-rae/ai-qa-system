package com.ai.qa.user.api.dto;

import lombok.Data;

@Data
public class UserLoginRequestDto {
    private String username;
    private String password;
}