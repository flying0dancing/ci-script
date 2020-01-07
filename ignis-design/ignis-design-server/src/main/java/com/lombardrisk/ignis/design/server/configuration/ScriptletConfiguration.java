package com.lombardrisk.ignis.design.server.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class ScriptletConfiguration {

    @Value("${scriptlet.jars.path:./scriptlets}")
    private String scriptletJarPath;

    @Value("${scriptlet.interface.class:com.lombardrisk.ignis.spark.script.api.Scriptlet}")
    private String scriptletInterfaceClass;

    @Value("${scriptlet.input.method.name:inputTraits}")
    private String scriptletInputMethodName;

    @Value("${scriptlet.output.method.name:outputTrait}")
    private String scriptletOutputMethodName;
}
