
package com.sancruz.vacation;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final ReservationRepository reservationRepository;

    public CustomerService(
            CustomerRepository repository,
            ReservationRepository reservationRepository) {

        this.repository = repository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<Customer> getAllCustomers() {
        return repository.findAll(
                Sort.by("lastName", "firstName")
        );
    }

    @Transactional(readOnly = true)
    public Customer getCustomer(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Customer not found"
                ));
    }

    @Transactional
    public Customer createCustomer(CustomerRequest request) {

        String email = normalizeEmail(request.email());

        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer email already exists"
            );
        }

        Customer customer = new Customer();
        copyFields(customer, request, email);

        return repository.save(customer);
    }

    @Transactional
    public Customer updateCustomer(
            Long id,
            CustomerRequest request) {

        Customer customer = getCustomer(id);
        String email = normalizeEmail(request.email());

        if (!customer.getEmail().equalsIgnoreCase(email)
                && repository.existsByEmailIgnoreCase(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Customer email already exists"
            );
        }

        copyFields(customer, request, email);

        return repository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {

        Customer customer = getCustomer(id);

        if (reservationRepository.existsByCustomer_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a customer with reservation history"
            );
        }

        repository.delete(customer);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private void copyFields(
            Customer customer,
            CustomerRequest request,
            String email) {

        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setEmail(email);

        customer.setPhone(
                request.phone() == null
                        ? null
                        : request.phone().trim()
        );
    }
}
