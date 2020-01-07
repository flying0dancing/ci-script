package com.lombardrisk.ignis.server.security;

import com.lombardrisk.ignis.server.user.UserService;
import com.lombardrisk.ignis.server.user.model.SecUser;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Order(100)
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    public UserDetailsServiceImpl(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) {
        SecUser secUser = userService.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Unable to find user with username ["
                        + username
                        + "]"));
        return secUser.toSpringSecurityUser();
    }
}
