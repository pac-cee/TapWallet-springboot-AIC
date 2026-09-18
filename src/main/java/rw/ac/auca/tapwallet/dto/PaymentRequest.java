package rw.ac.auca.tapwallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Body for paying a merchant from a wallet. */
public record PaymentRequest(
        @NotNull(message = "walletId is required")
        Long walletId,

        @NotNull(message = "merchantId is required")
        Long merchantId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 17, fraction = 2, message = "Amount may have at most 2 decimal places")
        BigDecimal amount,

        @Size(max = 120, message = "Note must be at most 120 characters")
        String note) {
}
