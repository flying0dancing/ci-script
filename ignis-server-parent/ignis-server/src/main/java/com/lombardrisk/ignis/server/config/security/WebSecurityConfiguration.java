package com.lombardrisk.ignis.server.config.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    public static final String SWAGGER_HOME = "/swagger-ui.html";

    WebSecurityConfiguration() {
        // noop
    }

    @Configuration
    @Order(10)
    public static class ApiSecurityConfig extends WebSecurityConfigurerAdapter {

        private static final String[] SWAGGER_RESOURCES = {
                // -- swagger ui
                "/swagger-resources/**",
                SWAGGER_HOME,
                "/v2/api-docs",
                "/webjars/**"
        };

        private final AuthenticationProviderConfig authenticationProviderConfig;

        @Autowired
        public ApiSecurityConfig(final AuthenticationProviderConfig authenticationProviderConfig) {
            this.authenticationProviderConfig = authenticationProviderConfig;
        }

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .authorizeRequests()
                    .antMatchers(SWAGGER_RESOURCES).authenticated()
                    .antMatchers("/api/**", "/auth/**").authenticated()
                    .and()
                    .httpBasic().realmName("FCR")
                    .authenticationEntryPoint(authenticationProviderConfig.basicAuthenticationEntryPoint());
        }

        @Autowired
        public void configureGlobal(final AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(authenticationProviderConfig.getAuthenticationProvider());
        }
    }

    @Configuration
    public static class FrontEndSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(final HttpSecurity http) throws Exception {
            http
                    .formLogin().loginPage("/index.html")
                    .and()
                    .authorizeRequests().anyRequest().permitAll();
        }
    }
}
