package com.lombardrisk.ignis.server.controller;

import lombok.experimental.UtilityClass;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

@UtilityClass
public class ServletUriUtils {

    public static String getContextPath() {
        UriComponents uriComponents = ServletUriComponentsBuilder.fromCurrentRequest().build();
        StringBuilder uriBuilder = new StringBuilder();
        if (uriComponents.getScheme() != null) {
            uriBuilder.append(uriComponents.getScheme());
            uriBuilder.append(':');
        }
        if (uriComponents.getUserInfo() != null || uriComponents.getHost() != null) {
            uriBuilder.append("//");
            if (uriComponents.getUserInfo() != null) {
                uriBuilder.append(uriComponents.getUserInfo());
                uriBuilder.append('@');
            }
            if (uriComponents.getHost() != null) {
                uriBuilder.append(uriComponents.getHost());
            }
            if (uriComponents.getPort() != -1) {
                uriBuilder.append(':');
                uriBuilder.append(uriComponents.getPort());
            }
        }
        return uriBuilder.toString();
    }
}
