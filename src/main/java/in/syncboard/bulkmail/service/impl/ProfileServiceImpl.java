package in.syncboard.bulkmail.service.impl;

import in.syncboard.bulkmail.dto.ProfileDTO;
import in.syncboard.bulkmail.entity.ProfileEntity;
import in.syncboard.bulkmail.exception.ProfileException;
import in.syncboard.bulkmail.repository.ProfileRepository;
import in.syncboard.bulkmail.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final ModelMapper modelMapper;

    @Override
    public Flux<ProfileDTO> getAllProfiles() {
        log.debug("Getting all profiles");
        return profileRepository.findAll()
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ProfileDTO> getProfileById(Long id) {
        log.debug("Getting profile by id: {}", id);
        return profileRepository.findById(id)
                .switchIfEmpty(Mono.error(ProfileException.notFound("Profile not found with ID: " + id)))
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ProfileDTO> getProfileByEmail(String email) {
        log.debug("Getting profile by email: {}", email);
        return profileRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(ProfileException.notFound("Profile not found with email: " + email)))
                .map(this::mapToDTO);
    }

    @Override
    public Flux<ProfileDTO> getProfilesByName(String name) {
        log.debug("Getting profiles by name containing: {}", name);
        return profileRepository.findByNameContainingIgnoreCase(name)
                .map(this::mapToDTO);
    }

    @Override
    public Flux<ProfileDTO> getProfilesByCompany(String company) {
        log.debug("Getting profiles by company containing: {}", company);
        return profileRepository.findByCompanyContainingIgnoreCase(company)
                .map(this::mapToDTO);
    }

    @Override
    public Flux<ProfileDTO> getProfilesByEmailDomain(String domain) {
        String domainPattern = "%@" + domain;
        log.debug("Getting profiles by email domain: {}", domain);
        return profileRepository.findByEmailDomain(domainPattern)
                .map(this::mapToDTO);
    }

    @Override
    public Flux<ProfileDTO> getProfilesBySkill(String skill) {
        String skillPattern = "%" + skill + "%";
        log.debug("Getting profiles by skill: {}", skill);
        return profileRepository.findBySkill(skillPattern)
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ProfileDTO> createProfile(ProfileDTO profileDTO) {
        log.debug("Creating new profile with email: {}", profileDTO.getEmail());

        return profileRepository.findByEmail(profileDTO.getEmail())
                .flatMap(existingProfile -> Mono.error(
                        ProfileException.conflict("Profile with this email already exists: " + profileDTO.getEmail())))
                .then(Mono.defer(() -> {
                    ProfileEntity entity = mapToEntity(profileDTO);
                    return profileRepository.save(entity);
                }))
                .map(this::mapToDTO);
    }


    @Override
    public Mono<ProfileDTO> updateProfile(Long id, ProfileDTO profileDTO) {
        log.debug("Updating profile with id: {}", id);
        return profileRepository.findById(id)
                .switchIfEmpty(Mono.error(ProfileException.notFound("Profile not found with ID: " + id)))
                .flatMap(existingProfile -> {
                    // Check if we're changing the email and it already exists
                    if (!existingProfile.getEmail().equals(profileDTO.getEmail())) {
                        return profileRepository.findByEmail(profileDTO.getEmail())
                                .flatMap(p -> Mono.error(
                                        ProfileException.conflict("Profile with this email already exists: " + profileDTO.getEmail())))
                                .switchIfEmpty(Mono.defer(() -> updateEntity(existingProfile, profileDTO)))
                                .then(Mono.fromCallable(() -> existingProfile));
                    }
                    return updateEntity(existingProfile, profileDTO);
                })
                .flatMap(profileRepository::save)
                .map(this::mapToDTO);
    }

    @Override
    public Mono<Void> deleteProfile(Long id) {
        log.debug("Deleting profile with id: {}", id);
        return profileRepository.findById(id)
                .switchIfEmpty(Mono.error(ProfileException.notFound("Profile not found with ID: " + id)))
                .flatMap(profileRepository::delete);
    }

    private Mono<ProfileEntity> updateEntity(ProfileEntity entity, ProfileDTO dto) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setTitle(dto.getTitle());
        entity.setCompany(dto.getCompany());
        entity.setPhone(dto.getPhone());
        entity.setLinkedin(dto.getLinkedin());
        entity.setSkills(dto.getSkills());
        entity.setResumeUrl(dto.getResumeUrl());
        return Mono.just(entity);
    }

    private ProfileDTO mapToDTO(ProfileEntity entity) {
        return modelMapper.map(entity, ProfileDTO.class);
    }

    private ProfileEntity mapToEntity(ProfileDTO dto) {
        return modelMapper.map(dto, ProfileEntity.class);
    }
}
