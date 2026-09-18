package rw.ac.auca.tapwallet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import rw.ac.auca.tapwallet.domain.AccountStatus;

/** Body for updating a wallet's editable attributes (balance is never set directly). */
public record WalletUpdateRequest(
        @NotNull(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter code like RWF")
        String currency,

        @NotNull(message = "Status is required (ACTIVE or FROZEN)")
        AccountStatus status) {
}
