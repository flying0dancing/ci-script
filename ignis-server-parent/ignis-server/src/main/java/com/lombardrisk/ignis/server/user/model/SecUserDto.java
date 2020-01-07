package com.lombardrisk.ignis.server.user.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class SecUserDto implements Comparable<SecUserDto> {

    private Long id;
    private String username;

    public static SecUserDto from(SecUser user) {
        SecUserDto dto = new SecUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    @Override
    public int compareTo(SecUserDto o) {
        return username.toUpperCase().compareTo(o.getUsername().toUpperCase());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SecUserDto that = (SecUserDto) o;
        return Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
