package apap.ti._5.tour_package_2306165540_be.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ModelEntityTest {

    @Test
    void activityDefaultsToNotDeleted() {
        Activity activity = new Activity();
        assertThat(activity.getIsDeleted()).isFalse();
        assertThat(activity.getOrderedQuantities()).isNotNull();
    }

    @Test
    void planMaintainsOrderedQuantitiesList() {
        Plan plan = new Plan();
        assertThat(plan.getOrderedQuantities()).isNotNull();
        plan.getOrderedQuantities().add(new OrderedQuantity());
        assertThat(plan.getOrderedQuantities()).hasSize(1);
    }

    @Test
    void packageMaintainsPlansList() {
        Package pkg = new Package();
        assertThat(pkg.getPlans()).isNotNull();
        pkg.getPlans().add(new Plan());
        assertThat(pkg.getPlans()).hasSize(1);
    }

    @Test
    void orderedQuantityStoresActivityOrPlan() {
        OrderedQuantity quantity = new OrderedQuantity();
        quantity.setId(UUID.randomUUID());
        quantity.setActivity(new Activity());
        quantity.setActivityPlan(new Plan());
        assertThat(quantity.getActivity()).isNotNull();
        assertThat(quantity.getActivityPlan()).isNotNull();
    }

    @Test
    void topUpTransactionAssignsIdAndTimestampOnPersist() {
        TopUpTransaction transaction = new TopUpTransaction();
        transaction.onPersist();
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getTransactionDate()).isNotNull();
    }
}
