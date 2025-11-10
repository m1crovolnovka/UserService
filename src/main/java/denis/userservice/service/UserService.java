package denis.userservice.service;

import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import org.springframework.data.domain.Page;
import java.util.UUID;


public interface  UserService {
    UserResponseDto create(UserRequestDto dto);
    UserResponseDto getById(UUID id);
    Page<UserResponseDto> getAll(String name, String surname, int page, int size);
    UserResponseDto update(UUID id, UserRequestDto dto);
    void activate(UUID id);
    void deactivate(UUID id);
    void delete(UUID id);
}
