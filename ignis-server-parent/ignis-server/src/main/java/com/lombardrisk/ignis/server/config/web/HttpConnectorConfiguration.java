package com.lombardrisk.ignis.server.config.web;

import com.lombardrisk.ignis.server.controller.drillback.RedirectService;
import com.lombardrisk.ignis.web.common.config.CorrelationIdFilter;
import com.lombardrisk.ignis.web.common.config.FrontEndRewriterFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.File;
import java.util.UUID;

@Configuration
@ConditionalOnProperty(name = "ignis.ui.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class HttpConnectorConfiguration {

    @Value("classpath:WEB-INF/urlrewrite.xml")
    private Resource urlRewriteXmlConfig;

    @Value("${server.servlet.contextPath}")
    private String servletContextPath;

    @Value("${logging.level.root:INFO}")
    private String urlRewriterLogLevel;

    @Bean
    public FilterRegistrationBean<FrontEndRewriterFilter> urlRewriteFilter() {
        FilterRegistrationBean<FrontEndRewriterFilter> urlRewriterRegistrationBean = new FilterRegistrationBean<>();

        urlRewriterRegistrationBean.setFilter(
                new FrontEndRewriterFilter(urlRewriteXmlConfig, servletContextPath));

        urlRewriterRegistrationBean.addInitParameter("logLevel", urlRewriterLogLevel);
        return urlRewriterRegistrationBean;
    }

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter(() -> UUID.randomUUID().toString());
    }

    @Bean
    public RedirectService redirectService() {
        return new RedirectService(servletContextPath);
    }

    @Bean
    public FCRHeaderFilter fcrHeaderFilter() {
        return new FCRHeaderFilter();
    }

    @Bean
    public ServletWebServerFactory servletContainer(
            @Value("${server.http.port}") final Integer httpPort,
            @Value("${server.https.port}") final Integer httpsPort,
            @Value("${ignis.ui.home}") final String ignisUiPath) {
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
            @Override
            protected void postProcessContext(final Context context) {
                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(getHttpConnector(httpPort, httpsPort));
        File docRoot = new File(ignisUiPath);
        if (docRoot.exists() && docRoot.isDirectory()) {
            log.info(
                    "Custom location is used for static assets, document root folder: {}",
                    docRoot.getAbsolutePath());
            tomcat.setDocumentRoot(docRoot);
        } else {
            log.warn(
                    "Custom document root folder {} doesn't exist, custom location for static assets was not used.",
                    docRoot.getAbsolutePath());
        }

        return tomcat;
    }

    private static Connector getHttpConnector(final Integer httpPort, final Integer httpsPort) {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(httpPort);
        connector.setSecure(false);
        connector.setRedirectPort(httpsPort);
        return connector;
    }
}
