package denis.userservice.service.Impl;

import denis.userservice.dao.UserDao;
import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import denis.userservice.entity.User;
import denis.userservice.mapper.UserMapper;
import denis.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final UserMapper userMapper;

    @Override
    public UserResponseDto create(UserRequestDto dto) {
        User entity = userMapper.toEntity(dto);
        User saved = userDao.create(entity);
        return userMapper.toResponseDto(saved);
    }

    @Override
    public UserResponseDto getById(UUID id) {
        return userMapper.toResponseDto(userDao.getById(id));
    }

    @Override
    public Page<UserResponseDto> getAll(String name, String surname, int page, int size) {
        Page<User> users = userDao.getAll(name, surname, page, size);
        return new PageImpl<>(
                users.get().map(userMapper::toResponseDto).toList(),
                PageRequest.of(page, size),
                users.getTotalElements()
        );
    }

    @Transactional
    @Override
    public UserResponseDto update(UUID id, UserRequestDto dto) {
        User updated = userDao.update(id, userMapper.toEntity(dto));
        return userMapper.toResponseDto(updated);
    }

    @Transactional
    @Override
    public void activate(UUID id) {
        userDao.activate(id);
    }

    @Transactional
    @Override
    public void deactivate(UUID id) {
        userDao.deactivate(id);
    }

    @Transactional
    @Override
    public void delete(UUID id) {
        userDao.delete(id);
    }
}
