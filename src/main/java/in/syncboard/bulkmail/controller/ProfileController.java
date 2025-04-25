package in.syncboard.bulkmail.controller;

import in.syncboard.bulkmail.dto.APIResponse;
import in.syncboard.bulkmail.dto.ProfileDTO;
import in.syncboard.bulkmail.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile Management API", description = "APIs for managing candidate profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    @Operation(summary = "Get all profiles", description = "Retrieves all candidate profiles in the system")
    public Mono<ResponseEntity<APIResponse<List<ProfileDTO>>>> getAllProfiles() {
        return profileService.getAllProfiles()
                .collectList()
                .map(profiles -> ResponseEntity.ok(
                        APIResponse.<List<ProfileDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profiles retrieved successfully")
                                .data(profiles)
                                .build()
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get profile by ID", description = "Retrieves a specific profile by its ID")
    public Mono<ResponseEntity<APIResponse<ProfileDTO>>> getProfileById(@PathVariable Long id) {
        return profileService.getProfileById(id)
                .map(profile -> ResponseEntity.ok(
                        APIResponse.<ProfileDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profile retrieved successfully")
                                .data(profile)
                                .build()
                ));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get profile by email", description = "Retrieves a profile by their email address")
    public Mono<ResponseEntity<APIResponse<ProfileDTO>>> getProfileByEmail(@PathVariable String email) {
        return profileService.getProfileByEmail(email)
                .map(profile -> ResponseEntity.ok(
                        APIResponse.<ProfileDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profile retrieved successfully")
                                .data(profile)
                                .build()
                ));
    }

    @GetMapping("/search/name")
    @Operation(summary = "Search profiles by name", description = "Searches profiles that contain the given name string")
    public Mono<ResponseEntity<APIResponse<List<ProfileDTO>>>> searchProfilesByName(
            @RequestParam String name) {
        return profileService.getProfilesByName(name)
                .collectList()
                .map(profiles -> ResponseEntity.ok(
                        APIResponse.<List<ProfileDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profiles retrieved successfully")
                                .data(profiles)
                                .build()
                ));
    }

    @GetMapping("/search/company")
    @Operation(summary = "Search profiles by company", description = "Searches profiles that are associated with the given company")
    public Mono<ResponseEntity<APIResponse<List<ProfileDTO>>>> searchProfilesByCompany(
            @RequestParam String company) {
        return profileService.getProfilesByCompany(company)
                .collectList()
                .map(profiles -> ResponseEntity.ok(
                        APIResponse.<List<ProfileDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profiles retrieved successfully")
                                .data(profiles)
                                .build()
                ));
    }

    @GetMapping("/search/domain")
    @Operation(summary = "Search profiles by email domain", description = "Searches profiles that have emails with the specified domain")
    public Mono<ResponseEntity<APIResponse<List<ProfileDTO>>>> searchProfilesByDomain(
            @RequestParam String domain) {
        return profileService.getProfilesByEmailDomain(domain)
                .collectList()
                .map(profiles -> ResponseEntity.ok(
                        APIResponse.<List<ProfileDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profiles retrieved successfully")
                                .data(profiles)
                                .build()
                ));
    }

    @GetMapping("/search/skill")
    @Operation(summary = "Search profiles by skill", description = "Searches profiles that have the specified skill")
    public Mono<ResponseEntity<APIResponse<List<ProfileDTO>>>> searchProfilesBySkill(
            @RequestParam String skill) {
        return profileService.getProfilesBySkill(skill)
                .collectList()
                .map(profiles -> ResponseEntity.ok(
                        APIResponse.<List<ProfileDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profiles retrieved successfully")
                                .data(profiles)
                                .build()
                ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create new profile", description = "Creates a new candidate profile")
    public Mono<ResponseEntity<APIResponse<ProfileDTO>>> createProfile(@Valid @RequestBody ProfileDTO profileDTO) {
        return profileService.createProfile(profileDTO)
                .map(createdProfile -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(APIResponse.<ProfileDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.CREATED.value())
                                .message("Profile created successfully")
                                .data(createdProfile)
                                .build()
                        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update profile", description = "Updates an existing profile by ID")
    public Mono<ResponseEntity<APIResponse<ProfileDTO>>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody ProfileDTO profileDTO) {
        return profileService.updateProfile(id, profileDTO)
                .map(updatedProfile -> ResponseEntity.ok(
                        APIResponse.<ProfileDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profile updated successfully")
                                .data(updatedProfile)
                                .build()
                ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete profile", description = "Deletes a profile by ID")
    public Mono<ResponseEntity<APIResponse<Void>>> deleteProfile(@PathVariable Long id) {
        return profileService.deleteProfile(id)
                .then(Mono.just(ResponseEntity.ok(
                        APIResponse.<Void>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Profile deleted successfully")
                                .build()
                )));
    }
}
