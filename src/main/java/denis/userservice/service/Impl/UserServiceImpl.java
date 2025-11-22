package denis.userservice.service.Impl;

import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import denis.userservice.entity.User;
import denis.userservice.exception.EntityAlreadyExistsException;
import denis.userservice.exception.UserNotFoundException;
import denis.userservice.mapper.UserMapper;
import denis.userservice.repository.UserRepository;
import denis.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public String getCurrentUserId() {
        return (String) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    @Transactional
    @CachePut(key="#result.id")
    @Override
    public UserResponseDto create(UserRequestDto dto) {
        if (userRepository.existsById(dto.userId())) {
            throw new EntityAlreadyExistsException("User with ID " + dto.userId() + " already exists");
        }
        User entity = new User();
        entity.setId(dto.userId());
        entity.setName(dto.name());
        entity.setEmail(dto.email());
        entity.setBirthDate(dto.birthDate());
        entity.setSurname(dto.surname());
        entity.setActive(true);
        User saved = userRepository.save(entity);
        return userMapper.toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key="#id")
    @Override
    public UserResponseDto getById(UUID id) {
        return userMapper.toResponseDto(userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found")));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<UserResponseDto> getAll(String name, String surname, Pageable pageable) {
        Specification<User> spec = Specification.unrestricted();
        spec = userSpecifications(spec,name, "name");
        spec = userSpecifications(spec,surname, "surname");
        Page<User> users = userRepository.findAll(spec,pageable);
        return new PageImpl<>(
                users.get().map(userMapper::toResponseDto).toList(),
                pageable, users.getTotalElements());
    }

     private Specification<User> userSpecifications(Specification<User> spec, String field, String fieldName) {
         if (field != null && !field.isBlank()) {
             return spec.and((root, query, criteriaBuilder) ->
                 criteriaBuilder.like(criteriaBuilder.lower(root.get(fieldName)),
                         "%" + field.toLowerCase() + "%"));
         }
         return spec;
     }

    @Transactional
    @CachePut(key="#id")
    @Override
    public UserResponseDto update(UUID id, UserRequestDto dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setName(dto.name());
        user.setSurname(dto.surname());
        user.setEmail(dto.email());
        user.setBirthDate(dto.birthDate());
        User updated = userRepository.save(user);
        return userMapper.toResponseDto(updated);
    }

    @Transactional
    @CacheEvict(key = "#id")
    @Override
    public void activate(UUID id) {
        userRepository.activate(id);
    }

    @Transactional
    @CacheEvict(key = "#id")
    @Override
    public void deactivate(UUID id) {
        userRepository.deactivate(id);
    }

    @Transactional
    @CacheEvict(key = "#id")
    @Override
    public void delete(UUID id) {
        userRepository.deleteById(id);
    }
}
