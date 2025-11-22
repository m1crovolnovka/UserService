package denis.userservice.mapper;

import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import denis.userservice.entity.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = PaymentCardMapper.class)
public interface UserMapper {
    User toEntity(UserRequestDto dto);
    UserResponseDto toResponseDto(User user);
}
