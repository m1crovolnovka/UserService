package denis.userservice.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;

import java.time.LocalDate;

public record UserRequestDto (
    @NotBlank(message = "Name cannot be blank")
    String name,

    @NotBlank(message = "Surname cannot be blank")
    String surname,

    @Past(message = "Birth date must be in the past")
    LocalDate birthDate,

    @Email(message = "Email must be valid")
    String email
){}
