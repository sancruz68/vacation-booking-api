
package com.sancruz.vacation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository
        extends JpaRepository<Reservation, Long> {

    List<Reservation> findAllByOrderByIdAsc();

    @Query("""
        SELECT COALESCE(SUM(r.travelers), 0)
        FROM Reservation r
        WHERE r.vacationPackage.id = :packageId
        AND r.status = :status
        """)
    Long sumTravelers(
            @Param("packageId") Long packageId,
            @Param("status") ReservationStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(
            @Param("id") Long id
    );

    boolean existsByCustomer_Id(Long customerId);

    boolean existsByVacationPackage_Id(Long packageId);
}
