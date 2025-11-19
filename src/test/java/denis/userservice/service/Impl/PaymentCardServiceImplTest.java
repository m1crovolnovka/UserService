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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class PaymentCardServiceImplTest {

    @InjectMocks
    private PaymentCardServiceImpl cardService;

    @Mock
    private PaymentCardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentCardMapper cardMapper;

    private User user;
    private PaymentCard card;
    private PaymentCardRequestDto requestDto;
    private PaymentCardResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John");
        user.setPaymentCards(new ArrayList<>());
        user.setActive(true);

        card = new PaymentCard();
        card.setId(UUID.randomUUID());
        card.setUser(user);
        card.setNumber("1234 5678 9012 3456");
        card.setHolder("John Doe");
        card.setExpirationDate(LocalDate.of(2030, 12, 31));

        requestDto = new PaymentCardRequestDto(user.getId(), "1234 5678 9012 3456", "John Doe", LocalDate.of(2030, 12, 31));
        responseDto = new PaymentCardResponseDto(card.getId(), user.getId(), card.getNumber(), card.getHolder(), card.getExpirationDate(),true);
    }

    @Test
    void testCreate_Success() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(cardMapper.toEntity(requestDto)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.create(requestDto);

        assertEquals(responseDto, result);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testCreate_UserNotFound() {
        when(cardMapper.toEntity(requestDto)).thenReturn(card);
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardService.create(requestDto));
    }

    @Test
    void testCreate_CardListFull() {
        for (int i = 0; i < 5; i++) {
            user.getPaymentCards().add(new PaymentCard());
        }
        when(cardMapper.toEntity(requestDto)).thenReturn(card);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        assertThrows(CardListFullException.class, () -> cardService.create(requestDto));
    }

    @Test
    void testGetById_Success() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardMapper.toResponseDto(card)).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.getById(card.getId());

        assertEquals(responseDto, result);
    }

    @Test
    void testGetById_NotFound() {
        UUID id = UUID.randomUUID();
        when(cardRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> cardService.getById(id));
    }

    @Test
    void testGetAllPaymentCards() {
        PaymentCard card1 = new PaymentCard();
        card1.setId(UUID.randomUUID());
        card1.setNumber("1111 2222 3333 4444");

        PaymentCard card2 = new PaymentCard();
        card2.setId(UUID.randomUUID());
        card2.setNumber("5555 6666 7777 8888");

        List<PaymentCard> cards = List.of(card1, card2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<PaymentCard> page = new PageImpl<>(cards, pageable, cards.size());

        when(cardRepository.findAll(pageable)).thenReturn(page);
        when(cardMapper.toResponseDto(card1)).thenReturn(new PaymentCardResponseDto(card1.getId(), user.getId(), card1.getNumber(), null, null, true));
        when(cardMapper.toResponseDto(card2)).thenReturn(new PaymentCardResponseDto(card2.getId(),user.getId(), card2.getNumber(), null, null, true));

        Page<PaymentCardResponseDto> result = cardService.getAll(pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("1111 2222 3333 4444", result.getContent().get(0).number());
        assertEquals("5555 6666 7777 8888", result.getContent().get(1).number());
    }

    @Test
    void testGetByUserId() {
        UUID userId = UUID.randomUUID();

        PaymentCard card1 = new PaymentCard();
        card1.setId(UUID.randomUUID());
        card1.setNumber("1111 2222 3333 4444");

        PaymentCard card2 = new PaymentCard();
        card2.setId(UUID.randomUUID());
        card2.setNumber("5555 6666 7777 8888");

        List<PaymentCard> cards = List.of(card1, card2);

        when(cardRepository.findAllCardsByUserId(userId)).thenReturn(cards);
        when(cardMapper.toResponseDto(card1)).thenReturn(new PaymentCardResponseDto(card1.getId(),userId, card1.getNumber(), null, null, true));
        when(cardMapper.toResponseDto(card2)).thenReturn(new PaymentCardResponseDto(card2.getId(),userId, card2.getNumber(), null, null, true));

        List<PaymentCardResponseDto> result = cardService.getByUserId(userId);

        assertEquals(2, result.size());
        assertEquals("1111 2222 3333 4444", result.getFirst().number());
        assertEquals(userId, result.getFirst().userId());
    }

    @Test
    void testUpdate_Success() {
        when(cardRepository.findById(card.getId())).thenReturn(Optional.of(card));
        when(cardRepository.save(any(PaymentCard.class))).thenReturn(card);
        when(cardMapper.toResponseDto(any(PaymentCard.class))).thenReturn(responseDto);

        PaymentCardResponseDto result = cardService.update(card.getId(), requestDto);

        assertEquals(responseDto, result);
        verify(cardRepository, times(1)).save(any(PaymentCard.class));
    }

    @Test
    void testActivateAndDeactivate() {
        doNothing().when(cardRepository).activate(card.getId());
        doNothing().when(cardRepository).deactivate(card.getId());

        cardService.activate(card.getId());
        cardService.deactivate(card.getId());

        verify(cardRepository, times(1)).activate(card.getId());
        verify(cardRepository, times(1)).deactivate(card.getId());
    }

    @Test
    void testDelete() {
        doNothing().when(cardRepository).deleteById(card.getId());

        cardService.delete(card.getId());

        verify(cardRepository, times(1)).deleteById(card.getId());
    }

}
