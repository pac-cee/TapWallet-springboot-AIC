package rw.ac.auca.tapwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

/** Body for opening a wallet. */
public record WalletRequest(
        @NotNull(message = "ownerId is required")
        Long ownerId,

        @NotNull(message = "openingBalance is required")
        @DecimalMin(value = "0.00", message = "Opening balance cannot be negative")
        BigDecimal openingBalance,

        @NotNull(message = "Currency is required")
        @Pattern(regexp = "^RWF$", message = "This academic wallet currently supports RWF only")
        String currency) {
}
