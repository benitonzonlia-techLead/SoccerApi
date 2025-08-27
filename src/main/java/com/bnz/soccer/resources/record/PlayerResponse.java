package com.bnz.soccer.resources.record;

import com.bnz.soccer.resources.enums.Position;

public record PlayerResponse(
        Long id,
        String name,
        Position position
) {}
