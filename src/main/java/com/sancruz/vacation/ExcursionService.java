
package com.sancruz.vacation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ExcursionService {

    private final ExcursionRepository excursionRepository;
    private final VacationPackageRepository packageRepository;

    public ExcursionService(
            ExcursionRepository excursionRepository,
            VacationPackageRepository packageRepository) {
        this.excursionRepository = excursionRepository;
        this.packageRepository = packageRepository;
    }

    @Transactional(readOnly = true)
    public List<ExcursionResponse> getAll(Long packageId) {

        if (!packageRepository.existsById(packageId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Vacation package not found"
            );
        }

        return excursionRepository
            .findByVacationPackage_IdOrderByIdAsc(packageId)
            .stream()
            .map(ExcursionResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public ExcursionResponse getOne(Long packageId, Long id) {
        return ExcursionResponse.from(findExcursion(packageId, id));
    }

    @Transactional
    public ExcursionResponse create(
            Long packageId,
            ExcursionRequest request) {

        VacationPackage vacationPackage =
            packageRepository.findById(packageId)
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Vacation package not found"
                ));

        Excursion excursion = new Excursion();
        excursion.setVacationPackage(vacationPackage);

        copyFields(excursion, request);

        return ExcursionResponse.from(
            excursionRepository.save(excursion)
        );
    }

    @Transactional
    public ExcursionResponse update(
            Long packageId,
            Long id,
            ExcursionRequest request) {

        Excursion excursion = findExcursion(packageId, id);

        copyFields(excursion, request);

        return ExcursionResponse.from(
            excursionRepository.save(excursion)
        );
    }

    @Transactional
    public void delete(Long packageId, Long id) {

        Excursion excursion = findExcursion(packageId, id);

        excursionRepository.delete(excursion);
    }

    private Excursion findExcursion(Long packageId, Long id) {
        return excursionRepository
            .findByIdAndVacationPackage_Id(id, packageId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Excursion not found for this package"
            ));
    }

    private void copyFields(
            Excursion excursion,
            ExcursionRequest request) {

        excursion.setName(request.name().trim());
        excursion.setDescription(request.description());
        excursion.setPrice(request.price());
        excursion.setDurationMinutes(request.durationMinutes());
    }
}
