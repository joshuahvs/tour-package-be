package apap.ti._5.tour_package_2306165540_be.restservice;

import apap.ti._5.tour_package_2306165540_be.model.TopUpTransaction;
import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.model.profile.TourPackageVendor;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import apap.ti._5.tour_package_2306165540_be.repository.TopUpTransactionRepository;
import apap.ti._5.tour_package_2306165540_be.restdto.request.CreateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.request.UpdateEndUserRequestDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.CustomerResponseDTO;
import apap.ti._5.tour_package_2306165540_be.restdto.response.EndUserResponseDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndUserRestServiceImplTest {

    @Mock
    private EndUserRepository endUserRepository;

    @Mock
    private TopUpTransactionRepository topUpTransactionRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private EndUserRestServiceImpl service;

    @AfterEach
    void cleanupSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createEndUser_withoutRoleInheritsCreatorRole() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "creator", null, List.of(new SimpleGrantedAuthority("ROLE_TOUR_PACKAGE_VENDOR")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        TourPackageVendor creator = new TourPackageVendor();
        creator.setId(UUID.randomUUID());
        creator.setUsername("creator");
        creator.setPassword("encoded");
        creator.setActive(true);
        when(endUserRepository.findByUsernameIgnoreCase("creator"))
                .thenReturn(Optional.of(creator));
        when(endUserRepository.findByUsernameIgnoreCase("newuser"))
                .thenReturn(Optional.empty());
        when(endUserRepository.findByEmailIgnoreCase("new@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed");
        when(endUserRepository.save(any(EndUser.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateEndUserRequestDTO requestDTO = new CreateEndUserRequestDTO();
        requestDTO.setUsername("newuser");
        requestDTO.setEmail("new@example.com");
        requestDTO.setFullName("New User");
        requestDTO.setPassword("Secret123");

        EndUserResponseDTO responseDTO = service.createEndUser(requestDTO);

        assertThat(responseDTO.getRole()).isEqualTo(RoleType.TOUR_PACKAGE_VENDOR.name());
        assertThat(responseDTO.getUsername()).isEqualTo("newuser");
    }

    @Test
    void createEndUser_rejectsExplicitSuperadminRole() {
        CreateEndUserRequestDTO requestDTO = new CreateEndUserRequestDTO();
        requestDTO.setUsername("super");
        requestDTO.setEmail("super@example.com");
        requestDTO.setFullName("Super Admin");
        requestDTO.setPassword("secret");
        requestDTO.setRole(RoleType.SUPERADMIN.name());

        assertThatThrownBy(() -> service.createEndUser(requestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Superadmin");
    }

    @Test
    void updateEndUser_nonSuperadminCannotChangeSaldo() {
        Customer existing = new Customer();
        UUID id = UUID.randomUUID();
        existing.setId(id);
        existing.setUsername("customer");
        existing.setEmail("customer@example.com");
        existing.setFullName("Customer");
        existing.setPassword("encoded");
        existing.setActive(true);
        when(endUserRepository.findById(id)).thenReturn(Optional.of(existing));

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "customer", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateEndUserRequestDTO requestDTO = new UpdateEndUserRequestDTO();
        requestDTO.setId(id);
        requestDTO.setSaldo(1_000L);

        assertThatThrownBy(() -> service.updateEndUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Saldo hanya");
    }

    @Test
    void deductBalance_reducesSaldoAndPersists() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setSaldo(1_000L);
        when(endUserRepository.findById(customer.getId())).thenReturn(Optional.of(customer));

        service.deductBalance(customer.getId(), 200d);

        assertThat(customer.getSaldo()).isEqualTo(800L);
        verify(endUserRepository).save(customer);
    }

    @Test
    void searchCustomers_filtersByNameAndEmail() {
        Customer matching = buildCustomer("alice", "Alice Wonder", "alice@example.com");
        matching.setSaldo(10_000L);
        Customer notMatching = buildCustomer("bob", "Bobby", "bob@example.com");
        EndUser otherRole = new TourPackageVendor();
        otherRole.setUsername("vendor");
        otherRole.setFullName("Vendor");
        otherRole.setEmail("vendor@example.com");
        when(endUserRepository.findAllByOrderByUsernameAsc())
                .thenReturn(List.of(matching, notMatching, otherRole));

        List<CustomerResponseDTO> results = service.searchCustomers("alice", "alice@");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    void getUserProfile_returnsTopUpHistory() {
        Customer customer = buildCustomer("carol", "Carol", "carol@example.com");
        customer.setId(UUID.randomUUID());
        customer.setCreatedAt(LocalDateTime.now().minusDays(1));
        customer.setUpdatedAt(LocalDateTime.now());
        when(endUserRepository.findById(customer.getId())).thenReturn(Optional.of(customer));
        when(endUserRepository.findByUsernameIgnoreCase(customer.getId().toString())).thenReturn(Optional.empty());
        when(endUserRepository.findByEmailIgnoreCase(customer.getId().toString())).thenReturn(Optional.empty());

        TopUpTransaction transaction = new TopUpTransaction();
        transaction.setId(UUID.randomUUID());
        transaction.setUserId(customer.getId());
        transaction.setAmount(100L);
        transaction.setStatus("SUCCESS");
        transaction.setTransactionDate(LocalDateTime.now());
        when(topUpTransactionRepository.findByUserIdOrderByTransactionDateDesc(customer.getId()))
                .thenReturn(List.of(transaction));

        var profile = service.getUserProfile(customer.getId().toString());

        assertThat(profile).isNotNull();
        assertThat(profile.getTopUpTransactions()).hasSize(1);
    }

    @Test
    void updateEndUser_superadminCanUpdateSaldoAndNotes() {
        Customer existing = buildCustomer("vip", "VIP", "vip@example.com");
        existing.setId(UUID.randomUUID());
        when(endUserRepository.findById(existing.getId())).thenReturn(Optional.of(existing));
        when(endUserRepository.existsByUsernameIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), any())).thenReturn(false);
        when(endUserRepository.save(existing)).thenReturn(existing);

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        UpdateEndUserRequestDTO requestDTO = new UpdateEndUserRequestDTO();
        requestDTO.setId(existing.getId());
        requestDTO.setUsername("vip-new");
        requestDTO.setEmail("vip-new@example.com");
        requestDTO.setFullName("VIP Updated");
        requestDTO.setSaldo(5_000L);
        requestDTO.setOrganizationName("Org");
        requestDTO.setNotes("Important");
        requestDTO.setActive(false);

        EndUserResponseDTO response = service.updateEndUser(requestDTO);

        assertThat(response.getUsername()).isEqualTo("vip-new");
        assertThat(existing.getSaldo()).isEqualTo(5_000L);
        assertThat(existing.isActive()).isFalse();
        verify(endUserRepository).save(existing);
    }

    @Test
    void createEndUser_rejectsDuplicateUsername() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "creator", null, List.of(new SimpleGrantedAuthority("ROLE_SUPERADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(endUserRepository.findByUsernameIgnoreCase("dup"))
                .thenReturn(Optional.of(new Customer()));

        CreateEndUserRequestDTO requestDTO = new CreateEndUserRequestDTO();
        requestDTO.setUsername("dup");
        requestDTO.setEmail("dup@example.com");
        requestDTO.setFullName("Dup");
        requestDTO.setPassword("secret");

        assertThatThrownBy(() -> service.createEndUser(requestDTO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username");
    }

    @Test
    void getEndUserByIdOrUsernameOrEmail_prefersUsernameMatch() {
        Customer customer = buildCustomer("lookup", "Lookup", "lookup@example.com");
        when(endUserRepository.findByUsernameIgnoreCase("lookup"))
                .thenReturn(Optional.of(customer));

        EndUserResponseDTO result = service.getEndUserByIdOrUsernameOrEmail("lookup");
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("lookup");
    }

    @Test
    void getEndUsersByRole_filtersCorrectly() {
        Customer customer = buildCustomer("cust", "Customer", "c@example.com");
        TourPackageVendor vendor = new TourPackageVendor();
        vendor.setId(UUID.randomUUID());
        vendor.setUsername("vendor");
        vendor.setEmail("vendor@example.com");
        vendor.setFullName("Vendor");
        vendor.setActive(true);
        when(endUserRepository.findAllByOrderByUsernameAsc()).thenReturn(List.of(customer, vendor));

        List<EndUserResponseDTO> results = service.getEndUsersByRole(RoleType.CUSTOMER.name());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUsername()).isEqualTo("cust");
    }

    @Test
    void deductBalance_nonCustomerThrows() {
        TourPackageVendor vendor = new TourPackageVendor();
        vendor.setId(UUID.randomUUID());
        when(endUserRepository.findById(vendor.getId())).thenReturn(Optional.of(vendor));

        assertThatThrownBy(() -> service.deductBalance(vendor.getId(), 10d))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bukan Customer");
    }

    private Customer buildCustomer(String username, String fullName, String email) {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername(username);
        customer.setFullName(fullName);
        customer.setEmail(email);
        customer.setPassword("encoded");
        customer.setActive(true);
        return customer;
    }
}
