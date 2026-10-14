
package com.sancruz.vacation;

import java.time.Instant;

public record ReservationResponse(
    Long id,
    Long customerId,
    Long packageId,
    String packageTitle,
    Integer travelers,
    ReservationStatus status,
    Instant createdAt
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getCustomer().getId(),
            reservation.getVacationPackage().getId(),
            reservation.getVacationPackage().getTitle(),
            reservation.getTravelers(),
            reservation.getStatus(),
            reservation.getCreatedAt()
        );
    }
}
