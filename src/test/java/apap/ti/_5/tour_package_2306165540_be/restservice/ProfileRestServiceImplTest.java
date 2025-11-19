package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.model.profile.SuperAdmin;
import apap.ti._5.tour_package_2306165540_be.model.profile.TourPackageVendor;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpsertEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileRestServiceImplTest {

    @Mock
    private EndUserRepository endUserRepository;

    @InjectMocks
    private ProfileRestServiceImpl profileRestService;

    private UpsertEndUserRequestDTO baseRequest;

    @BeforeEach
    void setUp() {
        baseRequest = new UpsertEndUserRequestDTO();
        baseRequest.setUsername("superadmin");
        baseRequest.setEmail("admin@travelapap.id");
        baseRequest.setFullName("Super Admin");
        baseRequest.setRole(RoleType.SUPERADMIN.name());
        baseRequest.setPhoneNumber("0812");
        baseRequest.setNotes("note");
    }

    @Test
    @DisplayName("Upsert creates new user when username & email not found")
    void upsert_creates_new_user() {
        when(endUserRepository.findByUsernameIgnoreCase("superadmin")).thenReturn(Optional.empty());
        when(endUserRepository.findByEmailIgnoreCase("admin@travelapap.id")).thenReturn(Optional.empty());
        when(endUserRepository.existsByUsernameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.save(any(EndUser.class))).thenAnswer(inv -> inv.getArgument(0));

        EndUserResponseDTO response = profileRestService.upsertEndUser(baseRequest);
        assertThat(response.getUsername()).isEqualTo("superadmin");
        assertThat(response.getRole()).isEqualTo(RoleType.SUPERADMIN.name());
        assertThat(response.isActive()).isTrue();
        verify(endUserRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Upsert updates existing user when username already exists")
    void upsert_updates_existing_user() {
        SuperAdmin existing = new SuperAdmin();
        existing.setId(UUID.randomUUID());
        existing.setUsername("superadmin");
        existing.setEmail("old@travelapap.id");
        existing.setFullName("Old");
        existing.setCreatedAt(LocalDateTime.now().minusDays(1));
        existing.setUpdatedAt(LocalDateTime.now().minusDays(1));

        when(endUserRepository.findByUsernameIgnoreCase("superadmin")).thenReturn(Optional.of(existing));
        when(endUserRepository.findByEmailIgnoreCase("admin@travelapap.id")).thenReturn(Optional.of(existing));
        when(endUserRepository.existsByUsernameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.save(existing)).thenReturn(existing);

        EndUserResponseDTO response = profileRestService.upsertEndUser(baseRequest);
        assertThat(response.getEmail()).isEqualTo("admin@travelapap.id");
        assertThat(existing.getFullName()).isEqualTo("Super Admin");
        verify(endUserRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Upsert switches role by deleting old subclass and creating new one")
    void upsert_switch_role() {
        Customer existing = new Customer();
        existing.setId(UUID.randomUUID());
        existing.setUsername("tour.vendor");
        existing.setEmail("tour@customer.id");
        existing.setFullName("Customer User");
        existing.setCreatedAt(LocalDateTime.now().minusDays(3));
        existing.setUpdatedAt(LocalDateTime.now().minusDays(2));

        UpsertEndUserRequestDTO request = new UpsertEndUserRequestDTO();
        request.setUsername("tour.vendor");
        request.setEmail("tour@customer.id");
        request.setFullName("Vendor Baru");
        request.setRole(RoleType.TOUR_PACKAGE_VENDOR.name());

        when(endUserRepository.findByUsernameIgnoreCase("tour.vendor")).thenReturn(Optional.of(existing));
        when(endUserRepository.findByEmailIgnoreCase("tour@customer.id")).thenReturn(Optional.of(existing));
        when(endUserRepository.existsByUsernameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.save(any(EndUser.class))).thenAnswer(inv -> inv.getArgument(0));

        EndUserResponseDTO response = profileRestService.upsertEndUser(request);
        assertThat(response.getRole()).isEqualTo(RoleType.TOUR_PACKAGE_VENDOR.name());
        verify(endUserRepository).delete(existing);
        verify(endUserRepository).flush();
    }

    @Test
    @DisplayName("Upsert throws when username & email belong to different users")
    void upsert_conflicting_identity() {
        SuperAdmin usernameOwner = new SuperAdmin();
        usernameOwner.setId(UUID.randomUUID());
        usernameOwner.setUsername("conflict");
        usernameOwner.setEmail("user1@travelapap.id");

        Customer emailOwner = new Customer();
        emailOwner.setId(UUID.randomUUID());
        emailOwner.setUsername("other");
        emailOwner.setEmail("user2@travelapap.id");

        UpsertEndUserRequestDTO request = new UpsertEndUserRequestDTO();
        request.setUsername("conflict");
        request.setEmail("user2@travelapap.id");
        request.setFullName("Any");
        request.setRole(RoleType.CUSTOMER.name());

        when(endUserRepository.findByUsernameIgnoreCase("conflict")).thenReturn(Optional.of(usernameOwner));
        when(endUserRepository.findByEmailIgnoreCase("user2@travelapap.id")).thenReturn(Optional.of(emailOwner));

        assertThatThrownBy(() -> profileRestService.upsertEndUser(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username dan email mengarah ke user yang berbeda");
    }

    @Test
    @DisplayName("Deactivate user sets active=false")
    void deactivate_user() {
        TourPackageVendor vendor = new TourPackageVendor();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("vendor");
        vendor.setEmail("vendor@travelapap.id");
        vendor.setFullName("Vendor");
        vendor.setActive(true);

        when(endUserRepository.findByUsernameIgnoreCase("vendor")).thenReturn(Optional.of(vendor));

        profileRestService.deactivateEndUser("vendor");
        assertThat(vendor.isActive()).isFalse();
        verify(endUserRepository).save(vendor);
    }
}
