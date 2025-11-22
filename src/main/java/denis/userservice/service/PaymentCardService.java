package denis.userservice.service;

import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;


public interface PaymentCardService {
    PaymentCardResponseDto create(PaymentCardRequestDto dto);
    PaymentCardResponseDto getById(UUID id);
    Page<PaymentCardResponseDto> getAll(String cardNumber, String holderName, Pageable pageable);
    List<PaymentCardResponseDto> getByUserId(UUID userId);
    PaymentCardResponseDto update(UUID id, PaymentCardRequestDto dto);
    PaymentCardResponseDto activate(UUID id);
    PaymentCardResponseDto deactivate(UUID id);
    UUID delete(UUID id);
}
