package rw.ac.auca.tapwallet.dto;

import rw.ac.auca.tapwallet.domain.AccountStatus;
import rw.ac.auca.tapwallet.domain.Role;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String fullName,
        String email,
        Role role,
        AccountStatus status) {
}
