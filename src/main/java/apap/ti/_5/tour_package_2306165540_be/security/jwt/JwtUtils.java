package apap.ti._5.tour_package_2306165540_be.security.jwt;

import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${security.jwt.jwtSecret}")
    private String jwtSecret;

    @Value("${security.jwt.jwtExpirationMs}")
    private long jwtExpirationMs;

    private final EndUserRepository endUserRepository;

    public JwtUtils(EndUserRepository endUserRepository) {
        this.endUserRepository = endUserRepository;
    }

    public String generateToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        String username = userPrincipal.getUsername();

        Collection<? extends GrantedAuthority> authorities = userPrincipal.getAuthorities();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Ambil data user untuk klaim tambahan (id, name, email, role tanpa prefix)
        Optional<EndUser> userOpt = endUserRepository.findByUsernameIgnoreCase(username);
        String userId = null;
        String fullName = null;
        String email = null;
        String role = null;
        if (userOpt.isPresent()) {
            EndUser u = userOpt.get();
            userId = u.getId() != null ? u.getId().toString() : null;
            fullName = u.getFullName();
            email = u.getEmail();
            role = u.getRoleType() != null ? u.getRoleType().name() : null;
        }

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .claim("userId", userId)
                .claim("name", fullName)
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = parseClaims(token);
        return claims.get("roles", List.class);
    }

    public String getUserIdFromToken(String token) {
        Object v = parseClaims(token).get("userId");
        return v != null ? v.toString() : null;
    }

    public String getNameFromToken(String token) {
        Object v = parseClaims(token).get("name");
        return v != null ? v.toString() : null;
    }

    public String getEmailFromToken(String token) {
        Object v = parseClaims(token).get("email");
        return v != null ? v.toString() : null;
    }

    public String getRoleFromToken(String token) {
        Object v = parseClaims(token).get("role");
        return v != null ? v.toString() : null;
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUsernameFromToken(token);
        return username.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public Instant getExpirationInstant(String token) {
        return parseClaims(token).getExpiration().toInstant();
    }

    private boolean isTokenExpired(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = Arrays.copyOf(keyBytes, 32);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
}