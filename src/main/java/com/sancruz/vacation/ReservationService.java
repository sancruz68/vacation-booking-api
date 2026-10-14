
package com.sancruz.vacation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final CustomerRepository customerRepository;
    private final VacationPackageRepository packageRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            CustomerRepository customerRepository,
            VacationPackageRepository packageRepository) {

        this.reservationRepository = reservationRepository;
        this.customerRepository = customerRepository;
        this.packageRepository = packageRepository;
    }

    @Transactional(readOnly = true)
    public List<ReservationResponse> getAll() {

        return reservationRepository.findAllByOrderByIdAsc()
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservationResponse getOne(Long id) {

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found"
                ));

        return ReservationResponse.from(reservation);
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request) {

        // Lock the package before checking available capacity.
        VacationPackage vacationPackage =
                packageRepository.findByIdForUpdate(request.packageId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Vacation package not found"
                ));

        Customer customer =
                customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));

        long bookedTravelers = reservationRepository.sumTravelers(
                vacationPackage.getId(),
                ReservationStatus.CONFIRMED
        );

        long availableSeats =
                vacationPackage.getCapacity() - bookedTravelers;

        if (request.travelers() > availableSeats) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Not enough available seats"
            );
        }

        Reservation reservation = new Reservation();
        reservation.setCustomer(customer);
        reservation.setVacationPackage(vacationPackage);
        reservation.setTravelers(request.travelers());
        reservation.setStatus(ReservationStatus.CONFIRMED);

        return ReservationResponse.from(
                reservationRepository.saveAndFlush(reservation)
        );
    }

    @Transactional
    public ReservationResponse cancel(Long id) {

        Reservation reservation =
                reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Reservation not found"
                ));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Reservation is already cancelled"
            );
        }

        reservation.setStatus(ReservationStatus.CANCELLED);

        return ReservationResponse.from(reservation);
    }
}
