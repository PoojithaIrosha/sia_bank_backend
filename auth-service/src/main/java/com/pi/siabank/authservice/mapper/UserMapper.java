package com.pi.siabank.authservice.mapper;

import com.pi.siabank.authservice.dto.RegisterUserDto;
import com.pi.siabank.authservice.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "id", ignore = true)
    User toUser(RegisterUserDto registerUserDto);
}
