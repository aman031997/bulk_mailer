package in.syncboard.bulkmail.controller;

import in.syncboard.bulkmail.dto.*;
import in.syncboard.bulkmail.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication API", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
    public Mono<ResponseEntity<APIResponse<AuthResponseDTO>>> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        return authService.login(authRequest)
                .map(authResponse -> ResponseEntity.ok(
                        APIResponse.<AuthResponseDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Login successful")
                                .data(authResponse)
                                .build()
                ));
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Registers a new user")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        return authService.register(registerRequest)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.CREATED.value())
                                .message("Registration successful")
                                .data(user)
                                .build()
                ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refreshes a JWT token")
    public Mono<ResponseEntity<APIResponse<AuthResponseDTO>>> refreshToken(@RequestBody TokenRefreshRequest refreshRequest) {
        return authService.refreshToken(refreshRequest.getRefreshToken())
                .map(authResponse -> ResponseEntity.ok(
                        APIResponse.<AuthResponseDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Token refreshed successfully")
                                .data(authResponse)
                                .build()
                ));
    }

    @PostMapping("/logout")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Logout", description = "Logs out a user")
    public Mono<ResponseEntity<APIResponse<Void>>> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return authService.logout(token)
                .then(Mono.just(ResponseEntity.ok(
                        APIResponse.<Void>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Logout successful")
                                .build()
                )));
    }

    @PostMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password", description = "Changes the password for a user")
    public Mono<ResponseEntity<APIResponse<Void>>> changePassword(
            @Valid @RequestBody PasswordChangeRequest passwordChangeRequest) {
        return authService.changePassword(
                        passwordChangeRequest.getUserId(),
                        passwordChangeRequest.getCurrentPassword(),
                        passwordChangeRequest.getNewPassword()
                )
                .then(Mono.just(ResponseEntity.ok(
                        APIResponse.<Void>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Password changed successfully")
                                .build()
                )));
    }
}
