
package com.sancruz.vacation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages/{packageId}/excursions")
public class ExcursionController {

    private final ExcursionService service;

    public ExcursionController(ExcursionService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExcursionResponse> getAll(
            @PathVariable Long packageId) {
        return service.getAll(packageId);
    }

    @GetMapping("/{id}")
    public ExcursionResponse getOne(
            @PathVariable Long packageId,
            @PathVariable Long id) {
        return service.getOne(packageId, id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExcursionResponse create(
            @PathVariable Long packageId,
            @Valid @RequestBody ExcursionRequest request) {
        return service.create(packageId, request);
    }

    @PutMapping("/{id}")
    public ExcursionResponse update(
            @PathVariable Long packageId,
            @PathVariable Long id,
            @Valid @RequestBody ExcursionRequest request) {
        return service.update(packageId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long packageId,
            @PathVariable Long id) {
        service.delete(packageId, id);
    }
}
