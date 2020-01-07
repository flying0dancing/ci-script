package com.lombardrisk.ignis.test.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

public class UserTestFixture {

    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public UserTestFixture(final PasswordEncoder passwordEncoder, final JdbcTemplate jdbcTemplate) {
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addUser(final String username, final String password) {
        jdbcTemplate.execute(
                String.format(
                        "insert into SEC_USER "
                                + "(ID, USERNAME, PASSWORD) "
                                + "values ("
                                + "(select coalesce(max(ID), 0) + 1 from SEC_USER),"
                                + "'%s', '%s')",
                        username,
                        passwordEncoder.encode(password)));
    }
}