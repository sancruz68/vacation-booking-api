
package com.sancruz.vacation;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record VacationPackageRequest(

    @NotBlank(message = "Title is required")
    @Size(max = 120)
    String title,

    @NotBlank(message = "Destination is required")
    @Size(max = 120)
    String destination,

    @Size(max = 1000)
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    @Digits(integer = 8, fraction = 2)
    BigDecimal price,

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    Integer capacity,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    @NotNull(message = "End date is required")
    LocalDate endDate

) {
}
