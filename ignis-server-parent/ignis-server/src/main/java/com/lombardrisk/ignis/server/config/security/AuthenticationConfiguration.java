package com.lombardrisk.ignis.server.config.security;

import com.lombardrisk.ignis.server.controller.security.AuthenticationController;
import com.lombardrisk.ignis.server.controller.security.UserController;
import com.lombardrisk.ignis.server.user.UserRepository;
import com.lombardrisk.ignis.server.user.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

@Configuration
public class AuthenticationConfiguration {

    private final UserRepository userRepository;

    public AuthenticationConfiguration(final UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserService userService() {
        return new UserService(userRepository, passwordEncoder());
    }

    @Bean
    public AuthenticationController authenticationController() {
        return new AuthenticationController(userService(), new SecurityContextLogoutHandler());
    }

    @Bean
    public UserController userController() {
        return new UserController(userService());
    }
}
