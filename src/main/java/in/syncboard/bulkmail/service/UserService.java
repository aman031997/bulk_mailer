package in.syncboard.bulkmail.service;

import in.syncboard.bulkmail.dto.UserDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {

    Mono<UserDTO> createUser(UserDTO userDTO);

    Mono<UserDTO> getUserById(Long id);

    Mono<UserDTO> getUserByUsername(String username);

    Mono<UserDTO> getUserByEmail(String email);

    Flux<UserDTO> getAllUsers();

    Mono<UserDTO> updateUser(Long id, UserDTO userDTO);

    Mono<Void> deleteUser(Long id);

    Mono<UserDTO> addRoleToUser(Long userId, String roleName);

    Mono<UserDTO> removeRoleFromUser(Long userId, String roleName);
}
