package in.syncboard.bulkmail.service;

import in.syncboard.bulkmail.dto.AuthRequestDTO;
import in.syncboard.bulkmail.dto.AuthResponseDTO;
import in.syncboard.bulkmail.dto.RegisterRequestDTO;
import in.syncboard.bulkmail.dto.UserDTO;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<AuthResponseDTO> login(AuthRequestDTO authRequest);

    Mono<UserDTO> register(RegisterRequestDTO registerRequest);

    Mono<AuthResponseDTO> refreshToken(String refreshToken);

    Mono<Void> logout(String token);

    Mono<Void> changePassword(Long userId, String currentPassword, String newPassword);
}
