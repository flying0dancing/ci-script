package com.lombardrisk.ignis.design.server.scriptlet;

import com.lombardrisk.ignis.design.server.configuration.ScriptletConfiguration;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@Configuration
public class ScriptletServiceConfiguration {

    private final ScriptletConfiguration scriptletConfiguration;

    @Bean
    public JarUtils jarUtils() {
        return new JarUtils();
    }

    @Bean
    public ScriptletService scriptletService() {
        return new ScriptletService(
                scriptletConfiguration.getScriptletJarPath(),
                scriptletConfiguration.getScriptletInterfaceClass(),
                scriptletConfiguration.getScriptletInputMethodName(),
                scriptletConfiguration.getScriptletOutputMethodName(),
                jarUtils());
    }
}
