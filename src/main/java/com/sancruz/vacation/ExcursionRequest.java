
package com.sancruz.vacation;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ExcursionRequest(

    @NotBlank(message = "Excursion name is required")
    @Size(max = 120)
    String name,

    @Size(max = 1000)
    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00",
        message = "Price cannot be negative")
    @Digits(integer = 8, fraction = 2)
    BigDecimal price,

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    Integer durationMinutes

) {
}
