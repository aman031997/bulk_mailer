package in.syncboard.bulkmail.service;

import in.syncboard.bulkmail.dto.TemplateDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TemplateService {

    Flux<TemplateDTO> getAllTemplates();

    Flux<TemplateDTO> getAllActiveTemplates();

    Mono<TemplateDTO> getTemplateById(Long id);

    Mono<TemplateDTO> getTemplateByName(String name);

    Mono<TemplateDTO> createTemplate(TemplateDTO templateDTO);

    Mono<TemplateDTO> updateTemplate(Long id, TemplateDTO templateDTO);

    Mono<Void> deleteTemplate(Long id);

    Mono<TemplateDTO> activateTemplate(Long id);

    Mono<TemplateDTO> deactivateTemplate(Long id);
}
