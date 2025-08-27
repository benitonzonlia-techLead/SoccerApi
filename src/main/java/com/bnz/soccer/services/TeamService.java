package com.bnz.soccer.services;

        import com.bnz.soccer.data.entity.Team;
        import com.bnz.soccer.resources.record.TeamRequest;
        import com.bnz.soccer.resources.record.TeamPartialUpdateRequest;
        import com.bnz.soccer.resources.record.TeamResponse;
        import org.springframework.data.domain.Page;

        import java.math.BigDecimal;

public interface TeamService {

    Page<Team> findWithAllPlayers(Integer pageNumber,
                                  Integer pageSize,
                                  String sortBy,
                                  String sortDirection);

    TeamResponse addTeam(TeamRequest teamRequested);

    Page<Team> filterTeams(String name, BigDecimal minBudget);

    Team updateTeamPartially(Long id, TeamPartialUpdateRequest partialUpdate);

    Team updateTeamFully(Long id, TeamRequest teamRequest);

    void deleteTeam(Long id);
}
