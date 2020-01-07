package com.lombardrisk.ignis.server.user.model;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SEC_USER")
@Builder
public class SecUser {

    public static final String ROLE_USER = "USER";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME")
    @NotNull
    @Size(min = 5, max = 255)
    private String username;

    @Column(name = "PASSWORD")
    @NotNull
    @Size(min = 6)
    private String password;

    public SecUser(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    public User toSpringSecurityUser() {
        List<? extends GrantedAuthority> authorities = ImmutableList.of();
        return new User(username, password, authorities);
    }
}
