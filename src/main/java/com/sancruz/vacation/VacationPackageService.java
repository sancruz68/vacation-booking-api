
package com.sancruz.vacation;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class VacationPackageService {

    private final VacationPackageRepository repository;
    private final ReservationRepository reservationRepository;
    private final ExcursionRepository excursionRepository;

    public VacationPackageService(
            VacationPackageRepository repository,
            ReservationRepository reservationRepository,
            ExcursionRepository excursionRepository) {

        this.repository = repository;
        this.reservationRepository = reservationRepository;
        this.excursionRepository = excursionRepository;
    }

    @Transactional(readOnly = true)
    public List<VacationPackage> getAllPackages() {
        return repository.findAll(
                Sort.by(Sort.Direction.ASC, "id")
        );
    }

    @Transactional(readOnly = true)
    public VacationPackage getPackage(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vacation package not found"
                ));
    }

    @Transactional
    public VacationPackage createPackage(
            VacationPackageRequest request) {

        validateDates(request);

        VacationPackage vacation = new VacationPackage();
        copyFields(vacation, request);

        return repository.save(vacation);
    }

    @Transactional
    public VacationPackage updatePackage(
            Long id,
            VacationPackageRequest request) {

        validateDates(request);

        // Lock the package so capacity cannot change
        // during a competing booking operation.
        VacationPackage vacation =
                repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vacation package not found"
                ));

        long confirmedTravelers =
                reservationRepository.sumTravelers(
                        id,
                        ReservationStatus.CONFIRMED
                );

        if (request.capacity() < confirmedTravelers) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Capacity cannot be lower than confirmed bookings"
            );
        }

        copyFields(vacation, request);

        return repository.save(vacation);
    }

    @Transactional
    public void deletePackage(Long id) {

        VacationPackage vacation =
                repository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vacation package not found"
                ));

        if (reservationRepository.existsByVacationPackage_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a package with reservation history"
            );
        }

        if (excursionRepository.existsByVacationPackage_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a package with associated excursions"
            );
        }

        repository.delete(vacation);
    }

    private void validateDates(VacationPackageRequest request) {

        if (request.startDate() != null
                && request.endDate() != null
                && request.endDate().isBefore(request.startDate())) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "End date cannot be before start date"
            );
        }
    }

    private void copyFields(
            VacationPackage vacation,
            VacationPackageRequest request) {

        vacation.setTitle(request.title().trim());
        vacation.setDestination(request.destination().trim());
        vacation.setDescription(request.description());
        vacation.setPrice(request.price());
        vacation.setCapacity(request.capacity());
        vacation.setStartDate(request.startDate());
        vacation.setEndDate(request.endDate());
    }
}
