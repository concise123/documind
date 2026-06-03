package my.documind.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import my.documind.common.exception.ErrorMessage;
import my.documind.domain.User;
import my.documind.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("loadUserByUsername - email: " + email);

        User user = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new UsernameNotFoundException(ErrorMessage.USER_NOT_FOUND.getMessage()));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .build();
    }
}
