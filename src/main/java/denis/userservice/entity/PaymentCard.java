package denis.userservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "payment_cards")
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class PaymentCard extends Auditable{
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private String number;
    private String holder;
    private LocalDate expirationDate;
    private Boolean active;

}
