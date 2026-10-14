
package com.sancruz.vacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VacationPackageServiceTest {

    @Mock
    private VacationPackageRepository repository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private ExcursionRepository excursionRepository;

    @InjectMocks
    private VacationPackageService service;

    private VacationPackage vacationPackage;

    @BeforeEach
    void setUp() {
        vacationPackage = new VacationPackage();
        vacationPackage.setTitle("Puerto Rico Explorer");
        vacationPackage.setDestination("San Juan, Puerto Rico");
        vacationPackage.setPrice(new BigDecimal("1199.99"));
        vacationPackage.setCapacity(10);
        vacationPackage.setStartDate(LocalDate.of(2027, 3, 10));
        vacationPackage.setEndDate(LocalDate.of(2027, 3, 17));
    }

    private VacationPackageRequest validRequest(int capacity) {
        return new VacationPackageRequest(
            "Puerto Rico Explorer",
            "San Juan, Puerto Rico",
            "Seven-night vacation package",
            new BigDecimal("1199.99"),
            capacity,
            LocalDate.of(2027, 3, 10),
            LocalDate.of(2027, 3, 17)
        );
    }

    @Test
    void shouldCreateValidVacationPackage() {
        when(repository.save(any(VacationPackage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        VacationPackage created =
            service.createPackage(validRequest(10));

        assertEquals("Puerto Rico Explorer", created.getTitle());
        assertEquals(10, created.getCapacity());

        verify(repository).save(any(VacationPackage.class));
    }

    @Test
    void shouldRejectInvalidDateRange() {
        VacationPackageRequest request =
            new VacationPackageRequest(
                "Puerto Rico Explorer",
                "San Juan, Puerto Rico",
                "Invalid dates",
                new BigDecimal("1199.99"),
                10,
                LocalDate.of(2027, 3, 17),
                LocalDate.of(2027, 3, 10)
            );

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createPackage(request)
        );

        assertEquals(HttpStatus.BAD_REQUEST,
            exception.getStatusCode());

        verify(repository, never())
            .save(any(VacationPackage.class));
    }

    @Test
    void shouldRejectCapacityBelowConfirmedBookings() {
        when(repository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(vacationPackage));

        when(reservationRepository.sumTravelers(
            1L, ReservationStatus.CONFIRMED))
            .thenReturn(10L);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.updatePackage(1L, validRequest(9))
        );

        assertEquals(HttpStatus.CONFLICT,
            exception.getStatusCode());

        assertEquals(10, vacationPackage.getCapacity());

        verify(repository, never())
            .save(any(VacationPackage.class));
    }

    @Test
    void shouldAllowValidCapacityIncrease() {
        when(repository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(vacationPackage));

        when(reservationRepository.sumTravelers(
            1L, ReservationStatus.CONFIRMED))
            .thenReturn(10L);

        when(repository.save(vacationPackage))
            .thenReturn(vacationPackage);

        VacationPackage updated =
            service.updatePackage(1L, validRequest(12));

        assertEquals(12, updated.getCapacity());
        verify(repository).save(vacationPackage);
    }

    @Test
    void shouldPreventDeletingPackageWithReservations() {
        when(repository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(vacationPackage));

        when(reservationRepository.existsByVacationPackage_Id(1L))
            .thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deletePackage(1L)
        );

        assertEquals(HttpStatus.CONFLICT,
            exception.getStatusCode());

        verify(repository, never())
            .delete(any(VacationPackage.class));
    }

    @Test
    void shouldPreventDeletingPackageWithExcursions() {
        when(repository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(vacationPackage));

        when(excursionRepository.existsByVacationPackage_Id(1L))
            .thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deletePackage(1L)
        );

        assertEquals(HttpStatus.CONFLICT,
            exception.getStatusCode());

        verify(repository, never())
            .delete(any(VacationPackage.class));
    }
}
