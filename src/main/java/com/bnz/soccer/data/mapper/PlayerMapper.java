package com.bnz.soccer.data.mapper;

import com.bnz.soccer.data.entity.Player;
import com.bnz.soccer.resources.record.PlayerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    Player toEntity(PlayerRequest record);

}
