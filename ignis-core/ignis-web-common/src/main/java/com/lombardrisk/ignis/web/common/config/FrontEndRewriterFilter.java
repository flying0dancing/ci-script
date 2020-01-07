package com.lombardrisk.ignis.web.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.tuckey.web.filters.urlrewrite.Conf;
import org.tuckey.web.filters.urlrewrite.UrlRewriteFilter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import java.io.IOException;

public class FrontEndRewriterFilter extends UrlRewriteFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontEndRewriterFilter.class);

    private final Resource urlRewriteXmlConfig;
    private final String systemId;

    public FrontEndRewriterFilter(final Resource urlRewriteXmlConfig, final String systemId) {
        this.urlRewriteXmlConfig = urlRewriteXmlConfig;
        this.systemId = systemId;
    }

    @Override
    protected void loadUrlRewriter(final FilterConfig filterConfig) throws ServletException {
        try {
            Conf conf = new Conf(
                    filterConfig.getServletContext(),
                    urlRewriteXmlConfig.getInputStream(),
                    urlRewriteXmlConfig.getFilename(),
                    systemId);
            checkConf(conf);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            throw new ServletException(e);
        }
    }
}
