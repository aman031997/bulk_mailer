package in.syncboard.bulkmail.controller;

import in.syncboard.bulkmail.dto.APIResponse;
import in.syncboard.bulkmail.dto.TemplateDTO;
import in.syncboard.bulkmail.service.TemplateService;
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
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
@Tag(name = "Email Template API", description = "APIs for managing email templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(summary = "Get all templates", description = "Retrieves all email templates")
    public Mono<ResponseEntity<APIResponse<List<TemplateDTO>>>> getAllTemplates() {
        return templateService.getAllTemplates()
                .collectList()
                .map(templates -> ResponseEntity.ok(
                        APIResponse.<List<TemplateDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Templates retrieved successfully")
                                .data(templates)
                                .build()
                ));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active templates", description = "Retrieves all active email templates")
    public Mono<ResponseEntity<APIResponse<List<TemplateDTO>>>> getAllActiveTemplates() {
        return templateService.getAllActiveTemplates()
                .collectList()
                .map(templates -> ResponseEntity.ok(
                        APIResponse.<List<TemplateDTO>>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Active templates retrieved successfully")
                                .data(templates)
                                .build()
                ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get template by ID", description = "Retrieves a template by its ID")
    public Mono<ResponseEntity<APIResponse<TemplateDTO>>> getTemplateById(@PathVariable Long id) {
        return templateService.getTemplateById(id)
                .map(template -> ResponseEntity.ok(
                        APIResponse.<TemplateDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Template retrieved successfully")
                                .data(template)
                                .build()
                ));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get template by name", description = "Retrieves a template by its name")
    public Mono<ResponseEntity<APIResponse<TemplateDTO>>> getTemplateByName(@PathVariable String name) {
        return templateService.getTemplateByName(name)
                .map(template -> ResponseEntity.ok(
                        APIResponse.<TemplateDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Template retrieved successfully")
                                .data(template)
                                .build()
                ));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create template", description = "Creates a new email template")
    public Mono<ResponseEntity<APIResponse<TemplateDTO>>> createTemplate(@Valid @RequestBody TemplateDTO templateDTO) {
        return templateService.createTemplate(templateDTO)
                .map(createdTemplate -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(APIResponse.<TemplateDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.CREATED.value())
                                .message("Template created successfully")
                                .data(createdTemplate)
                                .build()
                        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update template", description = "Updates an existing email template")
    public Mono<ResponseEntity<APIResponse<TemplateDTO>>> updateTemplate(
            @PathVariable Long id,
            @Valid @RequestBody TemplateDTO templateDTO) {
        return templateService.updateTemplate(id, templateDTO)
                .map(updatedTemplate -> ResponseEntity.ok(
                        APIResponse.<TemplateDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Template updated successfully")
                                .data(updatedTemplate)
                                .build()
                ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete template", description = "Deletes an email template")
    public Mono<ResponseEntity<APIResponse<Void>>> deleteTemplate(@PathVariable Long id) {
        return templateService.deleteTemplate(id)
                .then(Mono.just(ResponseEntity.ok(
                        APIResponse.<Void>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Template deleted successfully")
                                .build()
                )));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate template", description = "Activates an email template")
    public Mono<ResponseEntity<APIResponse<TemplateDTO>>> activateTemplate(@PathVariable Long id) {
        return templateService.activateTemplate(id)
                .map(template -> ResponseEntity.ok(
                        APIResponse.<TemplateDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Template activated successfully")
                                .data(template)
                                .build()
                ));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate template", description = "Deactivates an email template")
    public Mono<ResponseEntity<APIResponse<TemplateDTO>>> deactivateTemplate(@PathVariable Long id) {
        return templateService.deactivateTemplate(id)
                .map(template -> ResponseEntity.ok(
                        APIResponse.<TemplateDTO>builder()
                                .success(true)
                                .statusCode(HttpStatus.OK.value())
                                .message("Template deactivated successfully")
                                .data(template)
                                .build()
                ));
    }
}
