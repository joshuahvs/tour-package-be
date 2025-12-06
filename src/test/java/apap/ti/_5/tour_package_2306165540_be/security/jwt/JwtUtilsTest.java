package apap.ti._5.tour_package_2306165540_be.security.jwt;

import apap.ti._5.tour_package_2306165540_be.model.profile.Customer;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtUtilsTest {

    @Mock
    private EndUserRepository endUserRepository;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(endUserRepository);
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "very-secret-key-for-tests-1234567890");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3_600_000L);
    }

    @Test
    void generateToken_containsUserClaimsAndValidatesSuccessfully() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername("alice");
        customer.setEmail("alice@example.com");
        customer.setFullName("Alice Wonderland");
        when(endUserRepository.findByUsernameIgnoreCase("alice"))
                .thenReturn(Optional.of(customer));

        UserDetails principal = new User("alice", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        String token = jwtUtils.generateToken(authentication);

        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("alice");
        assertThat(jwtUtils.getRolesFromToken(token)).containsExactly("ROLE_CUSTOMER");
        assertThat(jwtUtils.getUserIdFromToken(token)).isEqualTo(customer.getId().toString());
        assertThat(jwtUtils.getNameFromToken(token)).isEqualTo("Alice Wonderland");
        assertThat(jwtUtils.getEmailFromToken(token)).isEqualTo("alice@example.com");
        assertThat(jwtUtils.getRoleFromToken(token)).isEqualTo("CUSTOMER");
        assertThat(jwtUtils.validateToken(token, principal)).isTrue();
    }

    @Test
    void getExpirationInstant_returnsFutureInstant() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setUsername("bob");
        customer.setEmail("bob@example.com");
        customer.setFullName("Bob Builder");
        when(endUserRepository.findByUsernameIgnoreCase("bob"))
                .thenReturn(Optional.of(customer));

        UserDetails principal = new User("bob", "password",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
                principal.getAuthorities());

        String token = jwtUtils.generateToken(authentication);
        Instant expiration = jwtUtils.getExpirationInstant(token);

        assertThat(expiration).isAfter(Instant.now());
    }
}
