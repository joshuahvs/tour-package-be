package apap.ti._5.tour_package_2306165540_be.security.service;

import apap.ti._5.tour_package_2306165540_be.model.profile.EndUser;
import apap.ti._5.tour_package_2306165540_be.repository.EndUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EndUserRepository endUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EndUser user = endUserRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User dengan username " + username + " tidak ditemukan"));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User " + username + " tidak aktif");
        }

        String password = user.getPassword();
        if (password == null) {
            throw new UsernameNotFoundException("User " + username + " belum memiliki password");
        }

        return User
                .withUsername(user.getUsername())
                .password(password)
                .authorities(new SimpleGrantedAuthority(user.getRoleType().name()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}
