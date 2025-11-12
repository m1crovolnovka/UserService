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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

    @CachePut(key = "#result.id")
    @Override
    public PaymentCardResponseDto create(PaymentCardRequestDto dto) {
        PaymentCard card = cardMapper.toEntity(dto);
        User user = userRepository.findById(dto.userId()).orElseThrow(() -> new UserNotFoundException("User not found"));
        if(user.getPaymentCards() != null && user.getPaymentCards().size() >= 5) {
            throw new CardListFullException("Card list is full");
        }
        return cardMapper.toResponseDto(cardRepository.save(card));
    }

    @Cacheable(key = "#id")
    @Override
    public PaymentCardResponseDto getById(UUID id) {
        return cardMapper.toResponseDto(cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found")));
    }


    @Override
    public Page<PaymentCardResponseDto> getAll(Pageable pageable) {
        Page<PaymentCard> cards = cardRepository.findAll(pageable);
        return new PageImpl<>(
                cards.get().map(cardMapper::toResponseDto).toList(),
                pageable,
                cards.getTotalElements()
        );
    }

    @Override
    public List<PaymentCardResponseDto> getByUserId(UUID userId) {
        return cardRepository.findAllCardsByUserId(userId).stream().map(cardMapper::toResponseDto).toList();
    }

    @Transactional
    @CachePut(key = "#id")
    @Override
    public PaymentCardResponseDto update(UUID id, PaymentCardRequestDto dto) {
        PaymentCard card = cardRepository.findById(id).orElseThrow(() -> new CardNotFoundException("Card not found"));
        card.setNumber(dto.number());
        card.setExpirationDate(dto.expirationDate());
        card.setHolder(dto.holder());
        return cardMapper.toResponseDto(cardRepository.save(card));
    }

    @Transactional
    @CacheEvict(key = "#id")
    @Override
    public void activate(UUID id) {
        cardRepository.activate(id);
    }

    @Transactional
    @CacheEvict(key = "#id")
    @Override
    public void deactivate(UUID id) {
        cardRepository.deactivate(id);
    }

    @Transactional
    @CacheEvict(key = "#id")
    @Override
    public void delete(UUID id) {
        cardRepository.deleteById(id);
    }
}
