
package com.sancruz.vacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VacationPackageRepository packageRepository;

    @InjectMocks
    private ReservationService reservationService;

    private Customer customer;
    private VacationPackage vacationPackage;

    @BeforeEach
    void setUp() {

        customer = new Customer();
        ReflectionTestUtils.setField(customer, "id", 1L);
        customer.setFirstName("Alex");
        customer.setLastName("Rivera");

        vacationPackage = new VacationPackage();
        ReflectionTestUtils.setField(
                vacationPackage, "id", 1L
        );
        vacationPackage.setTitle("Puerto Rico Explorer");
        vacationPackage.setCapacity(10);
    }

    @Test
    void shouldCreateReservationWhenSeatsAvailable() {

        when(packageRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(vacationPackage));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(reservationRepository.sumTravelers(
                1L, ReservationStatus.CONFIRMED))
                .thenReturn(2L);

        when(reservationRepository.saveAndFlush(
                any(Reservation.class)))
                .thenAnswer(invocation -> {
                    Reservation saved = invocation.getArgument(0);
                    ReflectionTestUtils.setField(saved, "id", 2L);
                    return saved;
                });

        ReservationRequest request =
                new ReservationRequest(1L, 1L, 3);

        ReservationResponse result =
                reservationService.create(request);

        assertEquals(3, result.travelers());
        assertEquals(
                ReservationStatus.CONFIRMED,
                result.status()
        );

        verify(reservationRepository)
                .saveAndFlush(any(Reservation.class));
    }

    @Test
    void shouldRejectOverbooking() {

        when(packageRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(vacationPackage));

        when(customerRepository.findById(1L))
                .thenReturn(Optional.of(customer));

        when(reservationRepository.sumTravelers(
                1L, ReservationStatus.CONFIRMED))
                .thenReturn(10L);

        ReservationRequest request =
                new ReservationRequest(1L, 1L, 1);

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> reservationService.create(request)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(reservationRepository, never())
                .saveAndFlush(any(Reservation.class));
    }

    @Test
    void shouldCancelConfirmedReservation() {

        Reservation reservation = new Reservation();

        ReflectionTestUtils.setField(
                reservation, "id", 1L
        );

        reservation.setCustomer(customer);
        reservation.setVacationPackage(vacationPackage);
        reservation.setTravelers(2);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        ReservationResponse result =
                reservationService.cancel(1L);

        assertEquals(
                ReservationStatus.CANCELLED,
                result.status()
        );

        assertEquals(
                ReservationStatus.CANCELLED,
                reservation.getStatus()
        );
    }

    @Test
    void shouldRejectCancellingAlreadyCancelledReservation() {

        Reservation reservation = new Reservation();

        reservation.setStatus(ReservationStatus.CANCELLED);

        when(reservationRepository.findByIdForUpdate(1L))
                .thenReturn(Optional.of(reservation));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> reservationService.cancel(1L)
                );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
    }
}
