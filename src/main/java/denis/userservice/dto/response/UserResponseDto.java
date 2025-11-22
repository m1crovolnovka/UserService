package denis.userservice.dto.response;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String name,
        String surname,
        LocalDate birthDate,
        String email,
        boolean active,
        List<PaymentCardResponseDto> paymentCards
) {}