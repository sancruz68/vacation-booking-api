
package com.sancruz.vacation;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VacationPackageRepository
        extends JpaRepository<VacationPackage, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM VacationPackage p WHERE p.id = :id")
    Optional<VacationPackage> findByIdForUpdate(
        @Param("id") Long id
    );
}
