package com.bnz.soccer.services;

import com.bnz.soccer.data.entity.Team;
import com.bnz.soccer.data.mapper.PlayerMapperImpl;
import com.bnz.soccer.data.mapper.TeamMapper;
import com.bnz.soccer.data.mapper.TeamMapperImpl;
import com.bnz.soccer.data.repository.PlayerRepository;
import com.bnz.soccer.data.repository.TeamRepository;
import com.bnz.soccer.resources.enums.Position;
import com.bnz.soccer.resources.record.PlayerRequest;
import com.bnz.soccer.resources.record.TeamRequest;
import com.bnz.soccer.resources.record.TeamResponse;
import com.bnz.soccer.services.impl.TeamServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;
    @Mock
    private PlayerRepository playerRepository;

    private TeamMapper teamMapper;
    private TeamServiceImpl teamService;

    @BeforeEach
    void setUp() {
        PlayerMapperImpl realPlayerMapper = new PlayerMapperImpl();
        TeamMapperImpl realTeamMapper = new TeamMapperImpl();

        ReflectionTestUtils.setField(realTeamMapper, "playerMapper", realPlayerMapper);

        teamMapper = realTeamMapper;

        teamService = new TeamServiceImpl(teamRepository, playerRepository, teamMapper);
    }

    @Test
    void addTeam_withValidRecord_savesAndReturnsTeam() {
        TeamRequest dto = new TeamRequest(
                "Paris Saint-Germain",
                "PSG",
                new BigDecimal("800000000"),
                List.of(new PlayerRequest("Kylian Mbappe", Position.FORWARD))
        );

        Team saved = new Team();
        saved.setId(1L);
        saved.setName("Paris Saint-Germain");
        saved.setAcronym("PSG");
        saved.setPlayers(new ArrayList<>());
        saved.setBudget(new BigDecimal("800000000"));

        when(teamRepository.save(any(Team.class))).thenReturn(saved);

        TeamResponse result = teamService.addTeam(dto);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Paris Saint-Germain");
        assertThat(result.acronym()).isEqualTo("PSG");
        verify(teamRepository).save(any(Team.class));
    }


    @Test
    void addTeam_withEmptyName_throwsException() {
        TeamRequest dto = new TeamRequest(
                "",
                "PSG",
                new BigDecimal("800000000"),
                null
        );

        assertThatThrownBy(() -> teamService.addTeam(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Every field is required");

        verifyNoInteractions(teamRepository);
    }


    @Test
    void addTeam_mapsDtoToEntity_andSaves() {
        TeamRequest dto = new TeamRequest(
                "Paris Saint-Germain",
                "PSG",
                new BigDecimal("800000000"),
                List.of(new PlayerRequest("Kylian Mbappe", Position.FORWARD))
        );

        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> {
            Team t = inv.getArgument(0);
            t.setId(42L);
            return t;
        });

        TeamResponse result = teamService.addTeam(dto);

        assertThat(result.name()).isEqualTo("Paris Saint-Germain");
        assertThat(result.players()).hasSize(1);
        assertThat(result.players().get(0).name()).isEqualTo("Kylian Mbappe");

        verify(teamRepository).save(any(Team.class));
    }

    @Test
    void filterTeams_withNameAndBudget_returnsFilteredPage() {
        Team t = new Team();
        t.setName("OM");
        when(teamRepository.findAll(
                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Team>>any(),
                any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(t)));

        Page<Team> result = teamService.filterTeams("OM", BigDecimal.valueOf(10_000_000));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("OM");
        verify(teamRepository).findAll(
                ArgumentMatchers.<org.springframework.data.jpa.domain.Specification<Team>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void updateTeamPartially_withExistingTeam_updatesFields() {
        Team existing = new Team();
        existing.setId(1L);
        existing.setName("Old Name");
        existing.setBudget(BigDecimal.ONE);

        when(teamRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = new com.bnz.soccer.resources.record.TeamPartialUpdateRequest("New Name", BigDecimal.TEN);
        Team result = teamService.updateTeamPartially(1L, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getBudget()).isEqualTo(BigDecimal.TEN);
    }

    @Test
    void updateTeamPartially_withNonExistingTeam_throwsNotFound() {
        when(teamRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                teamService.updateTeamPartially(99L,
                        new com.bnz.soccer.resources.record.TeamPartialUpdateRequest("Name", BigDecimal.ONE)))
                .isInstanceOf(com.bnz.soccer.exceptions.TeamNotFoundException.class);
    }

    @Test
    void updateTeamFully_withExistingTeam_replacesAllFields() {
        Team existing = new Team();
        existing.setId(1L);
        existing.setName("Old Name");

        when(teamRepository.findById(1L)).thenReturn(java.util.Optional.of(existing));
        when(teamRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TeamRequest dto = new TeamRequest("New Name", "NEW", BigDecimal.valueOf(1000), new ArrayList<>());
        Team result = teamService.updateTeamFully(1L, dto);

        assertThat(result.getName()).isEqualTo("New Name");
        assertThat(result.getAcronym()).isEqualTo("NEW");
    }

    @Test
    void updateTeamFully_withNonExistingTeam_throwsNotFound() {
        when(teamRepository.findById(99L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() ->
                teamService.updateTeamFully(99L,
                        new TeamRequest("x", "y", BigDecimal.ONE, new ArrayList<>())))
                .isInstanceOf(com.bnz.soccer.exceptions.TeamNotFoundException.class);
    }

    @Test
    void deleteTeam_withExistingId_deletesTeam() {
        when(teamRepository.existsById(1L)).thenReturn(true);

        teamService.deleteTeam(1L);

        verify(teamRepository).deleteById(1L);
    }

    @Test
    void deleteTeam_withNonExistingId_throwsNotFound() {
        when(teamRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> teamService.deleteTeam(1L))
                .isInstanceOf(com.bnz.soccer.exceptions.TeamNotFoundException.class);
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.CsvSource({
            "0,5,name,asc",
            "1,10,budget,desc"
    })
    void findWithAllPlayers_parameterized(int page, int size, String sortBy, String direction) {
        Team t = new Team();
        t.setName("Test Team");

        Page<Team> mockPage = new PageImpl<>(List.of(t));
        when(teamRepository.findAll(any(PageRequest.class))).thenReturn(mockPage);

        Page<Team> result = teamService.findWithAllPlayers(page, size, sortBy, direction);

        assertThat(result.getContent()).hasSize(1);
        verify(teamRepository).findAll(
                PageRequest.of(page, size,
                        direction.equalsIgnoreCase("desc")
                                ? Sort.by(sortBy).descending()
                                : Sort.by(sortBy).ascending())
        );
    }

}
