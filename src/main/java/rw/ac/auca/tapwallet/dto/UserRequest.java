package rw.ac.auca.tapwallet.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import rw.ac.auca.tapwallet.domain.Role;

/** Body for creating or fully updating a user. */
public record UserRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 3, max = 60, message = "Full name must be between 3 and 60 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid address")
        String email,

        @NotBlank(message = "Phone is required")
        @Pattern(regexp = "^\\+?250\\d{9}$", message = "Phone must be a Rwandan number like +250788123456")
        String phone,

        @NotNull(message = "Role is required (ADMIN, CUSTOMER or MERCHANT)")
        Role role) {
}
