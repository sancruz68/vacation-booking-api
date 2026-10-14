
package com.sancruz.vacation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExcursionRepository
        extends JpaRepository<Excursion, Long> {

    List<Excursion> findByVacationPackage_IdOrderByIdAsc(
            Long packageId
    );

    Optional<Excursion> findByIdAndVacationPackage_Id(
            Long id,
            Long packageId
    );

    boolean existsByVacationPackage_Id(Long packageId);
}
