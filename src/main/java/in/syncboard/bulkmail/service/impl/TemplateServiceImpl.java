package in.syncboard.bulkmail.service.impl;

import in.syncboard.bulkmail.dto.TemplateDTO;
import in.syncboard.bulkmail.entity.TemplateEntity;
import in.syncboard.bulkmail.exception.ProfileException;
import in.syncboard.bulkmail.repository.TemplateRepository;
import in.syncboard.bulkmail.service.TemplateService;
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
public class TemplateServiceImpl implements TemplateService {

    private final TemplateRepository templateRepository;
    private final ModelMapper modelMapper;

    @Override
    public Flux<TemplateDTO> getAllTemplates() {
        log.debug("Getting all templates");
        return templateRepository.findAll()
                .map(this::mapToDTO);
    }

    @Override
    public Flux<TemplateDTO> getAllActiveTemplates() {
        log.debug("Getting all active templates");
        return templateRepository.findAllActive()
                .map(this::mapToDTO);
    }

    @Override
    public Mono<TemplateDTO> getTemplateById(Long id) {
        log.debug("Getting template by id: {}", id);
        return templateRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .map(this::mapToDTO);
    }

    @Override
    public Mono<TemplateDTO> getTemplateByName(String name) {
        log.debug("Getting template by name: {}", name);
        return templateRepository.findByName(name)
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with name: " + name, HttpStatus.NOT_FOUND)))
                .map(this::mapToDTO);
    }

    @Override
    public Mono<TemplateDTO> createTemplate(TemplateDTO templateDTO) {
        log.debug("Creating new template with name: {}", templateDTO.getName());
        return templateRepository.findByName(templateDTO.getName())
                .flatMap(existingTemplate -> Mono.error(
                        new ProfileException("Template with this name already exists: " + templateDTO.getName(), HttpStatus.CONFLICT)))
                .switchIfEmpty(Mono.defer(() -> {
                    TemplateEntity entity = mapToEntity(templateDTO);
                    if (entity.getIsActive() == null) {
                        entity.setIsActive(true); // Active by default
                    }
                    return templateRepository.save(entity);
                }))
                .ofType(TemplateEntity.class)
                .map(this::mapToDTO);
    }

    @Override
    public Mono<TemplateDTO> updateTemplate(Long id, TemplateDTO templateDTO) {
        log.debug("Updating template with id: {}", id);
        return templateRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .flatMap(existingTemplate -> {
                    if (!existingTemplate.getName().equals(templateDTO.getName())) {
                        return templateRepository.findByName(templateDTO.getName())
                                .flatMap(t -> Mono.error(
                                        new ProfileException("Template with this name already exists: " + templateDTO.getName(), HttpStatus.CONFLICT)))
                                .then(Mono.just(existingTemplate))
                                .switchIfEmpty(Mono.just(existingTemplate));
                    }
                    return Mono.just(existingTemplate);
                })
                .flatMap(existingTemplate -> {
                    updateEntityFields(existingTemplate, templateDTO);
                    return templateRepository.save(existingTemplate);
                })
                .map(this::mapToDTO);
    }

    @Override
    public Mono<Void> deleteTemplate(Long id) {
        log.debug("Deleting template with id: {}", id);
        return templateRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .flatMap(templateRepository::delete);
    }

    @Override
    public Mono<TemplateDTO> activateTemplate(Long id) {
        log.debug("Activating template with id: {}", id);
        return templateRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .flatMap(template -> {
                    template.setIsActive(true);
                    return templateRepository.save(template);
                })
                .map(this::mapToDTO);
    }

    @Override
    public Mono<TemplateDTO> deactivateTemplate(Long id) {
        log.debug("Deactivating template with id: {}", id);
        return templateRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProfileException("Template not found with ID: " + id, HttpStatus.NOT_FOUND)))
                .flatMap(template -> {
                    template.setIsActive(false);
                    return templateRepository.save(template);
                })
                .map(this::mapToDTO);
    }

    private void updateEntityFields(TemplateEntity entity, TemplateDTO dto) {
        entity.setName(dto.getName());
        entity.setSubject(dto.getSubject());
        entity.setContent(dto.getContent());
        entity.setDescription(dto.getDescription());
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }

    private TemplateDTO mapToDTO(TemplateEntity entity) {
        return modelMapper.map(entity, TemplateDTO.class);
    }

    private TemplateEntity mapToEntity(TemplateDTO dto) {
        return modelMapper.map(dto, TemplateEntity.class);
    }
}
