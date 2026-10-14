
package com.sancruz.vacation;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/packages")
public class VacationPackageController {

    private final VacationPackageService service;

    public VacationPackageController(
            VacationPackageService service) {
        this.service = service;
    }

    @GetMapping
    public List<VacationPackage> getAllPackages() {
        return service.getAllPackages();
    }

    @GetMapping("/{id}")
    public VacationPackage getPackage(
            @PathVariable Long id) {
        return service.getPackage(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VacationPackage createPackage(
            @Valid @RequestBody VacationPackageRequest request) {
        return service.createPackage(request);
    }

    @PutMapping("/{id}")
    public VacationPackage updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody VacationPackageRequest request) {
        return service.updatePackage(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePackage(@PathVariable Long id) {
        service.deletePackage(id);
    }
}
