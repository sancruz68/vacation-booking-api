
package com.sancruz.vacation;

import java.math.BigDecimal;

public record ExcursionResponse(
    Long id,
    Long packageId,
    String name,
    String description,
    BigDecimal price,
    Integer durationMinutes
) {

    public static ExcursionResponse from(Excursion excursion) {
        return new ExcursionResponse(
            excursion.getId(),
            excursion.getVacationPackage().getId(),
            excursion.getName(),
            excursion.getDescription(),
            excursion.getPrice(),
            excursion.getDurationMinutes()
        );
    }
}
