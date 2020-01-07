package com.lombardrisk.ignis.server.config.swagger;

import com.google.common.collect.Sets;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration;
import springfox.documentation.RequestHandler;
import springfox.documentation.annotations.ApiIgnore;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;
import java.util.Date;

import static java.util.Collections.emptyList;

@Configuration
@EnableSwagger2
@Import(BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .securitySchemes(Collections.singletonList(new BasicAuth("fcrLogin")))
                .apiInfo(apiInfo())
                .select()
                .apis(SwaggerConfig::isDocumentedApi)
                .paths(PathSelectors.any())
                .build()
                .ignoredParameterTypes(ResponseEntity.class, FcrResponse.class)
                .directModelSubstitute(Date.class, Long.class)
                .protocols(Sets.newHashSet("https"));
    }

    private static boolean isDocumentedApi(final RequestHandler handler) {
        return (RequestHandlerSelectors.withClassAnnotation(Api.class).test(handler)
                || RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class).test(handler))
                && !RequestHandlerSelectors.withMethodAnnotation(ApiIgnore.class).test(handler);
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "FCR Engine Api",
                "Api access to the FCR Engine",
                "1.0",
                null,
                null,
                null,
                null,
                emptyList()
        );
    }
}
