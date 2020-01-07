package com.lombardrisk.ignis.design.server.configuration.web;

import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lombardrisk.ignis.web.common.config.FrontEndRewriterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import javax.servlet.Filter;

@Configuration
@Import(SecurityConfiguration.class)
public class WebConfiguration {

    @Value("classpath:WEB-INF/urlrewrite.xml")
    private Resource urlRewriteXmlConfig;

    @Value("${logging.level.root:INFO}")
    private String urlRewriterLogLevel;

    @Bean
    public FilterRegistrationBean<FrontEndRewriterFilter> urlRewriteFilter() {
        FilterRegistrationBean<FrontEndRewriterFilter> urlRewriterRegistrationBean = new FilterRegistrationBean<>();

        urlRewriterRegistrationBean.setFilter(new FrontEndRewriterFilter(urlRewriteXmlConfig, ""));

        urlRewriterRegistrationBean.addInitParameter("logLevel", urlRewriterLogLevel);
        return urlRewriterRegistrationBean;
    }

    @Bean
    public Filter headerForwardingFilter() {
        return new DesignStudioHeaderFilter();
    }

    @Bean
    public MappingJackson2HttpMessageConverter jackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(
                Jackson2ObjectMapperBuilder.json()
                        .modules(new Jdk8Module(), new JavaTimeModule(), new GuavaModule())
                        .build());
    }
}
