package apap.ti._5.tour_package_2306165540_be.config;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final ActivityRepository activityRepository;
    private final PackageRepository packageRepository;
    private final PlanRepository planRepository;
    private final OrderedQuantityRepository orderedQuantityRepository;

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists
        if (activityRepository.count() > 0) {
            System.out.println("Database already has data. Skipping initialization.");
            return;
        }

        System.out.println("Initializing database with fake data...");

        // Create Activities
        List<Activity> activities = createActivities();
        activityRepository.saveAll(activities);

        // Create Packages
        List<Package> packages = createPackages();
        packageRepository.saveAll(packages);

        // Create Plans
        List<Plan> plans = createPlans(packages);
        planRepository.saveAll(plans);

        // Create Ordered Quantities
        List<OrderedQuantity> orderedQuantities = createOrderedQuantities(plans, activities);
        orderedQuantityRepository.saveAll(orderedQuantities);

        System.out.println("Database initialization completed!");
        System.out.println("Created " + activities.size() + " activities");
        System.out.println("Created " + packages.size() + " packages");
        System.out.println("Created " + plans.size() + " plans");
        System.out.println("Created " + orderedQuantities.size() + " ordered quantities");
    }

    private List<Activity> createActivities() {
        LocalDateTime now = LocalDateTime.now();

        Activity activity1 = new Activity();
        activity1.setId("ACT-001");
        activity1.setActivityName("Snorkeling Adventure");
        activity1.setActivityItem("Snorkeling Equipment, Life Jacket");
        activity1.setCapacity(30);
        activity1.setPrice(500000L);
        activity1.setActivityType("Water Sport");
        activity1.setStartDate(now.plusDays(5));
        activity1.setEndDate(now.plusDays(5).plusHours(4));
        activity1.setStartLocation("Nusa Dua Beach");
        activity1.setEndLocation("Nusa Dua Beach");

        Activity activity2 = new Activity();
        activity2.setId("ACT-002");
        activity2.setActivityName("Temple Tour");
        activity2.setActivityItem("Sarong, Tour Guide");
        activity2.setCapacity(50);
        activity2.setPrice(350000L);
        activity2.setActivityType("Cultural");
        activity2.setStartDate(now.plusDays(3));
        activity2.setEndDate(now.plusDays(3).plusHours(6));
        activity2.setStartLocation("Ubud Center");
        activity2.setEndLocation("Ubud Center");

        Activity activity3 = new Activity();
        activity3.setId("ACT-003");
        activity3.setActivityName("Mountain Hiking");
        activity3.setActivityItem("Hiking Boots, Backpack, Guide");
        activity3.setCapacity(20);
        activity3.setPrice(750000L);
        activity3.setActivityType("Adventure");
        activity3.setStartDate(now.plusDays(7));
        activity3.setEndDate(now.plusDays(7).plusHours(8));
        activity3.setStartLocation("Mount Batur Base Camp");
        activity3.setEndLocation("Mount Batur Summit");

        Activity activity4 = new Activity();
        activity4.setId("ACT-004");
        activity4.setActivityName("Cooking Class");
        activity4.setActivityItem("Ingredients, Apron, Recipe Book");
        activity4.setCapacity(15);
        activity4.setPrice(450000L);
        activity4.setActivityType("Culinary");
        activity4.setStartDate(now.plusDays(4));
        activity4.setEndDate(now.plusDays(4).plusHours(3));
        activity4.setStartLocation("Ubud Cooking School");
        activity4.setEndLocation("Ubud Cooking School");

        Activity activity5 = new Activity();
        activity5.setId("ACT-005");
        activity5.setActivityName("Surfing Lesson");
        activity5.setActivityItem("Surfboard, Wetsuit, Instructor");
        activity5.setCapacity(25);
        activity5.setPrice(600000L);
        activity5.setActivityType("Water Sport");
        activity5.setStartDate(now.plusDays(6));
        activity5.setEndDate(now.plusDays(6).plusHours(3));
        activity5.setStartLocation("Kuta Beach");
        activity5.setEndLocation("Kuta Beach");

        Activity activity6 = new Activity();
        activity6.setId("ACT-006");
        activity6.setActivityName("Spa & Massage");
        activity6.setActivityItem("Essential Oils, Towels");
        activity6.setCapacity(10);
        activity6.setPrice(400000L);
        activity6.setActivityType("Wellness");
        activity6.setStartDate(now.plusDays(2));
        activity6.setEndDate(now.plusDays(2).plusHours(2));
        activity6.setStartLocation("Seminyak Spa Center");
        activity6.setEndLocation("Seminyak Spa Center");

        return Arrays.asList(activity1, activity2, activity3, activity4, activity5, activity6);
    }

    private List<Package> createPackages() {
        LocalDateTime now = LocalDateTime.now();

        Package package1 = new Package();
        package1.setId("PKG-001");
        package1.setUserId("USER-001");
        package1.setPackageName("Bali Adventure Package");
        package1.setQuota(20);
        package1.setPrice(2500000L);
        package1.setStatus("ACTIVE");
        package1.setStartDate(now.plusDays(3));
        package1.setEndDate(now.plusDays(7));

        Package package2 = new Package();
        package2.setId("PKG-002");
        package2.setUserId("USER-002");
        package2.setPackageName("Cultural Experience");
        package2.setQuota(15);
        package2.setPrice(1800000L);
        package2.setStatus("ACTIVE");
        package2.setStartDate(now.plusDays(2));
        package2.setEndDate(now.plusDays(5));

        Package package3 = new Package();
        package3.setId("PKG-003");
        package3.setUserId("USER-003");
        package3.setPackageName("Water Sports Extravaganza");
        package3.setQuota(30);
        package3.setPrice(2200000L);
        package3.setStatus("ACTIVE");
        package3.setStartDate(now.plusDays(5));
        package3.setEndDate(now.plusDays(8));

        Package package4 = new Package();
        package4.setId("PKG-004");
        package4.setUserId("USER-001");
        package4.setPackageName("Relaxation Retreat");
        package4.setQuota(12);
        package4.setPrice(1500000L);
        package4.setStatus("PENDING");
        package4.setStartDate(now.plusDays(10));
        package4.setEndDate(now.plusDays(12));

        return Arrays.asList(package1, package2, package3, package4);
    }

    private List<Plan> createPlans(List<Package> packages) {
        LocalDateTime now = LocalDateTime.now();
        List<Plan> plans = new ArrayList<>();

        // Plans for Package 1 - Bali Adventure Package
        Plan plan1 = new Plan();
        plan1.setId(UUID.randomUUID());
        plan1.setPackageEntity(packages.get(0));
        plan1.setPrice(750000L);
        plan1.setActivityType("Adventure");
        plan1.setStatus("CONFIRMED");
        plan1.setStartDate(now.plusDays(7));
        plan1.setEndDate(now.plusDays(7).plusHours(8));
        plan1.setStartLocation("Mount Batur Base Camp");
        plan1.setEndLocation("Mount Batur Summit");
        plans.add(plan1);

        Plan plan2 = new Plan();
        plan2.setId(UUID.randomUUID());
        plan2.setPackageEntity(packages.get(0));
        plan2.setPrice(500000L);
        plan2.setActivityType("Water Sport");
        plan2.setStatus("CONFIRMED");
        plan2.setStartDate(now.plusDays(5));
        plan2.setEndDate(now.plusDays(5).plusHours(4));
        plan2.setStartLocation("Nusa Dua Beach");
        plan2.setEndLocation("Nusa Dua Beach");
        plans.add(plan2);

        // Plans for Package 2 - Cultural Experience
        Plan plan3 = new Plan();
        plan3.setId(UUID.randomUUID());
        plan3.setPackageEntity(packages.get(1));
        plan3.setPrice(350000L);
        plan3.setActivityType("Cultural");
        plan3.setStatus("CONFIRMED");
        plan3.setStartDate(now.plusDays(3));
        plan3.setEndDate(now.plusDays(3).plusHours(6));
        plan3.setStartLocation("Ubud Center");
        plan3.setEndLocation("Ubud Center");
        plans.add(plan3);

        Plan plan4 = new Plan();
        plan4.setId(UUID.randomUUID());
        plan4.setPackageEntity(packages.get(1));
        plan4.setPrice(450000L);
        plan4.setActivityType("Culinary");
        plan4.setStatus("CONFIRMED");
        plan4.setStartDate(now.plusDays(4));
        plan4.setEndDate(now.plusDays(4).plusHours(3));
        plan4.setStartLocation("Ubud Cooking School");
        plan4.setEndLocation("Ubud Cooking School");
        plans.add(plan4);

        // Plans for Package 3 - Water Sports Extravaganza
        Plan plan5 = new Plan();
        plan5.setId(UUID.randomUUID());
        plan5.setPackageEntity(packages.get(2));
        plan5.setPrice(600000L);
        plan5.setActivityType("Water Sport");
        plan5.setStatus("CONFIRMED");
        plan5.setStartDate(now.plusDays(6));
        plan5.setEndDate(now.plusDays(6).plusHours(3));
        plan5.setStartLocation("Kuta Beach");
        plan5.setEndLocation("Kuta Beach");
        plans.add(plan5);

        Plan plan6 = new Plan();
        plan6.setId(UUID.randomUUID());
        plan6.setPackageEntity(packages.get(2));
        plan6.setPrice(500000L);
        plan6.setActivityType("Water Sport");
        plan6.setStatus("CONFIRMED");
        plan6.setStartDate(now.plusDays(5));
        plan6.setEndDate(now.plusDays(5).plusHours(4));
        plan6.setStartLocation("Nusa Dua Beach");
        plan6.setEndLocation("Nusa Dua Beach");
        plans.add(plan6);

        // Plans for Package 4 - Relaxation Retreat
        Plan plan7 = new Plan();
        plan7.setId(UUID.randomUUID());
        plan7.setPackageEntity(packages.get(3));
        plan7.setPrice(400000L);
        plan7.setActivityType("Wellness");
        plan7.setStatus("PENDING");
        plan7.setStartDate(now.plusDays(10));
        plan7.setEndDate(now.plusDays(10).plusHours(2));
        plan7.setStartLocation("Seminyak Spa Center");
        plan7.setEndLocation("Seminyak Spa Center");
        plans.add(plan7);

        return plans;
    }

    private List<OrderedQuantity> createOrderedQuantities(List<Plan> plans, List<Activity> activities) {
        LocalDateTime now = LocalDateTime.now();
        List<OrderedQuantity> orderedQuantities = new ArrayList<>();

        // Ordered quantities for different plans
        OrderedQuantity oq1 = new OrderedQuantity();
        oq1.setId(UUID.randomUUID());
        oq1.setPlan(plans.get(0));
        oq1.setActivity(activities.get(2)); // Mountain Hiking
        oq1.setOrderedQuota(15);
        oq1.setQuota(20);
        oq1.setPrice(750000L);
        oq1.setStartDate(now.plusDays(7));
        oq1.setEndDate(now.plusDays(7).plusHours(8));
        orderedQuantities.add(oq1);

        OrderedQuantity oq2 = new OrderedQuantity();
        oq2.setId(UUID.randomUUID());
        oq2.setPlan(plans.get(1));
        oq2.setActivity(activities.get(0)); // Snorkeling
        oq2.setOrderedQuota(20);
        oq2.setQuota(30);
        oq2.setPrice(500000L);
        oq2.setStartDate(now.plusDays(5));
        oq2.setEndDate(now.plusDays(5).plusHours(4));
        orderedQuantities.add(oq2);

        OrderedQuantity oq3 = new OrderedQuantity();
        oq3.setId(UUID.randomUUID());
        oq3.setPlan(plans.get(2));
        oq3.setActivity(activities.get(1)); // Temple Tour
        oq3.setOrderedQuota(15);
        oq3.setQuota(50);
        oq3.setPrice(350000L);
        oq3.setStartDate(now.plusDays(3));
        oq3.setEndDate(now.plusDays(3).plusHours(6));
        orderedQuantities.add(oq3);

        OrderedQuantity oq4 = new OrderedQuantity();
        oq4.setId(UUID.randomUUID());
        oq4.setPlan(plans.get(3));
        oq4.setActivity(activities.get(3)); // Cooking Class
        oq4.setOrderedQuota(12);
        oq4.setQuota(15);
        oq4.setPrice(450000L);
        oq4.setStartDate(now.plusDays(4));
        oq4.setEndDate(now.plusDays(4).plusHours(3));
        orderedQuantities.add(oq4);

        OrderedQuantity oq5 = new OrderedQuantity();
        oq5.setId(UUID.randomUUID());
        oq5.setPlan(plans.get(4));
        oq5.setActivity(activities.get(4)); // Surfing
        oq5.setOrderedQuota(25);
        oq5.setQuota(25);
        oq5.setPrice(600000L);
        oq5.setStartDate(now.plusDays(6));
        oq5.setEndDate(now.plusDays(6).plusHours(3));
        orderedQuantities.add(oq5);

        OrderedQuantity oq6 = new OrderedQuantity();
        oq6.setId(UUID.randomUUID());
        oq6.setPlan(plans.get(5));
        oq6.setActivity(activities.get(0)); // Snorkeling
        oq6.setOrderedQuota(30);
        oq6.setQuota(30);
        oq6.setPrice(500000L);
        oq6.setStartDate(now.plusDays(5));
        oq6.setEndDate(now.plusDays(5).plusHours(4));
        orderedQuantities.add(oq6);

        OrderedQuantity oq7 = new OrderedQuantity();
        oq7.setId(UUID.randomUUID());
        oq7.setPlan(plans.get(6));
        oq7.setActivity(activities.get(5)); // Spa & Massage
        oq7.setOrderedQuota(10);
        oq7.setQuota(10);
        oq7.setPrice(400000L);
        oq7.setStartDate(now.plusDays(10));
        oq7.setEndDate(now.plusDays(10).plusHours(2));
        orderedQuantities.add(oq7);

        return orderedQuantities;
    }
}
