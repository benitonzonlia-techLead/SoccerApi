package com.bnz.soccer.exceptions;

public class TeamNotFoundException extends RuntimeException {
    public TeamNotFoundException(Long id) {
        super("Team not found with id " + id);
    }
}
