package in.syncboard.bulkmail.service;

import in.syncboard.bulkmail.dto.ProfileDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProfileService {

    Flux<ProfileDTO> getAllProfiles();

    Mono<ProfileDTO> getProfileById(Long id);

    Mono<ProfileDTO> getProfileByEmail(String email);

    Flux<ProfileDTO> getProfilesByName(String name);

    Flux<ProfileDTO> getProfilesByCompany(String company);

    Flux<ProfileDTO> getProfilesByEmailDomain(String domain);

    Flux<ProfileDTO> getProfilesBySkill(String skill);

    Mono<ProfileDTO> createProfile(ProfileDTO profileDTO);

    Mono<ProfileDTO> updateProfile(Long id, ProfileDTO profileDTO);

    Mono<Void> deleteProfile(Long id);
}
