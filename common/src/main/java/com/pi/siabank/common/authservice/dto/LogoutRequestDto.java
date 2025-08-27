package com.pi.siabank.common.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogoutRequestDto {

    @NotBlank(message = "Refresh token cannot be blank")
    private String refreshToken;
}
