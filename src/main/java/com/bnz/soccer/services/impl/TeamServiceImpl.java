package com.bnz.soccer.services.impl;

import com.bnz.soccer.data.entity.Player;
import com.bnz.soccer.data.entity.Team;
import com.bnz.soccer.data.mapper.TeamMapper;
import com.bnz.soccer.data.repository.PlayerRepository;
import com.bnz.soccer.data.repository.TeamRepository;
import com.bnz.soccer.exceptions.TeamNotFoundException;
import com.bnz.soccer.resources.record.PlayerRequest;
import com.bnz.soccer.resources.record.TeamPartialUpdateRequest;
import com.bnz.soccer.resources.record.TeamRequest;
import com.bnz.soccer.resources.record.TeamResponse;
import com.bnz.soccer.services.TeamService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.bnz.soccer.data.specifications.TeamSpecification.*;

/**
 * Service implementation for managing {@link Team} entities.
 */
@Service
public class TeamServiceImpl implements TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamServiceImpl.class);

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamMapper teamMapper;

    public TeamServiceImpl(TeamRepository teamRepository, PlayerRepository playerRepository, TeamMapper teamMapper) {
        this.teamRepository = teamRepository;
        this.playerRepository = playerRepository;
        this.teamMapper = teamMapper;
    }

    @Override
    public Page<Team> findWithAllPlayers(Integer pageNumber,
                                         Integer pageSize,
                                         String sortBy,
                                         String sortDirection) {
        log.info("Fetching teams with players - page={}, size={}, sortBy={}, sortDirection={}",
                pageNumber, pageSize, sortBy, sortDirection);

        // Determine sorting order dynamically
        Sort sort = sortDirection.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Repository call (fetches players eagerly if mapping is configured)
        Page<Team> result = teamRepository.findAll(pageable);

        log.info("Found {} teams", result.getTotalElements());
        return result;
    }

    @Override
    public TeamResponse addTeam(TeamRequest teamRequested) {
        log.info("Adding new team: {}", teamRequested);

        // Basic null/blank validation
        if (teamRequested.name() == null || teamRequested.name().isBlank()
                || teamRequested.acronym() == null || teamRequested.acronym().isBlank()
                || teamRequested.budget() == null) {
            log.info("Team creation failed - missing mandatory fields");
            throw new IllegalArgumentException("Every field is required");
        }

        // Map DTO to entity
        Team team = teamMapper.toEntity(teamRequested);

        // Link each player back to this team entity
        team.getPlayers().forEach(player -> player.setTeam(team));

        // Persist the new team
        Team saved = teamRepository.save(team);
        log.info("Team created with ID={}", saved.getId());

        // Map entity to response DTO
        return teamMapper.toResponse(saved);
    }


    @Override
    public Page<Team> filterTeams(String name, BigDecimal minBudget) {
        log.info("Filtering teams - name={}, minBudget={}", name, minBudget);

        // Build dynamic specification based on provided filters
        Specification<Team> spec = fetchPlayers()
                .and(nameContainsIgnoreCase(name))
                .and(budgetGreaterThanOrEqualTo(minBudget));

        // Default pagination for filter results
        Page<Team> result = teamRepository.findAll(spec, PageRequest.of(0, 10));

        log.info("Found {} teams after filter", result.getTotalElements());
        return result;
    }

    @Override
    public Team updateTeamPartially(Long id, TeamPartialUpdateRequest partialUpdate) {
        log.info("Partially updating team ID={} with data: {}", id, partialUpdate);

        // Retrieve existing team or throw if missing
        Team existing = teamRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Team with ID={} not found for partial update", id);
                    return new TeamNotFoundException(id);
                });

        // Update only provided fields
        if (partialUpdate.name() != null && !partialUpdate.name().isBlank()) {
            existing.setName(partialUpdate.name());
        }
        if (partialUpdate.budget() != null) {
            existing.setBudget(partialUpdate.budget());
        }

        // Persist updated entity
        Team updated = teamRepository.save(existing);
        log.info("Team ID={} partially updated", updated.getId());
        return updated;
    }

    @Transactional
    public Team updateTeamFully(Long teamId, TeamRequest request) {
        // 1. Load the existing team from the DB
        Team existingTeam = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        // 2. Update the team's simple attributes
        existingTeam.setName(request.name());
        existingTeam.setAcronym(request.acronym());
        existingTeam.setBudget(request.budget());

        // Explicitly remove players first
        playerRepository.deleteAll(existingTeam.getPlayers());
        existingTeam.getPlayers().clear();

        // Flush to force delete statements now
        teamRepository.flush();

        // 4. Create new Player entities from the request data
        for (PlayerRequest p : request.players()) {
            Player newPlayer = new Player();
            newPlayer.setName(p.name());
            newPlayer.setPosition(p.position());
            newPlayer.setTeam(existingTeam);
            existingTeam.getPlayers().add(newPlayer);
        }

        // 5. Save the team (cascade will handle players)
        return teamRepository.save(existingTeam);
    }



    @Override
    public void deleteTeam(Long id) {
        log.info("Deleting team ID={}", id);

        // Check existence before attempting deletion
        if (!teamRepository.existsById(id)) {
            log.info("Team with ID={} not found for deletion", id);
            throw new TeamNotFoundException(id);
        }

        teamRepository.deleteById(id);
        log.info("Team ID={} deleted successfully", id);
    }
}
