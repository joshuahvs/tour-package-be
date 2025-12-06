package apap.ti._5.tour_package_2306165540_be.security.service;

import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.model.profile.RoleType;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private EndUserRepository endUserRepository;

    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new UserDetailsServiceImpl(endUserRepository);
    }

    @Test
    void loadUserByUsername_returnsActiveUser() {
        Customer customer = buildCustomer(true, "encoded");
        when(endUserRepository.findByUsernameIgnoreCase("alice"))
                .thenReturn(Optional.of(customer));

        UserDetails details = userDetailsService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactly(RoleType.CUSTOMER.name());
    }

    @Test
    void loadUserByUsername_throwsWhenInactive() {
        Customer customer = buildCustomer(false, "encoded");
        when(endUserRepository.findByUsernameIgnoreCase("inactive"))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inactive"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("tidak aktif");
    }

    @Test
    void loadUserByUsername_throwsWhenPasswordMissing() {
        Customer customer = buildCustomer(true, null);
        when(endUserRepository.findByUsernameIgnoreCase("nopass"))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("nopass"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("belum memiliki password");
    }

    private Customer buildCustomer(boolean active, String password) {
        Customer customer = new Customer();
        customer.setUsername("alice");
        customer.setPassword(password);
        customer.setActive(active);
        return customer;
    }
}
