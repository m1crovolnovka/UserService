package denis.userservice.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record PaymentCardResponseDto(
        UUID id,
        UUID userId,
        String number,
        String holder,
        LocalDate expirationDate,
        boolean active
) {
}
