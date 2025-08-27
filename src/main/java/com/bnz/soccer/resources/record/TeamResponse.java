package com.bnz.soccer.resources.record;

import java.math.BigDecimal;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        String acronym,
        BigDecimal budget,
        List<PlayerResponse> players
) {}
