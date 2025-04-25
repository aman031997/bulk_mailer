package in.syncboard.bulkmail.controller;

import in.syncboard.bulkmail.dto.APIResponse;
import in.syncboard.bulkmail.dto.UserDTO;
import in.syncboard.bulkmail.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User API", description = "APIs for managing users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Returns a list of all users (Admin only)")
    public Mono<ResponseEntity<APIResponse<List<UserDTO>>>> getAllUsers() {
        return userService.getAllUsers()
                .collectList()
                .map(users -> ResponseEntity.ok(
                        APIResponse.<List<UserDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Users retrieved successfully")
                                .data(users)
                                .build()
                ));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER') and (authentication.name == @userService.getUserById(#id).block().username or hasRole('ADMIN'))")
    @Operation(summary = "Get user by ID", description = "Returns user details by ID (Admin or same user only)")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("User retrieved successfully")
                                .data(user)
                                .build()
                ));
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user by username", description = "Returns user details by username (Admin only)")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> getUserByUsername(@PathVariable String username) {
        return userService.getUserByUsername(username)
                .map(user -> ResponseEntity.ok(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("User retrieved successfully")
                                .data(user)
                                .build()
                ));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create user", description = "Creates a new user (Admin only)")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> createUser(@Valid @RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO)
                .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.CREATED.value())
                                .message("User created successfully")
                                .data(user)
                                .build()
                ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER') and (authentication.name == @userService.getUserById(#id).block().username or hasRole('ADMIN'))")
    @Operation(summary = "Update user", description = "Updates a user (Admin or same user only)")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO)
                .map(user -> ResponseEntity.ok(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("User updated successfully")
                                .data(user)
                                .build()
                ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete user", description = "Deletes a user (Admin only)")
    public Mono<ResponseEntity<APIResponse<Void>>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ResponseEntity.ok(
                        APIResponse.<Void>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("User deleted successfully")
                                .build()
                )));
    }

    @PostMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Add role to user", description = "Adds a role to a user (Admin only)")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> addRoleToUser(
            @PathVariable Long id,
            @PathVariable String roleName) {
        return userService.addRoleToUser(id, roleName)
                .map(user -> ResponseEntity.ok(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Role added successfully")
                                .data(user)
                                .build()
                ));
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Remove role from user", description = "Removes a role from a user (Admin only)")
    public Mono<ResponseEntity<APIResponse<UserDTO>>> removeRoleFromUser(
            @PathVariable Long id,
            @PathVariable String roleName) {
        return userService.removeRoleFromUser(id, roleName)
                .map(user -> ResponseEntity.ok(
                        APIResponse.<UserDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Role removed successfully")
                                .data(user)
                                .build()
                ));
    }
}
