package denis.userservice.controller;

import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import denis.userservice.service.PaymentCardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class PaymentCardController {

    private final PaymentCardService cardService;

    @PostMapping
    public ResponseEntity<PaymentCardResponseDto> create(@Valid @RequestBody PaymentCardRequestDto dto) {
        return new ResponseEntity<>(cardService.create(dto), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(cardService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<PaymentCardResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAll(pageable));
    }

    @GetMapping("/user-cards/{userId}")
    public ResponseEntity<List<PaymentCardResponseDto>> getByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(cardService.getByUserId(userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentCardResponseDto> update(
            @PathVariable UUID id,
            @Valid @RequestBody PaymentCardRequestDto dto
    ) {
        return ResponseEntity.ok(cardService.update(id, dto));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        cardService.activate(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        cardService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cardService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
