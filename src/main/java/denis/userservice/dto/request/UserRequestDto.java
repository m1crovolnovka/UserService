package denis.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;
import java.util.UUID;

public record UserRequestDto (
    @NotNull(message = "Id cannot be Null")
    UUID userId,

    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Surname cannot be blank")
    String surname,

    @Past(message = "Birth date must be in the past")
    LocalDate birthDate,

    @NotBlank
    @Email(message = "Email must be valid")
    String email
){}
