package in.syncboard.bulkmail.service.impl;

import in.syncboard.bulkmail.entity.UserEntity;
import in.syncboard.bulkmail.repository.UserRepository;
import in.syncboard.bulkmail.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> createUserDetails(user, roles)));
    }

    public Mono<UserDetails> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> createUserDetails(user, roles)));
    }

    private UserDetails createUserDetails(UserEntity user, List<String> roles) {
        return new SecurityUser(
                user.getUserId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getAccountNonExpired(),
                user.getAccountNonLocked(),
                user.getCredentialsNonExpired(),
                user.getEnabled(),
                roles
        );
    }
}
