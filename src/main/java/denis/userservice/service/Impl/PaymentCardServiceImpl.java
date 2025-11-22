package denis.userservice.service.Impl;

import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import denis.userservice.entity.PaymentCard;
import denis.userservice.entity.User;
import denis.userservice.exception.CardListFullException;
import denis.userservice.exception.CardNotFoundException;
import denis.userservice.exception.UserNotFoundException;
import denis.userservice.mapper.PaymentCardMapper;
import denis.userservice.repository.PaymentCardRepository;
import denis.userservice.repository.UserRepository;
import denis.userservice.service.PaymentCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "cards")
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardRepository cardRepository;
    private final UserRepository userRepository;
    private final PaymentCardMapper cardMapper;
    private final int MAX_NUMBER_OF_CARDS = 5;


    @Transactional
    @CachePut(key = "#result.id")
    @CacheEvict(cacheNames = "users", key = "#result.userId")
    @Override
    public PaymentCardResponseDto create(PaymentCardRequestDto dto) {
        PaymentCard card = cardMapper.toEntity(dto);
        User user = userRepository.findById(dto.userId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        if(user.getPaymentCards() != null && user.getPaymentCards().size() >= MAX_NUMBER_OF_CARDS) {
            throw new CardListFullException("Card list is full");
        }
        card.setUser(user);
        card.setActive(true);
        return cardMapper.toResponseDto(cardRepository.save(card));
    }

    @Cacheable(key = "#id")
    @Override
    public PaymentCardResponseDto getById(UUID id) {
        return cardMapper.toResponseDto(cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found")));
    }

    @Override
    public Page<PaymentCardResponseDto> getAll(String cardNumber, String holderName, Pageable pageable) {
        Specification<PaymentCard> spec = Specification.unrestricted();
        spec = cardSpecifications(spec, cardNumber, "cardNumber");
        spec = cardSpecifications(spec, holderName, "cardHolderName");
        Page<PaymentCard> cards = cardRepository.findAll(spec, pageable);
        return new PageImpl<>(
                cards.get().map(cardMapper::toResponseDto).toList(),
                pageable,
                cards.getTotalElements()
        );
    }

    private Specification<PaymentCard> cardSpecifications(
            Specification<PaymentCard> spec,
            String value,
            String fieldName
    ) {
        if (value != null && !value.isBlank()) {
            return spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get(fieldName)), "%" + value.toLowerCase() + "%"));
        }
        return spec;
    }

    @Override
    public List<PaymentCardResponseDto> getByUserId(UUID userId) {
        return cardRepository.findAllCardsByUserId(userId).stream().map(cardMapper::toResponseDto).toList();
    }

    @Transactional
    @CachePut(key = "#id")
    @CacheEvict(cacheNames = "users", key = "#result.userId")
    @Override
    public PaymentCardResponseDto update(UUID id, PaymentCardRequestDto dto) {
        PaymentCard card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setNumber(dto.number());
        card.setExpirationDate(dto.expirationDate());
        card.setHolder(dto.holder());
        return cardMapper.toResponseDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result.userId"),
            @CacheEvict(value = "cards", key = "#id")
    })
    public PaymentCardResponseDto activate(UUID id) {
        PaymentCard paymentCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        cardRepository.activate(id);
        paymentCard.setActive(true);
        return cardMapper.toResponseDto(paymentCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result.userId"),
            @CacheEvict(value = "cards", key = "#id")
    })
    public PaymentCardResponseDto deactivate(UUID id) {
        PaymentCard paymentCard = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"));
        cardRepository.deactivate(id);
        paymentCard.setActive(false);
        return cardMapper.toResponseDto(paymentCard);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "users", key = "#result"),
            @CacheEvict(value = "cards", key = "#id")
    })
    public UUID delete(UUID id) {
        UUID userId = cardRepository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Card not found"))
                .getUser()
                .getId();
        cardRepository.deleteById(id);
        return userId;
    }

}
