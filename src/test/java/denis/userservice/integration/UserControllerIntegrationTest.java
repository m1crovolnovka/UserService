package denis.userservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import denis.userservice.dto.request.UserRequestDto;
import denis.userservice.dto.response.UserResponseDto;
import denis.userservice.entity.User;
import denis.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.1")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379).toString());
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    private User buildUserEntity(String name, String email, boolean active) {
        return User.builder()
                .name(name)
                .surname("EntitySurname")
                .email(email)
                .active(active)
                .birthDate(LocalDate.of(2000,12,26))
                .build();
    }


    @Test
    void createUser_shouldReturnCreatedUserAnd201() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("John", "Garik",LocalDate.of(2000,12,26),"john.doe@example.com");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.name").value("John"))
                .andReturn();

        UserResponseDto response = objectMapper.readValue(result.getResponse().getContentAsString(), UserResponseDto.class);

        assertThat(userRepository.findById(response.id())).isPresent();
    }

    @Test
    void createUser_shouldReturn400OnValidationFailure() throws Exception {
        UserRequestDto invalidDto = new UserRequestDto("J","Doe",LocalDate.of(2000,12,26),"validemail.com");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_shouldReturnUserAnd200() throws Exception {
        User savedUser = userRepository.save(buildUserEntity("Jane", "jane.doe@example.com", true));

        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedUser.getId().toString()))
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    void getUserById_shouldReturn404ForNonExistingUser() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/api/users/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_shouldUpdateUserAndReturn200() throws Exception {
        User savedUser = userRepository.save(buildUserEntity("OldName", "old@example.com", true));
        UserRequestDto updateDto = new UserRequestDto("NewName", "NewSurname",LocalDate.of(2000,12,26),"new@example.com");
        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"))
                .andExpect(jsonPath("$.email").value("new@example.com"));

        User updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getName()).isEqualTo("NewName");
    }

    @Test
    void deactivate_shouldSetIsActiveToFalseAndReturn204() throws Exception {
        User savedUser = userRepository.save(buildUserEntity("Active", "active@example.com", true));

        mockMvc.perform(patch("/api/users/{id}/deactivate", savedUser.getId()))
                .andExpect(status().isNoContent());

        User deactivatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(deactivatedUser.getActive()).isFalse();
    }

    @Test
    void activate_shouldSetIsActiveToTrueAndReturn204() throws Exception {
        User savedUser = userRepository.save(buildUserEntity("Inactive", "inactive@example.com", false));

        mockMvc.perform(patch("/api/users/{id}/activate", savedUser.getId()))
                .andExpect(status().isNoContent());

        User activatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(activatedUser.getActive()).isTrue();
    }

    @Test
    void deleteUser_shouldRemoveUserAndReturn204() throws Exception {
        User userToDelete = userRepository.save(buildUserEntity("DeleteMe", "delete@example.com", true));

        mockMvc.perform(delete("/api/users/{id}", userToDelete.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(userToDelete.getId())).isNotPresent();
    }


    @Test
    void getAllUsers_shouldReturnFilteredAndPagedUsers() throws Exception {
        userRepository.save(buildUserEntity("Alice", "a@e.com", true));
        userRepository.save(buildUserEntity("Bob", "b@e.com", true));
        userRepository.save(buildUserEntity("Andrew", "c@e.com", true));
        userRepository.save(buildUserEntity("Charlie", "d@e.com", true));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc.perform(get("/api/users")
                        .param("firstName", "A")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }

}