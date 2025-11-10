package denis.userservice.mapper;

import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import denis.userservice.entity.PaymentCard;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentCardMapper {
    @Mapping(source = "user.id", target = "userId")
    PaymentCardResponseDto toResponseDto(PaymentCard card);

    @Mapping(source = "userId", target = "user.id")
    PaymentCard toEntity(PaymentCardRequestDto dto);
}
