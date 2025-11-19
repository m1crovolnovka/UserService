package denis.userservice.integration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import denis.userservice.dto.request.PaymentCardRequestDto;
import denis.userservice.dto.response.PaymentCardResponseDto;
import denis.userservice.entity.PaymentCard;
import denis.userservice.entity.User;
import denis.userservice.repository.PaymentCardRepository;
import denis.userservice.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
public class PaymentCardControllerIntegrationTest {

    public abstract static class PageMixIn<T> extends PageImpl<T> {
        @JsonCreator
        public PageMixIn(@JsonProperty("content") List<T> content,
                         @JsonProperty("pageable") Pageable pageable,
                         @JsonProperty("totalElements") long totalElements) {
            super(content, pageable, totalElements);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentCardRepository cardRepository;

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
        objectMapper.addMixIn(PageImpl.class, PageMixIn.class);
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        cardRepository.deleteAll();
        userRepository.deleteAll();
    }

    private User buildAndSaveUser(String email) {
        return userRepository.save(User.builder()
                .name("CardUser")
                .surname("TestSurname")
                .email(email)
                .birthDate(LocalDate.of(1990, 1, 1))
                .active(true)
                .build());
    }

    private PaymentCardRequestDto buildCardRequest(UUID userId, String number) {
        return new PaymentCardRequestDto(
                userId,
                number,
                "TEST HOLDER",
                LocalDate.now().plusYears(2)
        );
    }

    private PaymentCard buildAndSaveCard(User user, String number) {
        return cardRepository.save(PaymentCard.builder()
                .user(user)
                .number(number)
                .holder("SAVED HOLDER")
                .expirationDate(LocalDate.now().plusYears(1))
                .active(true)
                .build());
    }


    @Test
    void createCard_shouldReturnCreatedCardAnd201() throws Exception {
        User user = buildAndSaveUser("create@test.com");
        PaymentCardRequestDto requestDto = buildCardRequest(user.getId(), "1111222233334444");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isString())
                .andExpect(jsonPath("$.userId").value(user.getId().toString()))
                .andExpect(jsonPath("$.number").value("1111222233334444"));
    }

    @Test
    void createCard_shouldReturn404IfUserNotFound() throws Exception {
        UUID nonExistingUserId = UUID.randomUUID();
        PaymentCardRequestDto requestDto = buildCardRequest(nonExistingUserId, "1111222233334444");

        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createCard_shouldReturn400OnValidationFailure() throws Exception {
        User user = buildAndSaveUser("invalid@test.com");

        PaymentCardRequestDto invalidNumberDto = buildCardRequest(user.getId(), "12345");
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidNumberDto)))
                .andExpect(status().isBadRequest());

        PaymentCardRequestDto invalidDateDto = new PaymentCardRequestDto(
                user.getId(),
                "9999888877776666",
                "OLD CARD",
                LocalDate.now().minusDays(1)
        );
        mockMvc.perform(post("/api/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDateDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardById_shouldReturnCardAnd200() throws Exception {
        User user = buildAndSaveUser("getbyid@test.com");
        PaymentCard savedCard = buildAndSaveCard(user, "5555666677778888");

        mockMvc.perform(get("/api/cards/{id}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId().toString()))
                .andExpect(jsonPath("$.number").value("5555666677778888"));
    }

    @Test
    void getCardById_shouldReturn404ForNonExistingCard() throws Exception {
        UUID nonExistingId = UUID.randomUUID();
        mockMvc.perform(get("/api/cards/{id}", nonExistingId))
                .andExpect(status().isNotFound());
    }


    @Test
    void getAllCards_shouldReturnPagedCards() throws Exception {
        User userA = buildAndSaveUser("userA@test.com");
        User userB = buildAndSaveUser("userB@test.com");
        buildAndSaveCard(userA, "1000000000000001");
        buildAndSaveCard(userA, "1000000000000002");
        buildAndSaveCard(userB, "1000000000000003");
        buildAndSaveCard(userB, "1000000000000004");
        buildAndSaveCard(userB, "1000000000000005");
        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }


    @Test
    void getCardsByUserId_shouldReturnList() throws Exception {
        User user = buildAndSaveUser("listuser@test.com");
        User otherUser = buildAndSaveUser("other@test.com");
        buildAndSaveCard(user, "1111111111110001");
        buildAndSaveCard(user, "1111111111110002");
        buildAndSaveCard(otherUser, "2222222222220003");
        MvcResult result = mockMvc.perform(get("/api/cards/user-cards/{userId}", user.getId()))
                .andExpect(status().isOk())
                .andReturn();
        String jsonResponse = result.getResponse().getContentAsString();
        List<PaymentCardResponseDto> userCards = objectMapper.readValue(jsonResponse,
                new TypeReference<List<PaymentCardResponseDto>>() {});
        assertThat(userCards).hasSize(2);
        assertThat(userCards.stream().allMatch(card -> card.userId().equals(user.getId()))).isTrue();
    }


    @Test
    void updateCard_shouldUpdateCardAndReturn200() throws Exception {
        User user = buildAndSaveUser("update@test.com");
        PaymentCard savedCard = buildAndSaveCard(user, "9999999999999999");
        PaymentCardRequestDto updateDto = buildCardRequest(user.getId(), "1111111111111111");
        mockMvc.perform(put("/api/cards/{id}", savedCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(updateDto.number()))
                .andExpect(jsonPath("$.holder").value(updateDto.holder()));
        PaymentCard updatedCard = cardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(updatedCard.getNumber()).isEqualTo(updateDto.number());
        assertThat(updatedCard.getHolder()).isEqualTo(updateDto.holder());
    }


    @Test
    void deactivateCard_shouldSetIsActiveToFalseAndReturn204() throws Exception {
        User user = buildAndSaveUser("deact@test.com");
        PaymentCard savedCard = buildAndSaveCard(user, "1234567890123456");
        savedCard.setActive(true);
        cardRepository.save(savedCard);
        mockMvc.perform(patch("/api/cards/{id}/deactivate", savedCard.getId()))
                .andExpect(status().isNoContent());
        PaymentCard deactivatedCard = cardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(deactivatedCard.getActive()).isFalse();
    }

    @Test
    void activateCard_shouldSetIsActiveToTrueAndReturn204() throws Exception {
        User user = buildAndSaveUser("act@test.com");
        PaymentCard savedCard = buildAndSaveCard(user, "6543210987654321");
        savedCard.setActive(false);
        cardRepository.save(savedCard);
        mockMvc.perform(patch("/api/cards/{id}/activate", savedCard.getId()))
                .andExpect(status().isNoContent());
        PaymentCard activatedCard = cardRepository.findById(savedCard.getId()).orElseThrow();
        assertThat(activatedCard.getActive()).isTrue();
    }


    @Test
    void deleteCard_shouldRemoveCardAndReturn204() throws Exception {
        User user = buildAndSaveUser("delete@test.com");
        PaymentCard cardToDelete = buildAndSaveCard(user, "DELETEMECARD");
        mockMvc.perform(delete("/api/cards/{id}", cardToDelete.getId()))
                .andExpect(status().isNoContent());
        assertThat(cardRepository.findById(cardToDelete.getId())).isNotPresent();
    }
}