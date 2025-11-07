package apap.ti._5.tour_package_2306165540_be.config;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Locale;

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
        List<Activity> activities = new ArrayList<>();

        // Keep original six activities to preserve references used by ordered
        // quantities
        Activity activity1 = new Activity();
        activity1.setId("ACT-001");
        activity1.setActivityName("Economy Flight Jakarta-Bali");
        activity1.setActivityItem("Flight Ticket");
        activity1.setCapacity(180);
        activity1.setPrice(800000L);
        activity1.setActivityType("Flight");
        activity1.setStartDate(now.plusDays(5));
        activity1.setEndDate(now.plusDays(5).plusHours(2));
        activity1.setStartLocation("Soekarno-Hatta Airport");
        activity1.setEndLocation("Ngurah Rai Airport");

        Activity activity2 = new Activity();
        activity2.setId("ACT-002");
        activity2.setActivityName("City Hotel Stay");
        activity2.setActivityItem("Room, Breakfast Included");
        activity2.setCapacity(50);
        activity2.setPrice(350000L);
        activity2.setActivityType("Accommodation");
        activity2.setStartDate(now.plusDays(3));
        activity2.setEndDate(now.plusDays(3).plusHours(24));
        activity2.setStartLocation("Downtown Hotel");
        activity2.setEndLocation("Downtown Hotel");

        Activity activity3 = new Activity();
        activity3.setId("ACT-003");
        activity3.setActivityName("SUV Rental");
        activity3.setActivityItem("SUV Vehicle, Insurance");
        activity3.setCapacity(20);
        activity3.setPrice(750000L);
        activity3.setActivityType("Vehicle Rental");
        activity3.setStartDate(now.plusDays(7));
        activity3.setEndDate(now.plusDays(7).plusHours(8));
        activity3.setStartLocation("Jakarta Downtown");
        activity3.setEndLocation("Jakarta Downtown");

        Activity activity4 = new Activity();
        activity4.setId("ACT-004");
        activity4.setActivityName("Resort Hotel Stay");
        activity4.setActivityItem("Room, Pool Access");
        activity4.setCapacity(15);
        activity4.setPrice(450000L);
        activity4.setActivityType("Accommodation");
        activity4.setStartDate(now.plusDays(4));
        activity4.setEndDate(now.plusDays(4).plusHours(24));
        activity4.setStartLocation("Ubud Resort");
        activity4.setEndLocation("Ubud Resort");

        Activity activity5 = new Activity();
        activity5.setId("ACT-005");
        activity5.setActivityName("Compact Car Rental");
        activity5.setActivityItem("Car, Insurance");
        activity5.setCapacity(25);
        activity5.setPrice(600000L);
        activity5.setActivityType("Vehicle Rental");
        activity5.setStartDate(now.plusDays(6));
        activity5.setEndDate(now.plusDays(6).plusHours(3));
        activity5.setStartLocation("Kuta City Center");
        activity5.setEndLocation("Kuta City Center");

        Activity activity6 = new Activity();
        activity6.setId("ACT-006");
        activity6.setActivityName("Business Flight Jakarta-Bali");
        activity6.setActivityItem("Flight Ticket");
        activity6.setCapacity(20);
        activity6.setPrice(1500000L);
        activity6.setActivityType("Flight");
        activity6.setStartDate(now.plusDays(2));
        activity6.setEndDate(now.plusDays(2).plusHours(2));
        activity6.setStartLocation("Soekarno-Hatta Airport");
        activity6.setEndLocation("Ngurah Rai Airport");

        activities.addAll(Arrays.asList(activity1, activity2, activity3, activity4, activity5, activity6));

        // Generate many more activities using JavaFaker
        activities.addAll(generateFakerActivities(6));

        return activities;
    }

    private List<Activity> generateFakerActivities(int existingCount) {
        // Start numbering after the existing fixed activities
        int nextIndex = existingCount + 1;
        final int perType = 50; // generate 50 for each type (total +150)
        List<Activity> list = new ArrayList<>();

        Faker faker = new Faker(Locale.forLanguageTag("id-ID"));
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        // Some Indonesian locations/provinces to keep things realistic
        List<String> provinces = Arrays.asList(
                "DKI Jakarta (Provinsi)", "Bali (Provinsi)", "Jawa Barat (Provinsi)",
                "Jawa Tengah (Provinsi)", "DI Yogyakarta (Provinsi)", "Jawa Timur (Provinsi)",
                "Sumatera Utara (Provinsi)", "Sumatera Barat (Provinsi)", "Aceh", "Riau",
                "Kalimantan Timur (Provinsi)", "Sulawesi Selatan (Provinsi)");

        // Flights
        for (int i = 0; i < perType; i++) {
            String origin = randomPickDifferent(provinces, null);
            String dest = randomPickDifferent(provinces, origin);
            LocalDateTime start = now.plusDays(randBetween(1, 60)).withHour(randBetween(5, 22))
                    .withMinute(randBetween(0, 59));
            LocalDateTime end = start.plusHours(randBetween(1, 3)).plusMinutes(randBetween(0, 45));

            Activity a = new Activity();
            a.setId(String.format("ACT-%03d", nextIndex++));
            a.setActivityName(String.format("%s Flight %s - %s",
                    faker.company().name(), cleanProvince(origin), cleanProvince(dest)));
            a.setActivityItem("Flight Ticket");
            a.setCapacity(randBetween(100, 250));
            a.setPrice((long) randBetween(500_000, 3_000_000));
            a.setActivityType("Flight");
            a.setStartDate(start);
            a.setEndDate(end);
            a.setStartLocation(origin);
            a.setEndLocation(dest);
            list.add(a);
        }

        // Accommodation
        for (int i = 0; i < perType; i++) {
            String city = randomPickDifferent(provinces, null);
            LocalDateTime start = now.plusDays(randBetween(1, 90)).withHour(12).withMinute(0);
            LocalDateTime end = start.plusDays(randBetween(1, 7)).withHour(11).withMinute(0);

            Activity a = new Activity();
            a.setId(String.format("ACT-%03d", nextIndex++));
            a.setActivityName(String.format("%s Hotel Stay - %s",
                    faker.company().name(), cleanProvince(city)));
            a.setActivityItem("Room, Breakfast Included");
            a.setCapacity(randBetween(10, 100));
            a.setPrice((long) randBetween(200_000, 2_000_000));
            a.setActivityType("Accommodation");
            a.setStartDate(start);
            a.setEndDate(end);
            a.setStartLocation(city);
            a.setEndLocation(city);
            list.add(a);
        }

        // Vehicle Rental
        for (int i = 0; i < perType; i++) {
            String city = randomPickDifferent(provinces, null);
            LocalDateTime start = now.plusDays(randBetween(1, 60)).withHour(randBetween(7, 20))
                    .withMinute(randBetween(0, 59));
            LocalDateTime end = start.plusHours(randBetween(2, 12));

            Activity a = new Activity();
            a.setId(String.format("ACT-%03d", nextIndex++));
            a.setActivityName(String.format("%s Car Rental",
                    faker.ancient().titan()));
            a.setActivityItem("Car, Insurance");
            a.setCapacity(randBetween(10, 50));
            a.setPrice((long) randBetween(300_000, 1_000_000));
            a.setActivityType("Vehicle Rental");
            a.setStartDate(start);
            a.setEndDate(end);
            a.setStartLocation(city);
            a.setEndLocation(city);
            list.add(a);
        }

        return list;
    }

    private int randBetween(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private String randomPickDifferent(List<String> list, String notEqual) {
        String pick;
        do {
            pick = list.get(ThreadLocalRandom.current().nextInt(list.size()));
        } while (notEqual != null && pick.equals(notEqual));
        return pick;
    }

    private String cleanProvince(String s) {
        // Shorten label e.g., "DKI Jakarta (Provinsi)" -> "DKI Jakarta"
        int idx = s.indexOf(" (");
        return idx > 0 ? s.substring(0, idx) : s;
    }

    private List<Package> createPackages() {
        LocalDateTime now = LocalDateTime.now();

        Package package1 = new Package();
        package1.setId("PKG-001");
        package1.setUserId("USER-001");
        package1.setPackageName("Bali Adventure Package");
        package1.setQuota(20);
        package1.setPrice(2500000L);
        package1.setStatus("PROCESSED");
        package1.setStartDate(now.plusDays(3));
        package1.setEndDate(now.plusDays(7));

        Package package2 = new Package();
        package2.setId("PKG-002");
        package2.setUserId("USER-002");
        package2.setPackageName("Cultural Experience");
        package2.setQuota(15);
        package2.setPrice(1800000L);
        package2.setStatus("PROCESSED");
        package2.setStartDate(now.plusDays(2));
        package2.setEndDate(now.plusDays(5));

        Package package3 = new Package();
        package3.setId("PKG-003");
        package3.setUserId("USER-003");
        package3.setPackageName("Water Sports Extravaganza");
        package3.setQuota(30);
        package3.setPrice(2200000L);
        package3.setStatus("PROCESSED");
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

        // Plans for Package 1 - Converted to Vehicle Rental examples
        Plan plan1 = new Plan();
        plan1.setId(UUID.randomUUID());
        plan1.setPackageEntity(packages.get(0));
        plan1.setPlanName("SUV Rental Plan");
        plan1.setPrice(750000L);
        plan1.setActivityType("Vehicle Rental");
        plan1.setStatus("FULFILLED");
        plan1.setStartDate(now.plusDays(7));
        plan1.setEndDate(now.plusDays(7).plusHours(8));
        plan1.setStartLocation("Jakarta Downtown");
        plan1.setEndLocation("Jakarta Downtown");
        plans.add(plan1);

        Plan plan2 = new Plan();
        plan2.setId(UUID.randomUUID());
        plan2.setPackageEntity(packages.get(0));
        plan2.setPlanName("Car Rental Plan");
        plan2.setPrice(500000L);
        plan2.setActivityType("Vehicle Rental");
        plan2.setStatus("FULFILLED");
        plan2.setStartDate(now.plusDays(5));
        plan2.setEndDate(now.plusDays(5).plusHours(4));
        plan2.setStartLocation("Kuta City Center");
        plan2.setEndLocation("Kuta City Center");
        plans.add(plan2);

        // Plans for Package 2 - Accommodation examples
        Plan plan3 = new Plan();
        plan3.setId(UUID.randomUUID());
        plan3.setPackageEntity(packages.get(1));
        plan3.setPlanName("City Hotel Plan");
        plan3.setPrice(350000L);
        plan3.setActivityType("Accommodation");
        plan3.setStatus("FULFILLED");
        plan3.setStartDate(now.plusDays(3));
        plan3.setEndDate(now.plusDays(4));
        plan3.setStartLocation("Downtown Hotel");
        plan3.setEndLocation("Downtown Hotel");
        plans.add(plan3);

        Plan plan4 = new Plan();
        plan4.setId(UUID.randomUUID());
        plan4.setPackageEntity(packages.get(1));
        plan4.setPlanName("Resort Hotel Plan");
        plan4.setPrice(450000L);
        plan4.setActivityType("Accommodation");
        plan4.setStatus("FULFILLED");
        plan4.setStartDate(now.plusDays(4));
        plan4.setEndDate(now.plusDays(5));
        plan4.setStartLocation("Ubud Resort");
        plan4.setEndLocation("Ubud Resort");
        plans.add(plan4);

        // Plans for Package 3 - Vehicle Rental examples
        Plan plan5 = new Plan();
        plan5.setId(UUID.randomUUID());
        plan5.setPackageEntity(packages.get(2));
        plan5.setPlanName("Vehicle Rental Option 1");
        plan5.setPrice(600000L);
        plan5.setActivityType("Vehicle Rental");
        plan5.setStatus("FULFILLED");
        plan5.setStartDate(now.plusDays(6));
        plan5.setEndDate(now.plusDays(6).plusHours(3));
        plan5.setStartLocation("Kuta City Center");
        plan5.setEndLocation("Kuta City Center");
        plans.add(plan5);

        Plan plan6 = new Plan();
        plan6.setId(UUID.randomUUID());
        plan6.setPackageEntity(packages.get(2));
        plan6.setPlanName("Vehicle Rental Option 2");
        plan6.setPrice(500000L);
        plan6.setActivityType("Vehicle Rental");
        plan6.setStatus("FULFILLED");
        plan6.setStartDate(now.plusDays(5));
        plan6.setEndDate(now.plusDays(5).plusHours(4));
        plan6.setStartLocation("Nusa Dua Center");
        plan6.setEndLocation("Nusa Dua Center");
        plans.add(plan6);

        // Plans for Package 4 - Relaxation Retreat (Accommodation)
        Plan plan7 = new Plan();
        plan7.setId(UUID.randomUUID());
        plan7.setPackageEntity(packages.get(3));
        plan7.setPlanName("Spa & Massage Plan");
        plan7.setPrice(400000L);
        plan7.setActivityType("Accommodation");
        plan7.setStatus("Unfulfilled");
        plan7.setStartDate(now.plusDays(10));
        plan7.setEndDate(now.plusDays(10).plusHours(2));
        plan7.setStartLocation("Seminyak Spa Center");
        plan7.setEndLocation("Seminyak Spa Center");
        plans.add(plan7);

        // Add a group of Flight plans in Jakarta -> Bali to simulate the spec
        // screenshots
        // Base plan that the user will open (in a PENDING package timeframe)
        Plan flightBase = new Plan();
        flightBase.setId(UUID.randomUUID());
        flightBase.setPackageEntity(packages.get(3)); // PENDING package
        flightBase.setPlanName("Jakarta-Bali Flight Plan");
        flightBase.setPrice(0L);
        flightBase.setActivityType("Flight");
        flightBase.setStatus("Unfulfilled");
        // Simulate 01/11/2025 08:00 - 10:30 with offsets relative to now
        flightBase.setStartDate(now.plusDays(0).withHour(8).withMinute(0).withSecond(0).withNano(0));
        flightBase.setEndDate(now.plusDays(0).withHour(10).withMinute(30).withSecond(0).withNano(0));
        flightBase.setStartLocation("DKI Jakarta (Provinsi)");
        flightBase.setEndLocation("Bali (Provinsi)");
        plans.add(flightBase);

        // Option 1 within the same window
        Plan flightOpt1 = new Plan();
        flightOpt1.setId(UUID.randomUUID());
        flightOpt1.setPackageEntity(packages.get(3));
        flightOpt1.setPlanName("Jakarta to Bali Flight");
        flightOpt1.setPrice(1_500_000L);
        flightOpt1.setActivityType("Flight");
        flightOpt1.setStatus("Unfulfilled");
        flightOpt1.setStartDate(flightBase.getStartDate());
        flightOpt1.setEndDate(flightBase.getEndDate());
        flightOpt1.setStartLocation(flightBase.getStartLocation());
        flightOpt1.setEndLocation(flightBase.getEndLocation());
        plans.add(flightOpt1);

        // Option 2 within the same window
        Plan flightOpt2 = new Plan();
        flightOpt2.setId(UUID.randomUUID());
        flightOpt2.setPackageEntity(packages.get(3));
        flightOpt2.setPlanName("Lion Air - Jakarta to Bali Flight");
        flightOpt2.setPrice(800_000L);
        flightOpt2.setActivityType("Flight");
        flightOpt2.setStatus("Unfulfilled");
        flightOpt2.setStartDate(flightBase.getStartDate());
        flightOpt2.setEndDate(flightBase.getEndDate());
        flightOpt2.setStartLocation(flightBase.getStartLocation());
        flightOpt2.setEndLocation(flightBase.getEndLocation());
        plans.add(flightOpt2);

        // Also add an Accommodation/Aceh set to test location/type/date matching for
        // another case
        Plan accBase = new Plan();
        accBase.setId(UUID.randomUUID());
        accBase.setPackageEntity(packages.get(3));
        accBase.setPlanName("Luxury Hotel Stay - Main Plan");
        accBase.setPrice(0L);
        accBase.setActivityType("Accommodation");
        accBase.setStatus("Unfulfilled");
        accBase.setStartDate(now.plusDays(10).withHour(8).withMinute(0).withSecond(0).withNano(0));
        accBase.setEndDate(now.plusDays(15).withHour(10).withMinute(30).withSecond(0).withNano(0));
        accBase.setStartLocation("Aceh");
        accBase.setEndLocation("Aceh");
        plans.add(accBase);

        Plan accOpt1 = new Plan();
        accOpt1.setId(UUID.randomUUID());
        accOpt1.setPackageEntity(packages.get(3));
        accOpt1.setPlanName("Budget Hotel Room");
        accOpt1.setPrice(300_000L);
        accOpt1.setActivityType("Accommodation");
        accOpt1.setStatus("Unfulfilled");
        accOpt1.setStartDate(now.plusDays(11).withHour(12).withMinute(0).withSecond(0).withNano(0));
        accOpt1.setEndDate(now.plusDays(13).withHour(9).withMinute(0).withSecond(0).withNano(0));
        accOpt1.setStartLocation("Aceh");
        accOpt1.setEndLocation("Aceh");
        plans.add(accOpt1);

        Plan accOpt2 = new Plan();
        accOpt2.setId(UUID.randomUUID());
        accOpt2.setPackageEntity(packages.get(3));
        accOpt2.setPlanName("Standard Room Accommodation");
        accOpt2.setPrice(500_000L);
        accOpt2.setActivityType("Accommodation");
        accOpt2.setStatus("Unfulfilled");
        accOpt2.setStartDate(now.plusDays(12).withHour(12).withMinute(0).withSecond(0).withNano(0));
        accOpt2.setEndDate(now.plusDays(14).withHour(9).withMinute(0).withSecond(0).withNano(0));
        accOpt2.setStartLocation("Aceh");
        accOpt2.setEndLocation("Aceh");
        plans.add(accOpt2);

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
