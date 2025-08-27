package com.bnz.soccer.resources.record;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TeamPartialUpdateRequest(
        @Size(min = 2, message = "Le nom doit contenir au moins 2 caractères")
        String name,

        @Min(value = 0, message = "Le budget doit être positif")
        BigDecimal budget
) {
}