package com.lombardrisk.ignis.test.config;

import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

public class AdminUser {

    public static final String USERNAME = "admin";
    public static final String PASSWORD = "password";
    public static final RequestPostProcessor BASIC_AUTH = httpBasic(USERNAME, PASSWORD);

}
