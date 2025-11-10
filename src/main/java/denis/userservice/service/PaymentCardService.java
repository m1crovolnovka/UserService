package denis.userservice.service;

import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;


public interface PaymentCardService {
    PaymentCardResponseDto create(PaymentCardRequestDto dto);
    PaymentCardResponseDto getById(UUID id);
    Page<PaymentCardResponseDto> getAll(int page, int size);
    List<PaymentCardResponseDto> getByUserId(UUID userId);
    PaymentCardResponseDto update(UUID id, PaymentCardRequestDto dto);
    void activate(UUID id);
    void deactivate(UUID id);
    void delete(UUID id);
}
