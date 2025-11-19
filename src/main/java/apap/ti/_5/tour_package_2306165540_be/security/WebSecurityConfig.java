package apap.ti._5.tour_package_2306165540_be.security;

import apap.ti._5.tour_package_2306165540_be.security.jwt.JwtTokenFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    // ===================== JWT SECURITY (untuk /api/**) =====================
    @Bean
    @Order(1)
    public SecurityFilterChain jwtFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/**")
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/profile/roles").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        //TOUR PACKAGE MODULE
                        .requestMatchers("/api/packages").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        .requestMatchers("/api/packages/*").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        .requestMatchers("/api/packages/*/process").hasAuthority("CUSTOMER")
                        .requestMatchers("/api/packages/*/plans/**").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        
                        .requestMatchers("/api/plans").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        .requestMatchers("/api/plans/*").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        .requestMatchers("/api/plans/*/ordered-activities").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        
                        .requestMatchers("/api/activities").hasAnyAuthority("SUPERADMIN", "TOUR_PACKAGE_VENDOR", "FLIGHT_AIRLINE", "ACCOMMODATION_OWNER", "RENTAL_VENDOR", "CUSTOMER")
                        .requestMatchers("/api/activities/*").hasAnyAuthority("SUPERADMIN", "TOUR_PACKAGE_VENDOR", "FLIGHT_AIRLINE", "ACCOMMODATION_OWNER", "RENTAL_VENDOR")
                        .requestMatchers("/api/activities/create").hasAnyAuthority("SUPERADMIN", "TOUR_PACKAGE_VENDOR", "FLIGHT_AIRLINE", "ACCOMMODATION_OWNER", "RENTAL_VENDOR")
                        
                        .requestMatchers("/api/ordered-activities").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        .requestMatchers("/api/ordered-activities/*").hasAnyAuthority("SUPERADMIN", "CUSTOMER", "TOUR_PACKAGE_VENDOR")
                        
                        .requestMatchers("/api/statistics/**").hasAnyAuthority("SUPERADMIN", "TOUR_PACKAGE_VENDOR")


                        .anyRequest().authenticated())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler(new AccessDeniedHandler() {
                            @Override
                            public void handle(HttpServletRequest request, HttpServletResponse response,
                                    AccessDeniedException accessDeniedException)
                                    throws IOException, ServletException {
                                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                                response.getWriter().write("Anda Tidak Memiliki Akses ke Endpoint Ini!");
                            }
                        }));

        return http.build();
    }

    // ===================== WEB SECURITY =====================
    @Bean
    @Order(2)
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher(request -> !request.getRequestURI().startsWith("/api/"))
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/css/**").permitAll()
                        .requestMatchers("/js/**").permitAll()
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .defaultSuccessUrl("/"))
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login"))
                .exceptionHandling(handling -> handling
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendRedirect("/access-denied");
                        }));

        return http.build();
    }

    // ===================== AUTH MANAGER & PASSWORD ENCODER =====================
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
