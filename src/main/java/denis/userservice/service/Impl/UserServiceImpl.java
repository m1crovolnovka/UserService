package denis.userservice.service.Impl;

import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import denis.userservice.entity.User;
import denis.userservice.exception.UserNotFoundException;
import denis.userservice.mapper.UserMapper;
import denis.userservice.repository.UserRepository;
import denis.userservice.service.UserService;
import denis.userservice.specification.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto create(UserRequestDto dto) {
        User entity = userMapper.toEntity(dto);
        User saved = userRepository.save(entity);
        return userMapper.toResponseDto(saved);
    }

    @Override
    public UserResponseDto getById(UUID id) {
        return userMapper.toResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Override
    public Page<UserResponseDto> getAll(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification.unrestricted();
        spec = userSpecifications(spec,name);
        spec = userSpecifications(spec,surname);
        Page<User> users = userRepository.findAll(spec,pageable);
        return new PageImpl<>(
                users.get().map(userMapper::toResponseDto).toList(),
                pageable, users.getTotalElements());
    }

     private Specification<User> userSpecifications(Specification<User> spec, String field) {
         if (field != null && !field.isBlank()) {
             return spec.and(UserSpecifications.nameLike(field));
         }
         return spec;
     }

    @Transactional
    @Override
    public UserResponseDto update(UUID id, UserRequestDto dto) {
        User user = userMapper.toEntity(dto);
        user.setId(id);
        User updated = userRepository.save(user);
        return userMapper.toResponseDto(updated);
    }

    @Transactional
    @Override
    public void activate(UUID id) {
        userRepository.activate(id);
    }

    @Transactional
    @Override
    public void deactivate(UUID id) {
        userRepository.deactivate(id);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
