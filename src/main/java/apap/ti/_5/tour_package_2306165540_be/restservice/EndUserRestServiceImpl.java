package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306165540_be.model.profile.*;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.TopUpTransactionRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.CustomerResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.TopUpTransactionResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.UserProfileResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EndUserRestServiceImpl implements EndUserRestService {

    private static final String SALDO_SUPERADMIN_ONLY_MESSAGE = "Saldo hanya dapat diperbarui oleh Superadmin.";

    private final EndUserRepository endUserRepository;
    private final TopUpTransactionRepository topUpTransactionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<EndUserResponseDTO> getAllEndUsers() {
        return endUserRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::toEndUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EndUserResponseDTO> getEndUsersByRole(String roleType) {
        RoleType role = RoleType.fromCode(roleType);
        return endUserRepository.findAllByOrderByUsernameAsc().stream()
                .filter(user -> user.getRoleType() == role)
                .map(this::toEndUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<CustomerResponseDTO> searchCustomers(String name, String email) {
        String nameFilter = normalizeFilter(name);
        String emailFilter = normalizeFilter(email);

        return endUserRepository.findAllByOrderByUsernameAsc().stream()
                .filter(Customer.class::isInstance)
                .map(Customer.class::cast)
                .filter(customer -> matchesCustomer(customer, nameFilter, emailFilter))
                .sorted(Comparator.comparing(EndUser::getUsername, String.CASE_INSENSITIVE_ORDER))
                .map(this::toCustomerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public EndUserResponseDTO getEndUserByIdOrUsernameOrEmail(String identifier) {
        Optional<EndUser> byId = parseUuid(identifier)
                .flatMap(endUserRepository::findById);
        Optional<EndUser> byUsername = endUserRepository.findByUsernameIgnoreCase(identifier);
        Optional<EndUser> byEmail = endUserRepository.findByEmailIgnoreCase(identifier);

        return byId.or(() -> byUsername).or(() -> byEmail)
                .map(this::toEndUserResponse)
                .orElse(null);
    }

    @Override
    public UserProfileResponseDTO getUserProfile(String identifier) {
        Optional<EndUser> byId = parseUuid(identifier)
                .flatMap(endUserRepository::findById);
        Optional<EndUser> byUsername = endUserRepository.findByUsernameIgnoreCase(identifier);
        Optional<EndUser> byEmail = endUserRepository.findByEmailIgnoreCase(identifier);

        EndUser user = byId.or(() -> byUsername).or(() -> byEmail)
                .orElse(null);

        if (user == null) {
            return null;
        }

        return toUserProfileResponse(user);
    }

    @Override
    public EndUserResponseDTO createEndUser(CreateEndUserRequestDTO requestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Authority di-set sebagai "SUPERADMIN" (tanpa prefix ROLE_) di UserDetailsServiceImpl
        boolean isSuperadmin = hasAuthority(authentication, "SUPERADMIN");

        // Jika role tidak diisi, tentukan role berdasarkan pembuat
        RoleType targetRole;
        if (requestDTO.getRole() == null || requestDTO.getRole().trim().isEmpty()) {
            targetRole = determineRoleFromCreator(authentication, isSuperadmin);
        } else {
            targetRole = RoleType.fromCode(requestDTO.getRole().trim());
        }

        // SUPERADMIN tidak dapat dibuat melalui API, hanya lewat sistem
        if (targetRole == RoleType.SUPERADMIN) {
            throw new AccessDeniedException("Superadmin hanya dapat ditambahkan oleh sistem di awal run program.");
        }

        validateUniqueness(requestDTO.getUsername(), requestDTO.getEmail(), null);

        EndUser entity = instantiateRole(targetRole);
        entity.setId(UUID.randomUUID());
        applyCommonFieldsWithGender(entity, requestDTO.getUsername(), requestDTO.getEmail(), requestDTO.getFullName(),
                requestDTO.getGender(), requestDTO.getOrganizationName(), requestDTO.getNotes());
        entity.setPassword(passwordEncoder.encode(requestDTO.getPassword().trim()));
        entity.setActive(requestDTO.getActive() == null || requestDTO.getActive());
        entity.setCreatedAt(LocalDateTime.now());

        if (entity instanceof Customer customer) {
            customer.setSaldo(requestDTO.getSaldo() != null ? requestDTO.getSaldo() : 0L);
        }

        EndUser saved = endUserRepository.save(entity);
        return toEndUserResponse(saved);
    }

    @Override
    public EndUserResponseDTO updateEndUser(UpdateEndUserRequestDTO requestDTO) {
        EndUser existing = endUserRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "EndUser dengan id " + requestDTO.getId() + " tidak ditemukan."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;
        // Authority di-set sebagai "SUPERADMIN" (tanpa prefix ROLE_) di UserDetailsServiceImpl
        boolean isSuperadmin = hasAuthority(authentication, "SUPERADMIN");

        // Ownership validation: SUPERADMIN bisa update semua user, non-SUPERADMIN hanya bisa update akun sendiri
        // Khusus untuk update saldo, SUPERADMIN harus bisa update saldo customer lain (untuk top-up)
        if (!isSuperadmin && (currentUsername == null
                || !existing.getUsername().equalsIgnoreCase(currentUsername))) {
            throw new AccessDeniedException("Anda hanya dapat memperbarui akun milik sendiri.");
        }

        validateUniqueness(requestDTO.getUsername(), requestDTO.getEmail(), existing.getId());

        if (hasText(requestDTO.getUsername())) {
            existing.setUsername(requestDTO.getUsername().trim());
        }
        if (hasText(requestDTO.getEmail())) {
            existing.setEmail(requestDTO.getEmail().trim());
        }
        if (hasText(requestDTO.getFullName())) {
            existing.setFullName(requestDTO.getFullName().trim());
        }
        if (requestDTO.getGender() != null) {
            existing.setGender(trimToNull(requestDTO.getGender()));
        }
        if (existing instanceof RentalVendor rv && requestDTO.getPhoneNumber() != null) {
            String phoneNumber = requestDTO.getPhoneNumber().trim();
            rv.setPhone(phoneNumber.isEmpty() ? null : phoneNumber);
        }
        if (hasText(requestDTO.getPassword())) {
            existing.setPassword(passwordEncoder.encode(requestDTO.getPassword().trim()));
        }

        if (isSuperadmin) {
            if (requestDTO.getOrganizationName() != null) {
                String organization = requestDTO.getOrganizationName().trim();
                existing.setOrganizationName(organization.isEmpty() ? null : organization);
            }
            if (requestDTO.getNotes() != null) {
                String notes = requestDTO.getNotes().trim();
                existing.setNotes(notes.isEmpty() ? null : notes);
            }
            if (requestDTO.getActive() != null) {
                existing.setActive(requestDTO.getActive());
            }
        } else {
            if (requestDTO.getSaldo() != null) {
                throw new IllegalArgumentException(SALDO_SUPERADMIN_ONLY_MESSAGE);
            }
        }

        if (existing instanceof Customer customer && requestDTO.getSaldo() != null) {
            customer.setSaldo(requestDTO.getSaldo());
        }

        existing.setUpdatedAt(LocalDateTime.now());

        EndUser saved = endUserRepository.save(existing);
        return toEndUserResponse(saved);
    }

    @Override
    public void deleteEndUser(UUID id) {
        EndUser existing = endUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("EndUser dengan id " + id + " tidak ditemukan."));
        endUserRepository.delete(existing);
    }

    private void validateUniqueness(String username, String email, UUID excludeId) {
        if (hasText(username)) {
            boolean exists = excludeId == null
                    ? endUserRepository.findByUsernameIgnoreCase(username.trim()).isPresent()
                    : endUserRepository.existsByUsernameIgnoreCaseAndIdNot(username.trim(), excludeId);
            if (exists) {
                throw new IllegalArgumentException("Username sudah digunakan.");
            }
        }

        if (hasText(email)) {
            boolean exists = excludeId == null
                    ? endUserRepository.findByEmailIgnoreCase(email.trim()).isPresent()
                    : endUserRepository.existsByEmailIgnoreCaseAndIdNot(email.trim(), excludeId);
            if (exists) {
                throw new IllegalArgumentException("Email sudah digunakan.");
            }
        }
    }

    @Override
    public void deductBalance(UUID userId, Double amount) {
        EndUser user = endUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan."));

        if (!(user instanceof Customer)) {
             throw new IllegalArgumentException("Tipe user ini bukan Customer, jadi tidak memiliki saldo.");
        }

  
        Customer customer = (Customer) user;

        long currentBalance = (customer.getSaldo() == null) ? 0L : customer.getSaldo();
        long amountToDeduct = amount.longValue();

        if (currentBalance < amountToDeduct) {
            throw new IllegalArgumentException("\"User balance insufficient, please Top Up balance. Current Balance " + currentBalance);
        }

        customer.setSaldo(currentBalance - amountToDeduct);
        customer.setUpdatedAt(LocalDateTime.now());
        
        endUserRepository.save(customer);
    }

    // private void applyCommonFields(EndUser user, String username, String email, String fullName,
    //         String organizationName, String notes) {
    //     user.setUsername(username.trim());
    //     user.setEmail(email.trim());
    //     user.setFullName(fullName.trim());
    //     user.setOrganizationName(trimToNull(organizationName));
    //     user.setNotes(trimToNull(notes));
    // }

    private void applyCommonFieldsWithGender(EndUser user, String username, String email, String fullName,
            String gender, String organizationName, String notes) {
        user.setUsername(username.trim());
        user.setEmail(email.trim());
        user.setFullName(fullName.trim());
        user.setGender(trimToNull(gender));
        user.setOrganizationName(trimToNull(organizationName));
        user.setNotes(trimToNull(notes));
    }

    private EndUserResponseDTO toEndUserResponse(EndUser user) {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setGender(user.getGender());
        if (user instanceof RentalVendor rv) {
            dto.setPhoneNumber(rv.getPhone());
            dto.setLocations(rv.getListOfLocations());
        }
        dto.setRole(user.getRoleType().name());
        dto.setRoleDisplayName(user.getRoleType().getDisplayName());
        dto.setResponsibility(user.getRoleType().getResponsibility());
        dto.setActive(user.isActive());
        dto.setOrganizationName(user.getOrganizationName());
        dto.setNotes(user.getNotes());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        if (user instanceof Customer customer) {
            dto.setSaldo(customer.getSaldo());
        }
        return dto;
    }

    private CustomerResponseDTO toCustomerResponse(Customer customer) {
        CustomerResponseDTO dto = new CustomerResponseDTO();
        dto.setId(customer.getId());
        dto.setUsername(customer.getUsername());
        dto.setEmail(customer.getEmail());
        dto.setFullName(customer.getFullName());
        // Customers do not have phone attribute
        dto.setActive(customer.isActive());
        dto.setSaldo(customer.getSaldo());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }

    private UserProfileResponseDTO toUserProfileResponse(EndUser user) {
        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setGender(user.getGender());
        dto.setRole(user.getRoleType().name());
        dto.setRoleDisplayName(user.getRoleType().getDisplayName());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());

        // Set saldo only for Customer
        if (user instanceof Customer customer) {
            dto.setSaldo(customer.getSaldo());
        } else {
            dto.setSaldo(0L);
        }

        // Get top-up transactions
        List<TopUpTransaction> transactions = topUpTransactionRepository
                .findByUserIdOrderByTransactionDateDesc(user.getId());
        List<TopUpTransactionResponseDTO> transactionDTOs = transactions.stream()
                .map(this::toTopUpTransactionResponse)
                .collect(Collectors.toList());
        dto.setTopUpTransactions(transactionDTOs);

        return dto;
    }

    private TopUpTransactionResponseDTO toTopUpTransactionResponse(TopUpTransaction transaction) {
        TopUpTransactionResponseDTO dto = new TopUpTransactionResponseDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUserId());
        dto.setAmount(transaction.getAmount());
        dto.setStatus(transaction.getStatus());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setDescription(transaction.getDescription());
        return dto;
    }

    private boolean matchesCustomer(Customer customer, String nameFilter, String emailFilter) {
        boolean matchesName = nameFilter == null
                || customer.getFullName().toLowerCase().contains(nameFilter)
                || customer.getUsername().toLowerCase().contains(nameFilter);
        boolean matchesEmail = emailFilter == null
                || customer.getEmail().toLowerCase().contains(emailFilter);
        return matchesName && matchesEmail;
    }

    private String normalizeFilter(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private Optional<UUID> parseUuid(String identifier) {
        try {
            return Optional.of(UUID.fromString(identifier));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private RoleType resolveRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return RoleType.CUSTOMER;
        }
        return RoleType.fromCode(role.trim());
    }

    /**
     * Menentukan role EndUser baru berdasarkan siapa yang membuatnya.
     * Jika SUPERADMIN yang membuat, default ke CUSTOMER.
     * Jika role lain yang membuat, default ke role pembuat itu sendiri.
     */
    private RoleType determineRoleFromCreator(Authentication authentication, boolean isSuperadmin) {
        if (isSuperadmin) {
            // SUPERADMIN dapat membuat user apa saja, default ke CUSTOMER
            return RoleType.CUSTOMER;
        }

        // Non-SUPERADMIN membuat user dengan role yang sama dengan dirinya
        if (authentication == null || authentication.getName() == null) {
            return RoleType.CUSTOMER;
        }

        // Cari user yang sedang login untuk mendapatkan role-nya
        Optional<EndUser> currentUser = endUserRepository.findByUsernameIgnoreCase(authentication.getName());
        if (currentUser.isPresent()) {
            return currentUser.get().getRoleType();
        }

        // Fallback ke CUSTOMER jika tidak ditemukan
        return RoleType.CUSTOMER;
    }

    private EndUser instantiateRole(RoleType roleType) {
        return switch (roleType) {
            case SUPERADMIN -> new SuperAdmin();
            case ACCOMMODATION_OWNER -> new AccommodationOwner();
            case FLIGHT_AIRLINE -> new FlightAirline();
            case INSURANCE_PROVIDER -> new InsuranceProvider();
            case TOUR_PACKAGE_VENDOR -> new TourPackageVendor();
            case RENTAL_VENDOR -> new RentalVendor();
            case CUSTOMER -> new Customer();
        };
    }

    private boolean hasAuthority(Authentication authentication, String requiredAuthority) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authorityName = authority.getAuthority();
            // Check exact match atau tanpa prefix ROLE_ (karena authority di-set sebagai "SUPERADMIN" bukan "ROLE_SUPERADMIN")
            if (requiredAuthority.equalsIgnoreCase(authorityName) 
                    || requiredAuthority.equalsIgnoreCase("ROLE_" + authorityName)
                    || authorityName.equalsIgnoreCase("ROLE_" + requiredAuthority)) {
                return true;
            }
        }
        return false;
    }
}
