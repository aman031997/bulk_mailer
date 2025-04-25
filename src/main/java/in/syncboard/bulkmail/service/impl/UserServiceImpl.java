package in.syncboard.bulkmail.service.impl;

import in.syncboard.bulkmail.dto.UserDTO;
import in.syncboard.bulkmail.entity.RoleEntity;
import in.syncboard.bulkmail.entity.UserEntity;
import in.syncboard.bulkmail.entity.UserRoleEntity;
import in.syncboard.bulkmail.exception.ProfileException;
import in.syncboard.bulkmail.repository.RoleRepository;
import in.syncboard.bulkmail.repository.UserRepository;
import in.syncboard.bulkmail.repository.UserRoleRepository;
import in.syncboard.bulkmail.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserDTO> createUser(UserDTO userDTO) {
        log.debug("Creating new user with username: {}", userDTO.getUsername());

        String encodedPassword = passwordEncoder.encode("defaultPassword"); // You'd get this from the DTO in a real app

        // First check for existing username
        return userRepository.findByUsername(userDTO.getUsername())
                .flatMap(existingUser -> Mono.error(new ProfileException("Username already exists", HttpStatus.CONFLICT)))
                .switchIfEmpty(
                        // Then check for existing email
                        userRepository.findByEmail(userDTO.getEmail())
                                .flatMap(existingUser -> Mono.error(new ProfileException("Email already exists", HttpStatus.CONFLICT)))
                                .switchIfEmpty(
                                        // If neither exists, create the new user
                                        Mono.defer(() -> {
                                            UserEntity userEntity = modelMapper.map(userDTO, UserEntity.class);
                                            userEntity.setPassword(encodedPassword);
                                            userEntity.setEnabled(true);
                                            userEntity.setAccountNonExpired(true);
                                            userEntity.setAccountNonLocked(true);
                                            userEntity.setCredentialsNonExpired(true);
                                            userEntity.setCreatedTs(LocalDateTime.now());
                                            userEntity.setPowerLevel(1); // Default power level

                                            return userRepository.save(userEntity);
                                        })
                                )
                )
                .cast(UserEntity.class) // Add cast to ensure correct type
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
                .cast(UserEntity.class) // Add cast to ensure correct type
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }


    @Override
    public Mono<UserDTO> getUserById(Long id) {
        log.debug("Getting user by ID: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }

    @Override
    public Mono<UserDTO> getUserByUsername(String username) {
        log.debug("Getting user by username: {}", username);
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with username: " + username, HttpStatus.NOT_FOUND)))
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }

    @Override
    public Mono<UserDTO> getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with email: " + email, HttpStatus.NOT_FOUND)))
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }

    @Override
    public Flux<UserDTO> getAllUsers() {
        log.debug("Getting all users");
        return userRepository.findAll()
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }

    @Override
    public Mono<UserDTO> updateUser(Long id, UserDTO userDTO) {
        log.debug("Updating user with ID: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .cast(UserEntity.class) // Cast to UserEntity
                .flatMap(existingUser -> {
                    // Update fields from DTO
                    if (userDTO.getUsername() != null && !userDTO.getUsername().equals(existingUser.getUsername())) {
                        return userRepository.findByUsername(userDTO.getUsername())
                                .flatMap(found -> Mono.error(new ProfileException("Username already exists", HttpStatus.CONFLICT)))
                                .switchIfEmpty(Mono.defer(() -> {
                                    existingUser.setUsername(userDTO.getUsername());
                                    return Mono.just(existingUser);
                                }));
                    }
                    return Mono.just(existingUser);
                })
                .cast(UserEntity.class) // Cast to UserEntity
                .flatMap(existingUser -> {
                    if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
                        return userRepository.findByEmail(userDTO.getEmail())
                                .flatMap(found -> Mono.error(new ProfileException("Email already exists", HttpStatus.CONFLICT)))
                                .switchIfEmpty(Mono.defer(() -> {
                                    existingUser.setEmail(userDTO.getEmail());
                                    return Mono.just(existingUser);
                                }));
                    }
                    return Mono.just(existingUser);
                })
                .cast(UserEntity.class) // Cast to UserEntity
                .flatMap(existingUser -> {
                    // Update other fields
                    if (userDTO.getPhone() != null) {
                        existingUser.setPhone(userDTO.getPhone());
                    }
                    if (userDTO.getPowerLevel() != null) {
                        existingUser.setPowerLevel(userDTO.getPowerLevel());
                    }
                    if (userDTO.getEnabled() != null) {
                        existingUser.setEnabled(userDTO.getEnabled());
                    }
                    existingUser.setUpdatedTs(LocalDateTime.now());

                    return userRepository.save(existingUser);
                })
                .cast(UserEntity.class) // Cast to UserEntity
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }


    @Override
    public Mono<Void> deleteUser(Long id) {
        log.debug("Deleting user with ID: {}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .flatMap(user -> userRoleRepository.findByUserId(user.getUserId())
                        .flatMap(userRoleRepository::delete)
                        .then(Mono.just(user))
                )
                .flatMap(userRepository::delete);
    }

    @Override
    public Mono<UserDTO> addRoleToUser(Long userId, String roleName) {
        log.debug("Adding role {} to user with ID: {}", roleName, userId);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with ID: " + userId, HttpStatus.NOT_FOUND)))
                .flatMap(user -> roleRepository.findByName(roleName)
                        .switchIfEmpty(Mono.error(new ProfileException("Role not found: " + roleName, HttpStatus.NOT_FOUND)))
                        .flatMap(role -> {
                            UserRoleEntity userRole = UserRoleEntity.builder()
                                    .userId(userId)
                                    .roleId(role.getRoleId())
                                    .createdTs(LocalDateTime.now())
                                    .build();
                            return userRoleRepository.save(userRole).thenReturn(user);
                        })
                )
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }

    @Override
    public Mono<UserDTO> removeRoleFromUser(Long userId, String roleName) {
        log.debug("Removing role {} from user with ID: {}", roleName, userId);
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new ProfileException("User not found with ID: " + userId, HttpStatus.NOT_FOUND)))
                .flatMap(user -> roleRepository.findByName(roleName)
                        .switchIfEmpty(Mono.error(new ProfileException("Role not found: " + roleName, HttpStatus.NOT_FOUND)))
                        .flatMap(role -> userRoleRepository.deleteByUserIdAndRoleId(userId, role.getRoleId())
                                .thenReturn(user))
                )
                .flatMap(user -> userRoleRepository.findRoleNamesByUserId(user.getUserId())
                        .collectList()
                        .map(roles -> {
                            UserDTO dto = modelMapper.map(user, UserDTO.class);
                            dto.setRoles(roles);
                            return dto;
                        })
                );
    }
}
