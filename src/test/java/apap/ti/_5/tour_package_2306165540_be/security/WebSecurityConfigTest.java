package apap.ti._5.tour_package_2306165540_be.security;

import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = WebSecurityConfigTest.SecurityTestConfig.class)
@ActiveProfiles("test")
class WebSecurityConfigTest {

    @Autowired
    private WebSecurityConfig webSecurityConfig;

    @Autowired
    @Qualifier("jwtFilterChain")
    private SecurityFilterChain jwtFilterChain;

    @Autowired
    @Qualifier("webFilterChain")
    private SecurityFilterChain webFilterChain;

    @Autowired
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    void jwtFilterChain_matchesApiRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/auth/login");
        assertThat(jwtFilterChain.matches(request)).isTrue();
    }

    @Test
    void webFilterChain_matchesNonApiRequests() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/dashboard");
        assertThat(webFilterChain.matches(request)).isTrue();
        request.setRequestURI("/api/secure");
        assertThat(webFilterChain.matches(request)).isFalse();
    }

    @Test
    void passwordEncoder_encodesPasswords() {
        BCryptPasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        String encoded = encoder.encode("secret");
        assertThat(encoder.matches("secret", encoded)).isTrue();
    }

    @Test
    void authenticationManagerBeanIsProvided() throws Exception {
        AuthenticationManager manager = webSecurityConfig.authenticationManager(authenticationConfiguration);
        assertThat(manager).isNotNull();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            DataSourceTransactionManagerAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class
    })
    @Import(WebSecurityConfig.class)
    static class SecurityTestConfig {
        @Bean
        JwtTokenFilter jwtTokenFilter() {
            return Mockito.mock(JwtTokenFilter.class);
        }
    }
}
