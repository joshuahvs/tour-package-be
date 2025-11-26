package apap.ti._5.tour_package_2306165540_be.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "top_up_transaction")
public class TopUpTransaction {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // SUCCESS, PENDING, FAILED

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "description", length = 255)
    private String description;

    @PrePersist
    protected void onPersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (transactionDate == null) {
            transactionDate = LocalDateTime.now();
        }
    }
}
