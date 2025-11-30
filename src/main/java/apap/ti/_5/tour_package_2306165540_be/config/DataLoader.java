package apap.ti._5.tour_package_2306165540_be.config;

import apap.ti._5.tour_package_2306165540_be.model.Activity;
import apap.ti._5.tour_package_2306165540_be.model.OrderedQuantity;
import apap.ti._5.tour_package_2306165540_be.model.Package;
import apap.ti._5.tour_package_2306165540_be.model.Plan;
import apap.ti._5.tour_package_2306165540_be.model.profile.*;
import apap.ti._5.tour_package_2306165540_be.repository.ActivityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.OrderedQuantityRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PackageRepository;
import apap.ti._5.tour_package_2306165540_be.repository.PlanRepository;
import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private static final int FAKER_PER_ACTIVITY_TYPE = 120;

    private final ActivityRepository activityRepository;
    private final PackageRepository packageRepository;
    private final PlanRepository planRepository;
    private final OrderedQuantityRepository orderedQuantityRepository;
    private final EndUserRepository endUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Initializing database with fake data...");

        LocalDateTime baseTime = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);

        // Seed end users for Profile Service - always ensure they exist with correct
        // passwords
        ensureSeedUsers();

        // Check if data already exists
        if (activityRepository.count() > 0) {
            System.out.println("Database already has data. Skipping activity/package/plan initialization.");
            return;
        }

        // Get vendor user IDs for activity creators
        String flightAirlineId = endUserRepository.findByUsernameIgnoreCase("flightairline")
                .orElseThrow(() -> new RuntimeException("FlightAirline user not found"))
                .getId().toString();
        String accommodationOwnerId = endUserRepository.findByUsernameIgnoreCase("accommodationowner")
                .orElseThrow(() -> new RuntimeException("AccommodationOwner user not found"))
                .getId().toString();
        String rentalVendorId = endUserRepository.findByUsernameIgnoreCase("rentalvendor")
                .orElseThrow(() -> new RuntimeException("RentalVendor user not found"))
                .getId().toString();

        // Create Activities
        List<Activity> activities = createActivities(baseTime, flightAirlineId, accommodationOwnerId, rentalVendorId);
        activityRepository.saveAll(activities);

        // Create Packages
        List<Package> packages = createPackages(baseTime);
        packageRepository.saveAll(packages);

        // Create Plans
        List<Plan> plans = createPlans(packages);
        planRepository.saveAll(plans);

        // Create Ordered Quantities
        List<OrderedQuantity> orderedQuantities = createOrderedQuantities(plans, activities);
        orderedQuantityRepository.saveAll(orderedQuantities);

        // Persist recalculated totals on plans and packages
        planRepository.saveAll(plans);
        packageRepository.saveAll(packages);

        System.out.println("Database initialization completed!");
        System.out.println("Created " + endUserRepository.count() + " users");
        System.out.println("Created " + activities.size() + " activities");
        System.out.println("Created " + packages.size() + " packages");
        System.out.println("Created " + plans.size() + " plans");
        System.out.println("Created " + orderedQuantities.size() + " ordered quantities");
    }

    private void ensureSeedUsers() {
        System.out.println("Ensuring seed users exist with correct passwords...");

        upsertUser(new SuperAdmin(), "superadmin", "superadmin@travelapap.id", "Super Admin",
                "+62-21-555-0001", "pass", "TravelAPAP Center", "Pengelola utama sistem.");

        upsertUser(new AccommodationOwner(), "accommodationowner", "owner@staycation.id",
                "Accommodation Owner", "+62-812-1234-7001", "pass", "Staycation Indonesia",
                "Mengelola seluruh inventori penginapan.");

        upsertUser(new FlightAirline(), "flightairline", "ops@garuda-connect.id", "Flight Airline",
                "+62-21-3300-9911",
                "pass", "Garuda Connect", "Menyediakan layanan penerbangan premium.");

        upsertUser(new InsuranceProvider(), "insuranceprovider", "support@travelshield.id", "Insurance Provider",
                "+62-812-7777-4455", "pass", "Travel Shield", "Fokus pada proteksi perjalanan.");

        upsertUser(new TourPackageVendor(), "tourpackagevendor", "sales@wonderfuljourney.id",
                "Tour Package Vendor", "+62-811-5566-7788", "pass", "Wonderful Journey",
                "Menyusun itinerary wisata domestik.");

        upsertUser(new RentalVendor(), "rentalvendor", "contact@driveasy.id", "Rental Vendor",
                "+62-811-9988-1122", "pass", "Driveasy Fleet", "Menyediakan armada rental.");

        upsertUser(new Customer(), "customer", "customer@apap.id", "APAP Customer",
                "+62-815-3333-2211", "pass", null, "Customer percobaan untuk pengujian.");

        System.out.println("Seed users ensured: " + endUserRepository.count() + " total users in database");
    }

    private void upsertUser(EndUser template, String username, String email, String fullName,
            String phone, String password, String organization, String notes) {
        EndUser existingUser = endUserRepository.findByUsernameIgnoreCase(username).orElse(null);

        if (existingUser != null) {
            // Update existing user with new password
            existingUser.setEmail(email);
            existingUser.setFullName(fullName);
            if (existingUser instanceof RentalVendor rv) {
                rv.setPhone(phone);
            }
            if (password != null) {
                existingUser.setPassword(passwordEncoder.encode(password));
            }
            existingUser.setOrganizationName(organization);
            existingUser.setNotes(notes);
            existingUser.setActive(true);
            existingUser.setUpdatedAt(LocalDateTime.now());
            endUserRepository.save(existingUser);
        } else {
            // Create new user
            EndUser newUser = buildUser(template, username, email, fullName, phone, password, organization, notes);
            endUserRepository.save(newUser);
        }
    }

    private EndUser buildUser(EndUser user, String username, String email, String fullName,
            String phone, String password, String organization, String notes) {
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        if (user instanceof RentalVendor rv) {
            rv.setPhone(phone);
        }
        if (password != null) {
            user.setPassword(passwordEncoder.encode(password));
        }
        user.setOrganizationName(organization);
        user.setNotes(notes);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }

    private List<Activity> createActivities(LocalDateTime baseTime, String flightAirlineId,
            String accommodationOwnerId, String rentalVendorId) {
        LocalDateTime now = baseTime;
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
        activity1.setCreatorId(flightAirlineId);
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
        activity2.setCreatorId(accommodationOwnerId);
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
        activity3.setCreatorId(rentalVendorId);
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
        activity4.setCreatorId(accommodationOwnerId);
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
        activity5.setCreatorId(rentalVendorId);
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
        activity6.setCreatorId(flightAirlineId);
        activity6.setStartDate(now.plusDays(2));
        activity6.setEndDate(now.plusDays(2).plusHours(2));
        activity6.setStartLocation("Soekarno-Hatta Airport");
        activity6.setEndLocation("Ngurah Rai Airport");

        Activity activity7 = new Activity();
        activity7.setId("ACT-007");
        activity7.setActivityName("Garuda Charter Jakarta-Bali");
        activity7.setActivityItem("Flight Ticket");
        activity7.setCapacity(150);
        activity7.setPrice(1200000L);
        activity7.setActivityType("Flight");
        activity7.setCreatorId(flightAirlineId);
        activity7.setStartDate(now.plusDays(6));
        activity7.setEndDate(now.plusDays(6).plusHours(2));
        activity7.setStartLocation("Soekarno-Hatta Airport");
        activity7.setEndLocation("Ngurah Rai Airport");

        Activity activity8 = new Activity();
        activity8.setId("ACT-008");
        activity8.setActivityName("Seminyak Boutique Hotel");
        activity8.setActivityItem("Room, Breakfast Included");
        activity8.setCapacity(80);
        activity8.setPrice(650000L);
        activity8.setActivityType("Accommodation");
        activity8.setCreatorId(accommodationOwnerId);
        activity8.setStartDate(now.plusDays(7));
        activity8.setEndDate(now.plusDays(7).plusHours(24));
        activity8.setStartLocation("Seminyak Resort");
        activity8.setEndLocation("Seminyak Resort");

        Activity activity9 = new Activity();
        activity9.setId("ACT-009");
        activity9.setActivityName("Labuan Bajo SUV Fleet");
        activity9.setActivityItem("SUV Vehicle, Insurance");
        activity9.setCapacity(40);
        activity9.setPrice(900000L);
        activity9.setActivityType("Vehicle Rental");
        activity9.setCreatorId(rentalVendorId);
        activity9.setStartDate(now.plusDays(11));
        activity9.setEndDate(now.plusDays(11).plusHours(8));
        activity9.setStartLocation("Labuan Bajo Center");
        activity9.setEndLocation("Labuan Bajo Center");

        Activity activity10 = new Activity();
        activity10.setId("ACT-010");
        activity10.setActivityName("Nusa Dua Beachfront Villa");
        activity10.setActivityItem("Villa Stay, Breakfast Included");
        activity10.setCapacity(70);
        activity10.setPrice(950000L);
        activity10.setActivityType("Accommodation");
        activity10.setCreatorId(accommodationOwnerId);
        activity10.setStartDate(now.plusDays(15));
        activity10.setEndDate(now.plusDays(15).plusHours(48));
        activity10.setStartLocation("Nusa Dua Resort");
        activity10.setEndLocation("Nusa Dua Resort");

        activities.addAll(Arrays.asList(
                activity1, activity2, activity3, activity4, activity5, activity6,
                activity7, activity8, activity9, activity10));

        // Generate many more activities using JavaFaker
        activities.addAll(generateFakerActivities(10, baseTime, flightAirlineId, accommodationOwnerId, rentalVendorId));

        return activities;
    }

    private List<Activity> generateFakerActivities(int existingCount, LocalDateTime baseTime,
            String flightAirlineId, String accommodationOwnerId, String rentalVendorId) {
        // Start numbering after the existing fixed activities
        int nextIndex = existingCount + 1;
        // Reduce to create more manageable, relevant activities
        final int perType = 20; // Reduced from 120 to 20 per type
        List<Activity> list = new ArrayList<>();

        Faker faker = new Faker(Locale.forLanguageTag("id-ID"));
        LocalDateTime now = baseTime.truncatedTo(ChronoUnit.MINUTES);

        // Some Indonesian locations/provinces to keep things realistic
        List<String> provinces = Arrays.asList(
                "DKI Jakarta (Provinsi)", "Bali (Provinsi)", "Jawa Barat (Provinsi)",
                "Jawa Tengah (Provinsi)", "DI Yogyakarta (Provinsi)", "Jawa Timur (Provinsi)",
                "Sumatera Utara (Provinsi)", "Sumatera Barat (Provinsi)", "Aceh", "Riau",
                "Kalimantan Timur (Provinsi)", "Sulawesi Selatan (Provinsi)");

        // Flights - create activities with dates spread over next 2 months
        for (int i = 0; i < perType; i++) {
            String origin = randomPickDifferent(provinces, null);
            String dest = randomPickDifferent(provinces, origin);
            // Spread flights over next 60 days, mostly during daytime hours
            LocalDateTime start = now.plusDays(randBetween(1, 60))
                    .withHour(randBetween(6, 20))
                    .withMinute(randBetween(0, 3) * 15); // 0, 15, 30, 45 minutes
            LocalDateTime end = start.plusHours(randBetween(1, 3)).plusMinutes(randBetween(0, 3) * 15);

            Activity a = new Activity();
            a.setId(String.format("ACT-%03d", nextIndex++));
            a.setActivityName(String.format("%s to %s Flight",
                    cleanProvince(origin), cleanProvince(dest)));
            a.setActivityItem("Flight Ticket with Baggage");
            a.setCapacity(randBetween(50, 200));
            a.setPrice((long) randBetween(500_000, 3_000_000));
            a.setActivityType("Flight");
            a.setCreatorId(flightAirlineId);
            a.setStartDate(start);
            a.setEndDate(end);
            a.setStartLocation(origin);
            a.setEndLocation(dest);
            list.add(a);
        }

        // Accommodation - create hotels with dates covering plan periods
        for (int i = 0; i < perType; i++) {
            String city = randomPickDifferent(provinces, null);
            // Spread accommodations over next 90 days
            LocalDateTime start = now.plusDays(randBetween(1, 90))
                    .withHour(14) // Standard check-in 2 PM
                    .withMinute(0);
            LocalDateTime end = start.plusDays(randBetween(1, 7)) // 1-7 night stays
                    .withHour(11) // Standard check-out 11 AM
                    .withMinute(0);

            Activity a = new Activity();
            a.setId(String.format("ACT-%03d", nextIndex++));
            a.setActivityName(String.format("%s Hotel %s",
                    cleanProvince(city), (char) ('A' + (i % 26))));
            a.setActivityItem(String.format("%d Star Hotel Room, Breakfast Included",
                    randBetween(3, 5)));
            a.setCapacity(randBetween(10, 100));
            a.setPrice((long) randBetween(200_000, 2_000_000));
            a.setActivityType("Accommodation");
            a.setCreatorId(accommodationOwnerId);
            a.setStartDate(start);
            a.setEndDate(end);
            a.setStartLocation(city);
            a.setEndLocation(city);
            list.add(a);
        }

        // Vehicle Rental - create rentals with dates covering plan periods
        for (int i = 0; i < perType; i++) {
            String city = randomPickDifferent(provinces, null);
            // Spread rentals over next 60 days, typical business hours
            LocalDateTime start = now.plusDays(randBetween(1, 60))
                    .withHour(randBetween(8, 18))
                    .withMinute(0);
            LocalDateTime end = start.plusHours(randBetween(4, 12)); // Half-day to full-day rentals

            Activity a = new Activity();
            a.setId(String.format("ACT-%03d", nextIndex++));
            String[] vehicleTypes = { "Sedan", "SUV", "Minibus", "MPV", "Van" };
            a.setActivityName(String.format("%s %s Rental - %s",
                    cleanProvince(city),
                    vehicleTypes[randBetween(0, vehicleTypes.length - 1)],
                    (char) ('A' + (i % 26))));
            a.setActivityItem("Vehicle, Full Insurance, Driver Optional");
            a.setCapacity(randBetween(10, 50));
            a.setPrice((long) randBetween(300_000, 1_000_000));
            a.setActivityType("Vehicle Rental");
            a.setCreatorId(rentalVendorId);
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

    private List<Package> createPackages(LocalDateTime baseTime) {
        List<Package> packages = new ArrayList<>();

        packages.add(buildPackage(
                "PKG-001",
                "USER-001",
                "Bali Adventure Package",
                12,
                "Waiting for Payment",
                baseTime.plusDays(5).withHour(8),
                baseTime.plusDays(8).withHour(18)));

        packages.add(buildPackage(
                "PKG-002",
                "USER-002",
                "Cultural Experience",
                16,
                "Waiting for Payment",
                baseTime.plusDays(9).withHour(9),
                baseTime.plusDays(13).withHour(21)));

        packages.add(buildPackage(
                "PKG-003",
                "USER-003",
                "Water Sports Extravaganza",
                18,
                "Waiting for Payment",
                baseTime.plusDays(14).withHour(7),
                baseTime.plusDays(18).withHour(20)));

        packages.add(buildPackage(
                "PKG-004",
                "USER-001",
                "Relaxation Retreat",
                12,
                "PENDING",
                baseTime.plusDays(20).withHour(8),
                baseTime.plusDays(23).withHour(17)));

        return packages;
    }

    private Package buildPackage(String id, String userId, String packageName, int quota, String status,
            LocalDateTime startDate, LocalDateTime endDate) {
        Package packageEntity = new Package();
        packageEntity.setId(id);
        packageEntity.setUserId(userId);
        packageEntity.setPackageName(packageName);
        packageEntity.setQuota(quota);
        packageEntity.setPrice(0L);
        packageEntity.setStatus(status);
        packageEntity.setStartDate(startDate);
        packageEntity.setEndDate(endDate);
        packageEntity.setPlans(new ArrayList<>());
        return packageEntity;
    }

    private List<Plan> createPlans(List<Package> packages) {
        Map<String, Package> packageById = new HashMap<>();
        for (Package pkg : packages) {
            packageById.put(pkg.getId(), pkg);
        }

        List<Plan> plans = new ArrayList<>();

        Package pkg1 = packageById.get("PKG-001");
        plans.add(buildPlan(
                pkg1,
                "Chartered Flight Transfer",
                "Flight",
                "Soekarno-Hatta Airport",
                "Ngurah Rai Airport",
                pkg1.getStartDate().plusHours(6),
                pkg1.getStartDate().plusHours(9)));

        plans.add(buildPlan(
                pkg1,
                "Seminyak Boutique Stay",
                "Accommodation",
                "Seminyak Resort",
                "Seminyak Resort",
                pkg1.getStartDate().plusHours(12),
                pkg1.getStartDate().plusHours(60)));

        Package pkg2 = packageById.get("PKG-002");
        plans.add(buildPlan(
                pkg2,
                "Island Connector Flight",
                "Flight",
                "Juanda International Airport",
                "Komodo Airport",
                pkg2.getStartDate().plusHours(5),
                pkg2.getStartDate().plusHours(8)));

        plans.add(buildPlan(
                pkg2,
                "SUV Explorer Tour",
                "Vehicle Rental",
                "Labuan Bajo Center",
                "Labuan Bajo Center",
                pkg2.getStartDate().plusHours(20),
                pkg2.getStartDate().plusHours(26)));

        Package pkg3 = packageById.get("PKG-003");
        plans.add(buildPlan(
                pkg3,
                "Executive Flight Transfer",
                "Flight",
                "Halim Perdanakusuma Airport",
                "Lombok Airport",
                pkg3.getStartDate().plusHours(8),
                pkg3.getStartDate().plusHours(11)));

        plans.add(buildPlan(
                pkg3,
                "Beachfront Luxury Stay",
                "Accommodation",
                "Nusa Dua Resort",
                "Nusa Dua Resort",
                pkg3.getStartDate().plusHours(32),
                pkg3.getStartDate().plusHours(80)));

        Package pkg4 = packageById.get("PKG-004");
        plans.add(buildPlan(
                pkg4,
                "Pending Flight Allocation",
                "Flight",
                "Soekarno-Hatta Airport",
                "Ngurah Rai Airport",
                pkg4.getStartDate().plusHours(4),
                pkg4.getStartDate().plusHours(7)));

        plans.add(buildPlan(
                pkg4,
                "Wellness Retreat Itinerary",
                "Accommodation",
                "Ubud Wellness Center",
                "Ubud Wellness Center",
                pkg4.getStartDate().plusHours(22),
                pkg4.getStartDate().plusHours(58)));

        return plans;
    }

    private Plan buildPlan(Package packageEntity, String planName, String activityType,
            String startLocation, String endLocation, LocalDateTime startDate, LocalDateTime endDate) {
        Plan plan = new Plan();
        plan.setId(UUID.randomUUID());
        plan.setPackageEntity(packageEntity);
        plan.setPlanName(planName);
        plan.setPrice(0L);
        plan.setActivityType(activityType);
        plan.setStatus("UNFULFILLED");
        plan.setStartDate(startDate);
        LocalDateTime boundedEnd = endDate.isAfter(packageEntity.getEndDate()) ? packageEntity.getEndDate() : endDate;
        if (boundedEnd.isBefore(startDate)) {
            boundedEnd = startDate.plusHours(2);
            if (boundedEnd.isAfter(packageEntity.getEndDate())) {
                boundedEnd = packageEntity.getEndDate();
            }
        }
        plan.setEndDate(boundedEnd);
        plan.setStartLocation(startLocation);
        plan.setEndLocation(endLocation);
        plan.setOrderedQuantities(new ArrayList<>());
        packageEntity.getPlans().add(plan);
        return plan;
    }

    private List<OrderedQuantity> createOrderedQuantities(List<Plan> plans, List<Activity> activities) {
        Map<String, Plan> planByName = new HashMap<>();
        for (Plan plan : plans) {
            planByName.put(plan.getPlanName(), plan);
        }

        Map<String, Activity> activityById = new HashMap<>();
        for (Activity activity : activities) {
            activityById.put(activity.getId(), activity);
        }

        List<OrderedQuantity> orderedQuantities = new ArrayList<>();

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Chartered Flight Transfer"),
                activityById.get("ACT-007"),
                12));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Seminyak Boutique Stay"),
                activityById.get("ACT-008"),
                12));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Island Connector Flight"),
                activityById.get("ACT-001"),
                16));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("SUV Explorer Tour"),
                activityById.get("ACT-009"),
                16));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Executive Flight Transfer"),
                activityById.get("ACT-007"),
                18));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Beachfront Luxury Stay"),
                activityById.get("ACT-010"),
                18));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Pending Flight Allocation"),
                activityById.get("ACT-006"),
                12));

        orderedQuantities.add(buildOrderedQuantity(
                planByName.get("Wellness Retreat Itinerary"),
                activityById.get("ACT-008"),
                6));

        // Update aggregates so seeded data reflects real totals
        for (Plan plan : plans) {
            refreshPlanAggregate(plan);
        }

        refreshPackageAggregates(plans);

        return orderedQuantities;
    }

    private OrderedQuantity buildOrderedQuantity(Plan plan, Activity activity, int requestedQuota) {
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found for ordered quantity seeding.");
        }
        if (activity == null) {
            throw new IllegalArgumentException("Activity not found for ordered quantity seeding.");
        }

        int appliedQuota = Math.min(requestedQuota, activity.getCapacity());

        OrderedQuantity orderedQuantity = new OrderedQuantity();
        orderedQuantity.setId(UUID.randomUUID());
        orderedQuantity.setPlan(plan);
        orderedQuantity.setActivity(activity);
        orderedQuantity.setActivityPlan(null);
        orderedQuantity.setOrderedQuota(appliedQuota);
        orderedQuantity.setQuota(activity.getCapacity());
        orderedQuantity.setPrice(activity.getPrice());
        orderedQuantity.setStartDate(plan.getStartDate());

        long durationHours = ChronoUnit.HOURS.between(plan.getStartDate(), plan.getEndDate());
        long bookedHours = durationHours <= 0 ? 2 : Math.min(4, Math.max(1, durationHours));
        LocalDateTime tentativeEnd = plan.getStartDate().plusHours(bookedHours);
        if (tentativeEnd.isAfter(plan.getEndDate())) {
            orderedQuantity.setEndDate(plan.getEndDate());
        } else {
            orderedQuantity.setEndDate(tentativeEnd);
        }

        plan.getOrderedQuantities().add(orderedQuantity);
        activity.getOrderedQuantities().add(orderedQuantity);

        return orderedQuantity;
    }

    private void refreshPlanAggregate(Plan plan) {
        long totalPrice = 0L;
        int totalOrdered = 0;
        for (OrderedQuantity oq : plan.getOrderedQuantities()) {
            if (oq.getDeletedAt() != null) {
                continue;
            }
            totalPrice += oq.getPrice() * oq.getOrderedQuota();
            totalOrdered += oq.getOrderedQuota();
        }
        plan.setPrice(totalPrice);
        if (totalOrdered >= plan.getPackageEntity().getQuota()) {
            plan.setStatus("FULFILLED");
        } else {
            plan.setStatus("UNFULFILLED");
        }
    }

    private void refreshPackageAggregates(List<Plan> plans) {
        Map<String, Package> packages = new HashMap<>();
        for (Plan plan : plans) {
            Package pkg = plan.getPackageEntity();
            packages.putIfAbsent(pkg.getId(), pkg);
        }

        for (Package pkg : packages.values()) {
            long totalPrice = 0L;
            for (Plan plan : pkg.getPlans()) {
                if (plan.getDeletedAt() != null) {
                    continue;
                }
                totalPrice += plan.getPrice();
            }
            pkg.setPrice(totalPrice);
        }
    }
}
