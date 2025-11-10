package denis.userservice.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

public record PaymentCardRequestDto(
        @NotNull(message = "User ID is required")
        UUID userId,
        @NotBlank(message = "Card number cannot be blank")
        @Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
        String number,
        @NotBlank(message = "Holder name cannot be blank")
        String holder,
        @Future(message = "Expiration date must be in the future")
        LocalDate expirationDate
) {}
