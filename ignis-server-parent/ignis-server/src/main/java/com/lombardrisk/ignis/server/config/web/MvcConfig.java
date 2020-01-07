package com.lombardrisk.ignis.server.config.web;

import com.lombardrisk.ignis.server.annotation.PageableLimits;
import com.lombardrisk.ignis.web.common.config.FeatureFlagConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

@Configuration
@Import({ FeatureFlagConfiguration.class })
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> argumentResolvers) {
        PageableHandlerMethodArgumentResolver resolver = new IgnisPageableHandler();
        resolver.setMaxPageSize(Integer.MAX_VALUE);
        argumentResolvers.add(resolver);
    }

    private static class IgnisPageableHandler extends PageableHandlerMethodArgumentResolver {

        @NotNull
        @Override
        public Pageable resolveArgument(
                final MethodParameter methodParameter,
                @Nullable final ModelAndViewContainer mavContainer,
                final NativeWebRequest webRequest,
                @Nullable final WebDataBinderFactory binderFactory) {

            Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);
            return getLimitsFromAnnotation(pageable, methodParameter);
        }

        private Pageable getLimitsFromAnnotation(final Pageable page, final MethodParameter methodParameter) {
            PageableLimits limits = methodParameter.getParameterAnnotation(PageableLimits.class);

            if (limits == null) {
                return page;
            }

            if (page.getPageSize() > limits.maxSize()) {
                return PageRequest.of(page.getPageNumber(), limits.maxSize(), page.getSort());
            }

            if (page.getPageSize() < limits.minSize()) {
                return PageRequest.of(page.getPageNumber(), limits.minSize(), page.getSort());
            }
            return page;
        }
    }
}
