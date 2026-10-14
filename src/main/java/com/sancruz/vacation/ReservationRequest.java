
package com.sancruz.vacation;

import jakarta.validation.constraints.*;

public record ReservationRequest(

    @NotNull(message = "Customer ID is required")
    @Positive
    Long customerId,

    @NotNull(message = "Package ID is required")
    @Positive
    Long packageId,

    @NotNull(message = "Number of travelers is required")
    @Min(value = 1, message = "At least one traveler is required")
    Integer travelers

) {
}
