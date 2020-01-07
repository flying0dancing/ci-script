package com.lombardrisk.ignis.server.user.model;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Used for changing password.
 */
@Getter
@Setter
public class SecUserPassword {

    @NotNull
    @Size(min = 6)
    private String oldPassword;
    @NotNull
    @Size(min = 6)
    private String newPassword;
}
