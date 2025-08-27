package com.bnz.soccer.resources.record;

import com.bnz.soccer.resources.enums.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlayerRequest(
        @NotBlank String name,
        @NotNull Position position
) {}