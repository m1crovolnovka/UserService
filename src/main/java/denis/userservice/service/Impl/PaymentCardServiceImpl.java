package denis.userservice.service.Impl;

import denis.userservice.dao.PaymentCardDao;
import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import denis.userservice.entity.PaymentCard;
import denis.userservice.mapper.PaymentCardMapper;
import denis.userservice.service.PaymentCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentCardServiceImpl implements PaymentCardService {

    private final PaymentCardDao cardDao;
    private final PaymentCardMapper cardMapper;


    @Override
    public PaymentCardResponseDto create(PaymentCardRequestDto dto) {
        PaymentCard entity = cardMapper.toEntity(dto);
        PaymentCard saved = cardDao.create(dto.userId(), entity);
        return cardMapper.toResponseDto(saved);
    }

    @Override
    public PaymentCardResponseDto getById(UUID id) {
        return cardMapper.toResponseDto(cardDao.getById(id));
    }

    @Override
    public Page<PaymentCardResponseDto> getAll(int page, int size) {
        Page<PaymentCard> cards = cardDao.getAll(page, size);
        return new PageImpl<>(
                cards.get().map(cardMapper::toResponseDto).toList(),
                PageRequest.of(page, size),
                cards.getTotalElements()
        );
    }

    @Override
    public List<PaymentCardResponseDto> getByUserId(UUID userId) {
        return cardDao.getByUserId(userId).stream().map(cardMapper::toResponseDto).toList();
    }

    @Transactional
    @Override
    public PaymentCardResponseDto update(UUID id, PaymentCardRequestDto dto) {
        PaymentCard updated = cardDao.update(id,cardMapper.toEntity(dto));
        return cardMapper.toResponseDto(updated);
    }

    @Transactional
    @Override
    public void activate(UUID id) {
        cardDao.activate(id);
    }

    @Transactional
    @Override
    public void deactivate(UUID id) {
        cardDao.deactivate(id);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        cardDao.delete(id);
    }
}
