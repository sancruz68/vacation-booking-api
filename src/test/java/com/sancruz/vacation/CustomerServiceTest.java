
package com.sancruz.vacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CustomerService service;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setFirstName("Alex");
        customer.setLastName("Rivera");
        customer.setEmail("alex.rivera@example.com");
        customer.setPhone("202-555-0148");
    }

    private CustomerRequest validRequest(String email) {
        return new CustomerRequest(
            "Alex",
            "Rivera",
            email,
            "202-555-0148"
        );
    }

    @Test
    void shouldCreateCustomerAndNormalizeEmail() {

        when(repository.save(any(Customer.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0));

        Customer created = service.createCustomer(
            validRequest("  ALEX.RIVERA@Example.Com  ")
        );

        assertEquals(
            "alex.rivera@example.com",
            created.getEmail()
        );

        assertEquals("Alex", created.getFirstName());

        verify(repository).existsByEmailIgnoreCase(
            "alex.rivera@example.com"
        );

        verify(repository).save(any(Customer.class));
    }

    @Test
    void shouldRejectDuplicateEmail() {

        when(repository.existsByEmailIgnoreCase(
            "alex.rivera@example.com"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.createCustomer(
                validRequest("alex.rivera@example.com")
            )
        );

        assertEquals(
            HttpStatus.CONFLICT,
            exception.getStatusCode()
        );

        verify(repository, never())
            .save(any(Customer.class));
    }

    @Test
    void shouldRejectMissingCustomer() {

        when(repository.findById(99L))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.getCustomer(99L)
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );
    }

    @Test
    void shouldUpdateExistingCustomer() {

        when(repository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(repository.save(customer))
            .thenReturn(customer);

        CustomerRequest request = new CustomerRequest(
            "Alexandra",
            "Rivera",
            " ALEX.RIVERA@EXAMPLE.COM ",
            "202-555-0199"
        );

        Customer updated =
            service.updateCustomer(1L, request);

        assertEquals(
            "Alexandra",
            updated.getFirstName()
        );

        assertEquals(
            "alex.rivera@example.com",
            updated.getEmail()
        );

        assertEquals(
            "202-555-0199",
            updated.getPhone()
        );

        verify(repository).save(customer);
    }

    @Test
    void shouldRejectDuplicateEmailDuringUpdate() {

        when(repository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(repository.existsByEmailIgnoreCase(
            "another@example.com"
        )).thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.updateCustomer(
                1L,
                validRequest("another@example.com")
            )
        );

        assertEquals(
            HttpStatus.CONFLICT,
            exception.getStatusCode()
        );

        verify(repository, never())
            .save(any(Customer.class));
    }

    @Test
    void shouldPreventDeletingCustomerWithReservations() {

        when(repository.findById(1L))
            .thenReturn(Optional.of(customer));

        when(reservationRepository.existsByCustomer_Id(1L))
            .thenReturn(true);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.deleteCustomer(1L)
        );

        assertEquals(
            HttpStatus.CONFLICT,
            exception.getStatusCode()
        );

        verify(repository, never())
            .delete(any(Customer.class));
    }

    @Test
    void shouldDeleteCustomerWithoutReservations() {

        when(repository.findById(1L))
            .thenReturn(Optional.of(customer));

        service.deleteCustomer(1L);

        verify(reservationRepository)
            .existsByCustomer_Id(1L);

        verify(repository).delete(customer);
    }
}
