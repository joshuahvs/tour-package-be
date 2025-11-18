package apap.ti._5.tour_package_2306165540_be.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ordered_quantity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderedQuantity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", referencedColumnName = "id", nullable = false)
    private Plan plan;

    // Legacy relationship (when activity was an Activity entity). Now optional.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", referencedColumnName = "id", nullable = true)
    private Activity activity;

    // New relationship: activity can be another Plan selected as an activity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_plan_id", referencedColumnName = "id")
    private Plan activityPlan;

    @Column(name = "ordered_quota", nullable = false)
    private int orderedQuota;

    @Column(name = "quota", nullable = false)
    private int quota;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}