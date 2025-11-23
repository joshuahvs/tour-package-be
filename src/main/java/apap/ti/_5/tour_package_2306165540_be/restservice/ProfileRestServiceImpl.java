package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.profile.*;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.RoleResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileRestServiceImpl implements ProfileRestService {

    private final EndUserRepository endUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public EndUserResponseDTO upsertEndUser(UpsertEndUserRequestDTO requestDTO) {
        validateRequest(requestDTO);
        RoleType roleType = RoleType.fromCode(requestDTO.getRole());

        Optional<EndUser> byUsername = endUserRepository.findByUsernameIgnoreCase(requestDTO.getUsername());
        Optional<EndUser> byEmail = endUserRepository.findByEmailIgnoreCase(requestDTO.getEmail());

        EndUser existing = resolveExisting(byUsername, byEmail);
        EndUser target = prepareTargetEntity(existing, roleType);

        applyRequestToEntity(target, requestDTO);

        if (target.getId() != null) {
            if (endUserRepository.existsByUsernameIgnoreCaseAndIdNot(target.getUsername(), target.getId())) {
                throw new IllegalArgumentException("Username sudah digunakan oleh user lain.");
            }
            if (endUserRepository.existsByEmailIgnoreCaseAndIdNot(target.getEmail(), target.getId())) {
                throw new IllegalArgumentException("Email sudah digunakan oleh user lain.");
            }
        }

        EndUser saved = endUserRepository.save(target);
        return toResponse(saved);
    }

    @Override
    public List<EndUserResponseDTO> getEndUsers(boolean includeInactive) {
        List<EndUser> users = includeInactive
                ? endUserRepository.findAllByOrderByUsernameAsc()
                : endUserRepository.findAllByActiveIsTrueOrderByUsernameAsc();
        return users.stream().map(this::toResponse).toList();
    }

    @Override
    public EndUserResponseDTO getEndUserByUsername(String username) {
        EndUser user = endUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(
                        () -> new IllegalArgumentException("User dengan username " + username + " tidak ditemukan."));
        return toResponse(user);
    }

    @Override
    public void deactivateEndUser(String username) {
        EndUser user = endUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(
                        () -> new IllegalArgumentException("User dengan username " + username + " tidak ditemukan."));
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        endUserRepository.save(user);
    }

    @Override
    public List<RoleResponseDTO> getRoleDefinitions() {
        List<RoleResponseDTO> roles = new ArrayList<>();
        for (RoleType role : RoleType.values()) {
            roles.add(new RoleResponseDTO(role.name(), role.getDisplayName(), role.getResponsibility()));
        }
        return roles;
    }

    private void validateRequest(UpsertEndUserRequestDTO request) {
        if (isBlank(request.getUsername()) || isBlank(request.getEmail()) || isBlank(request.getFullName())
                || isBlank(request.getRole())) {
            throw new IllegalArgumentException("Username, email, fullName, dan role wajib diisi.");
        }
    }

    private EndUser resolveExisting(Optional<EndUser> byUsername, Optional<EndUser> byEmail) {
        if (byUsername.isPresent() && byEmail.isPresent() && !byUsername.get().getId().equals(byEmail.get().getId())) {
            throw new IllegalArgumentException("Username dan email mengarah ke user yang berbeda.");
        }
        return byUsername.or(() -> byEmail).orElse(null);
    }

    private EndUser prepareTargetEntity(EndUser existing, RoleType requestedRole) {
        if (existing == null) {
            EndUser created = instantiateRole(requestedRole);
            created.setId(UUID.randomUUID());
            created.setCreatedAt(LocalDateTime.now());
            return created;
        }

        if (existing.getRoleType() == requestedRole) {
            return existing;
        }

        UUID persistentId = existing.getId();
        LocalDateTime createdAt = existing.getCreatedAt();
        endUserRepository.delete(existing);
        endUserRepository.flush();

        EndUser converted = instantiateRole(requestedRole);
        converted.setId(persistentId);
        converted.setCreatedAt(createdAt);
        return converted;
    }

    private void applyRequestToEntity(EndUser target, UpsertEndUserRequestDTO requestDTO) {
        target.setUsername(requestDTO.getUsername().trim());
        target.setEmail(requestDTO.getEmail().trim());
        target.setFullName(requestDTO.getFullName().trim());
        if (target instanceof RentalVendor rv) {
            rv.setPhone(trimToNull(requestDTO.getPhoneNumber()));
        }
        boolean hasNewPassword = !isBlank(requestDTO.getPassword());
        if (hasNewPassword) {
            target.setPassword(passwordEncoder.encode(requestDTO.getPassword().trim()));
        } else if (target.getPassword() == null) {
            throw new IllegalArgumentException("Password wajib diisi untuk user baru.");
        }
        target.setOrganizationName(trimToNull(requestDTO.getOrganizationName()));
        target.setNotes(trimToNull(requestDTO.getNotes()));
        if (requestDTO.getActive() != null) {
            target.setActive(requestDTO.getActive());
        }
        target.setUpdatedAt(LocalDateTime.now());
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

    private EndUserResponseDTO toResponse(EndUser user) {
        EndUserResponseDTO dto = new EndUserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        if (user instanceof RentalVendor rv) {
            dto.setPhoneNumber(rv.getPhone());
        }
        dto.setRole(user.getRoleType().name());
        dto.setRoleDisplayName(user.getRoleType().getDisplayName());
        dto.setResponsibility(user.getRoleType().getResponsibility());
        dto.setActive(user.isActive());
        dto.setOrganizationName(user.getOrganizationName());
        dto.setNotes(user.getNotes());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
