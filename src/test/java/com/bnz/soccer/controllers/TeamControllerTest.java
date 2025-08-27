package com.bnz.soccer.controllers;

import com.bnz.soccer.data.entity.Team;
import com.bnz.soccer.exceptions.GlobalExceptionHandler;
import com.bnz.soccer.resources.record.TeamRequest;
import com.bnz.soccer.resources.record.TeamResponse;
import com.bnz.soccer.services.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TeamControllerTest {

    private MockMvc mockMvc;
    private TeamService teamService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        teamService = Mockito.mock(TeamService.class);
        TeamController controller = new TeamController(teamService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllTeams_returnsPagedList_withRealEntityFields() throws Exception {
        Team psg = new Team();
        psg.setId(1L);
        psg.setName("Paris Saint-Germain");
        psg.setAcronym("PSG");
        psg.setPlayers(new ArrayList<>());
        psg.setBudget(new BigDecimal("800000000"));

        Page<Team> page = new PageImpl<>(List.of(psg), PageRequest.of(0, 5), 1);

        Mockito.when(teamService.findWithAllPlayers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/teams")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortBy", "name")
                        .param("sortDirection", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Paris Saint-Germain"))
                .andExpect(jsonPath("$.content[0].acronym").value("PSG"))
                .andExpect(jsonPath("$.content[0].players").isArray())
                .andExpect(jsonPath("$.content[0].budget").value(800000000))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void createTeam_withValidData_returnsCreatedTeam() throws Exception {
        TeamRequest requestBody = new TeamRequest(
                "Paris Saint-Germain",
                "PSG",
                new BigDecimal("800000000"),
                new ArrayList<>()
        );

        TeamResponse persisted = new TeamResponse(
                1L,
                "Paris Saint-Germain",
                "PSG",
                new BigDecimal("800000000"),
                new ArrayList<>()
        );

        Mockito.when(teamService.addTeam(Mockito.any())).thenReturn(persisted);

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Paris Saint-Germain"))
                .andExpect(jsonPath("$.acronym").value("PSG"))
                .andExpect(jsonPath("$.budget").value(800000000));
    }


    @Test
    void createTeam_withEmptyName_returnsBadRequest() throws Exception {
        TeamRequest requestBody = new TeamRequest(
                "", // empty name
                "PSG",
                new BigDecimal("800000000"),
                new ArrayList<>()
        );

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void createTeam_withNegativeBudget_returnsBadRequest() throws Exception {
        Team requestBody = new Team();
        requestBody.setName("Lyon");
        requestBody.setAcronym("OL");
        requestBody.setBudget(new BigDecimal("-1000"));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllTeams_withEmptyResult_returnsEmptyList() throws Exception {
        Page<Team> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 5), 0);

        Mockito.when(teamService.findWithAllPlayers(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/api/teams")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void createTeam_withMissingAcronym_returnsBadRequest() throws Exception {
        Team requestBody = new Team();
        requestBody.setName("Marseille");
        requestBody.setAcronym(null); // invalide
        requestBody.setBudget(new BigDecimal("50000000"));

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isBadRequest());
    }
}
