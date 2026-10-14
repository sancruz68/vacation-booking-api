
package com.sancruz.vacation;

import jakarta.validation.constraints.*;

public record CustomerRequest(

    @NotBlank(message = "First name is required")
    @Size(max = 80)
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 80)
    String lastName,

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(max = 180)
    String email,

    @Size(max = 30)
    String phone

) {
}
