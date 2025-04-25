package in.syncboard.bulkmail.service.impl;

import in.syncboard.bulkmail.dto.AuthRequestDTO;
import in.syncboard.bulkmail.dto.AuthResponseDTO;
import in.syncboard.bulkmail.dto.RegisterRequestDTO;
import in.syncboard.bulkmail.dto.UserDTO;
import in.syncboard.bulkmail.entity.UserEntity;
import in.syncboard.bulkmail.entity.UserRoleEntity;
import in.syncboard.bulkmail.exception.ProfileException;
import in.syncboard.bulkmail.repository.RoleRepository;
import in.syncboard.bulkmail.repository.UserRepository;
import in.syncboard.bulkmail.repository.UserRoleRepository;
import in.syncboard.bulkmail.service.AuthService;
import in.syncboard.bulkmail.service.UserService;
import in.syncboard.bulkmail.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    public Mono<AuthResponseDTO> login(AuthRequestDTO authRequest) {
        log.debug("Login attempt for user: {}", authRequest.getUsernameOrEmail());

        // Check if the input is email or username
        Mono<UserEntity> userEntityMono;
        if (authRequest.getUsernameOrEmail().contains("@")) {
            userEntityMono = userRepository.findByEmail(authRequest.getUsernameOrEmail());
        } else {
            userEntityMono = userRepository.findByUsername(authRequest.getUsernameOrEmail());
        }

        return userEntityMono
                .switchIfEmpty(Mono.error(new ProfileException("Invalid username/email or password", HttpStatus.UNAUTHORIZED)))
                .flatMap(user -> {
                    if (!user.getEnabled()) {
                        return Mono.error(new ProfileException("Account is disabled", HttpStatus.FORBIDDEN));
                    }

                    if (!passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                        return Mono.error(new ProfileException("Invalid username/email or password", HttpStatus.UNAUTHORIZED));
                    }

                    // Update last login time
                    user.setLastLogin(LocalDateTime.now());
                    return userRepository.save(user)
                            .flatMap(updatedUser -> userRoleRepository.findRoleNamesByUserId(updatedUser.getUserId())
                                    .collectList()
                                    .map(roles -> {
                                        UserDTO userDTO = UserDTO.builder()
                                                .userId(updatedUser.getUserId())
                                                .username(updatedUser.getUsername())
                                                .email(updatedUser.getEmail())
                                                .phone(updatedUser.getPhone())
                                                .powerLevel(updatedUser.getPowerLevel())
                                                .roles(roles)
                                                .build();

                                        String accessToken = jwtUtil.generateToken(userDTO, roles);
                                        String refreshToken = jwtUtil.generateRefreshToken(userDTO);

                                        return AuthResponseDTO.builder()
                                                .tokenType("Bearer")
                                                .accessToken(accessToken)
                                                .refreshToken(refreshToken)
                                                .expiresIn(3600L)
                                                .userId(updatedUser.getUserId())
                                                .username(updatedUser.getUsername())
                                                .email(updatedUser.getEmail())
                                                .roles(roles)
                                                .powerLevel(updatedUser.getPowerLevel())
                                                .isEmailVerified(true) // You'd have actual verification logic here
                                                .build();
                                    }));
                });
    }

    @Override
    public Mono<UserDTO> register(RegisterRequestDTO registerRequest) {
        log.debug("Registration attempt for username: {}", registerRequest.getUsername());

        // First check for existing username
        return userRepository.findByUsername(registerRequest.getUsername())
                .flatMap(existingUser -> Mono.error(new ProfileException("Username already exists", HttpStatus.CONFLICT)))
                .switchIfEmpty(
                        // Then check for existing email
                        userRepository.findByEmail(registerRequest.getEmail())
                                .flatMap(existingUser -> Mono.error(new ProfileException("Email already exists", HttpStatus.CONFLICT)))
                                .switchIfEmpty(
                                        // If neither exists, create the new user
                                        Mono.defer(() -> {
                                            UserEntity newUser = UserEntity.builder()
                                                    .username(registerRequest.getUsername())
                                                    .email(registerRequest.getEmail())
                                                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                                                    .phone(registerRequest.getPhone())
                                                    .powerLevel(1) // Default power level for new users
                                                    .enabled(true)
                                                    .accountNonExpired(true)
                                                    .accountNonLocked(true)
                                                    .credentialsNonExpired(true)
                                                    .createdTs(LocalDateTime.now())
                                                    .build();

                                            return userRepository.save(newUser);
                                        })
                                )
                )
                .cast(UserEntity.class) // Cast to UserEntity
                .flatMap(savedUser -> roleRepository.findByName("USER")
                        .flatMap(role -> {
                            UserRoleEntity userRole = UserRoleEntity.builder()
                                    .userId(savedUser.getUserId())
                                    .roleId(role.getRoleId())
                                    .createdTs(LocalDateTime.now())
                                    .build();
                            return userRoleRepository.save(userRole).thenReturn(savedUser);
                        })
                        .switchIfEmpty(Mono.just(savedUser))
                )
                .cast(UserEntity.class) // Cast to UserEntity
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = UserDTO.builder()
                                    .userId(user.getUserId())
                                    .username(user.getUsername())
                                    .email(user.getEmail())
                                    .phone(user.getPhone())
                                    .powerLevel(user.getPowerLevel())
                                    .enabled(user.getEnabled())
                                    .createdTs(user.getCreatedTs())
                                    .roles(roles)
                                    .build();
                            return dto;
                        })
                );
    }

    @Override
    public Mono<AuthResponseDTO> refreshToken(String refreshToken) {
        log.debug("Processing refresh token request");

        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            return Mono.error(new ProfileException("Invalid refresh token", HttpStatus.UNAUTHORIZED));
        }

        String username = jwtUtil.extractUsername(refreshToken);
        Long userId = jwtUtil.extractClaim(refreshToken, claims -> claims.get("userId", Long.class));

        return userService.getUserById(userId)
                .switchIfEmpty(Mono.error(new ProfileException("User not found", HttpStatus.NOT_FOUND)))
                .flatMap(userDTO -> {
                    if (!userDTO.getEnabled()) {
                        return Mono.error(new ProfileException("Account is disabled", HttpStatus.FORBIDDEN));
                    }

                    String newAccessToken = jwtUtil.generateToken(userDTO, userDTO.getRoles());
                    String newRefreshToken = jwtUtil.generateRefreshToken(userDTO);

                    return Mono.just(AuthResponseDTO.builder()
                            .tokenType("Bearer")
                            .accessToken(newAccessToken)
                            .refreshToken(newRefreshToken)
                            .expiresIn(3600L)
                            .userId(userDTO.getUserId())
                            .username(userDTO.getUsername())
                            .email(userDTO.getEmail())
                            .roles(userDTO.getRoles())
                            .powerLevel(userDTO.getPowerLevel())
                            .isEmailVerified(true) // You'd have actual verification logic here
                            .build());
                });
    }

    @Override
    public Mono<Void> logout(String token) {
        // In a more complete implementation, you would add the token to a blacklist
        // or update a user's session in the database
        log.debug("Logging out user");
        return Mono.empty();
    }

    @Override
    public Mono<Void> changePassword(Long userId, String currentPassword, String newPassword) {
        log.debug("Password change request for user ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ProfileException("User not found", HttpStatus.NOT_FOUND)))
                .flatMap(user -> {
                    if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                        return Mono.error(new ProfileException("Current password is incorrect", HttpStatus.BAD_REQUEST));
                    }

                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setUpdatedTs(LocalDateTime.now());
                    return userRepository.save(user).then();
                });
    }
}
