package com.mchis.auth;

import jakarta.validation.constraints.Email;

public record ForgotPasswordRequest(
        @Email
        String email
) {
}
