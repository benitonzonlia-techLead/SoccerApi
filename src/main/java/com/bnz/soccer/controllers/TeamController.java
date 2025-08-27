package com.bnz.soccer.controllers;

import com.bnz.soccer.data.entity.Team;
import com.bnz.soccer.resources.record.TeamPartialUpdateRequest;
import com.bnz.soccer.resources.record.TeamRequest;
import com.bnz.soccer.resources.record.TeamResponse;
import com.bnz.soccer.services.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/teams")
@Tag(name = "Teams", description = "Operations related to team management")
public class TeamController {

    private static final Logger log = LoggerFactory.getLogger(TeamController.class);

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @Operation(
            summary = "Get all teams with their players",
            description = "Returns a paginated list of all teams including their players",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of teams",
                            content = @Content(mediaType = "application/json"))
            }
    )
    @GetMapping
    public ResponseEntity<Page<Team>> getAllTeamsWithPlayers(
            @Parameter(description = "Page number (zero-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") @Min(1) int size,
            @Parameter(description = "Field to sort by") @RequestParam(defaultValue = "name") String sortBy,
            @Parameter(description = "Sort direction: asc or desc") @RequestParam(defaultValue = "asc") String direction
    ) {
        log.info("Fetching all teams with players - page={}, size={}, sortBy={}, direction={}", page, size, sortBy, direction);
        Page<Team> teams = teamService.findWithAllPlayers(page, size, sortBy, direction);
        log.info("Returned {} teams", teams.getTotalElements());
        return ResponseEntity.ok(teams);
    }

    @Operation(
            summary = "Filter teams",
            description = "Returns teams filtered by name and/or minimum budget",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Filtered list of teams")
            }
    )
    @GetMapping("/filter")
    public ResponseEntity<Page<Team>> filterTeams(
            @Parameter(description = "Partial or full team name (case-insensitive)") @RequestParam(required = false) String name,
            @Parameter(description = "Minimum budget") @RequestParam(required = false) BigDecimal minBudget
    ) {
        log.info("Filtering teams - name={}, minBudget={}", name, minBudget);
        Page<Team> teams = teamService.filterTeams(name, minBudget);
        log.info("Returned {} teams after filtering", teams.getTotalElements());
        return ResponseEntity.ok(teams);
    }

    @Operation(
            summary = "Create a new team",
            description = "Persists a new team with provided name, acronym, and budget",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Team created successfully",
                            content = @Content(mediaType = "application/json")),
                    @ApiResponse(responseCode = "400", description = "Invalid request")
            }
    )
    @PostMapping
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        log.info("Creating new team: {}", request);
        TeamResponse created = teamService.addTeam(request);
        log.info("Team created with ID={}", created.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Partially update a team",
            description = "Updates certain fields of an existing team (e.g., name, budget)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @PatchMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<Team> updateTeamPartially(
            @Parameter(description = "ID of the team to update") @PathVariable Long id,
            @Valid @RequestBody TeamPartialUpdateRequest team
    ) {
        log.info("Partially updating team ID={} with data: {}", id, team);
        Team updated = teamService.updateTeamPartially(id, team);
        log.info("Team ID={} partially updated", id);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Fully update a team",
            description = "Replaces all data of an existing team with new values",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team fully updated"),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @PutMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<Team> updateTeamFully(
            @Parameter(description = "ID of the team to update") @PathVariable Long id,
            @RequestBody TeamRequest team
    ) {
        log.info("Fully updating team ID={} with data: {}", id, team);
        Team updated = teamService.updateTeamFully(id, team);
        log.info("Team ID={} fully updated", id);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete a team",
            description = "Deletes a team by its ID",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Team deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "basicAuth")
    public ResponseEntity<Void> deleteTeam(
            @Parameter(description = "ID of the team to delete") @PathVariable Long id
    ) {
        log.info("Deleting team ID={}", id);
        teamService.deleteTeam(id);
        log.info("Team ID={} deleted", id);
        return ResponseEntity.noContent().build();
    }
}
