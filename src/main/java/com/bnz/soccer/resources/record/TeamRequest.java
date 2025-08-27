package com.bnz.soccer.resources.record;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record TeamRequest(
        @NotBlank String name,
        @NotBlank String acronym,
        @NotNull @Positive BigDecimal budget,
        List< @Valid PlayerRequest> players
) {}
