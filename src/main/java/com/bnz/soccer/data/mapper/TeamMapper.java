package com.bnz.soccer.data.mapper;

import com.bnz.soccer.data.entity.Team;
import com.bnz.soccer.resources.record.TeamRequest;
import com.bnz.soccer.resources.record.TeamResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { PlayerMapper.class })
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "players", source = "players")
    Team toEntity(TeamRequest dto);

    TeamResponse toResponse(Team entity);
}
