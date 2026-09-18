package rw.ac.auca.tapwallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body for registering or fully updating a merchant. */
public record MerchantRequest(
        @NotBlank(message = "Business name is required")
        @Size(min = 3, max = 80, message = "Business name must be between 3 and 80 characters")
        String businessName,

        @NotBlank(message = "Merchant code is required")
        @Pattern(regexp = "^[A-Z]{3}-\\d{3}$", message = "Merchant code must look like CAF-001")
        String code,

        @NotNull(message = "operatorId is required")
        Long operatorId) {
}
