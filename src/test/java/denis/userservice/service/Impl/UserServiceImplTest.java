package denis.userservice.service.Impl;

import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import denis.userservice.entity.User;
import denis.userservice.exception.UserNotFoundException;
import denis.userservice.mapper.UserMapper;
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
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private User user;
    private UserRequestDto requestDto;
    private UserResponseDto responseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setId(UUID.randomUUID());
        user.setName("John");
        user.setSurname("Doe");
        user.setBirthDate(LocalDate.of(1990, 1, 1));
        user.setEmail("john@example.com");

        requestDto = new UserRequestDto(UUID.randomUUID(),"John", "Doe", LocalDate.of(1990,1,1), "john@example.com");
        responseDto = new UserResponseDto(user.getId(), "John", "Doe", LocalDate.of(1990,1,1), "john@example.com", true, null);
    }

    @Test
    void testCreate() {
        when(userMapper.toEntity(requestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.create(requestDto);

        assertEquals(responseDto, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void testGetById_UserExists() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userMapper.toResponseDto(user)).thenReturn(responseDto);

        UserResponseDto result = userService.getById(user.getId());

        assertEquals(responseDto, result);
    }

    @Test
    void testGetById_UserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(id));
    }

    @Test
    void testGetAllUsers() {
        User user1 = new User();
        user1.setId(UUID.randomUUID());
        user1.setName("John");
        user1.setSurname("Doe");
        User user2 = new User();
        user2.setId(UUID.randomUUID());
        user2.setName("Jane");
        user2.setSurname("Smith");
        List<User> usersList = List.of(user1, user2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(usersList, pageable, usersList.size());

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(userMapper.toResponseDto(user1)).thenReturn(new UserResponseDto(user1.getId(), "John", "Doe", null, null, true, null));
        when(userMapper.toResponseDto(user2)).thenReturn(new UserResponseDto(user2.getId(), "Jane", "Smith", null, null, true, null));

        Page<UserResponseDto> result = userService.getAll(null, null, pageable);
        assertEquals(2, result.getContent().size());
        assertEquals("John", result.getContent().get(0).name());
        assertEquals("Jane", result.getContent().get(1).name());
    }


    @Test
    void testUpdate_UserExist() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponseDto(any(User.class))).thenReturn(responseDto);
        UserResponseDto result = userService.update(user.getId(), requestDto);
        assertEquals(responseDto, result);
    }

    @Test
    void testUpdate_UserNotFound() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getById(id));
    }


    @Test
    void testActivateAndDeactivate() {
        doNothing().when(userRepository).activate(user.getId());
        doNothing().when(userRepository).deactivate(user.getId());

        userService.activate(user.getId());
        userService.deactivate(user.getId());

        verify(userRepository, times(1)).activate(user.getId());
        verify(userRepository, times(1)).deactivate(user.getId());
    }

    @Test
    void testDelete() {
        doNothing().when(userRepository).deleteById(user.getId());
        userService.delete(user.getId());
        verify(userRepository, times(1)).deleteById(user.getId());
    }
}
