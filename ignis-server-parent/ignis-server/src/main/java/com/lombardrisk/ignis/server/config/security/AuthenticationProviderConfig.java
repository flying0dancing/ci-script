package com.lombardrisk.ignis.server.config.security;

import com.lombardrisk.ignis.server.config.web.HttpConnectorConfiguration;
import com.lombardrisk.ignis.server.security.BasicAuthenticationEntryPoint;
import com.lombardrisk.ignis.server.security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class AuthenticationProviderConfig {

    private final HttpConnectorConfiguration httpConnectorConfiguration;
    private final AuthenticationConfiguration authenticationConfiguration;

    @Autowired
    public AuthenticationProviderConfig(
            final HttpConnectorConfiguration httpConnectorConfiguration,
            final AuthenticationConfiguration authenticationConfiguration) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.httpConnectorConfiguration = httpConnectorConfiguration;
    }

    @Bean
    public AuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(authenticationConfiguration.passwordEncoder());
        return provider;
    }

    @Bean
    public BasicAuthenticationEntryPoint basicAuthenticationEntryPoint() {
        return new BasicAuthenticationEntryPoint(httpConnectorConfiguration.redirectService());
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsServiceImpl(authenticationConfiguration.userService());
    }
}
