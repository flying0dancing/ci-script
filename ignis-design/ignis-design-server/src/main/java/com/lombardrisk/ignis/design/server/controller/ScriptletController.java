package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.server.scriptlet.ScriptletMetadataView;
import com.lombardrisk.ignis.design.server.scriptlet.ScriptletService;
import com.lombardrisk.ignis.design.server.scriptlet.StructTypeFieldView;
import com.lombardrisk.ignis.web.common.response.EitherResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Slf4j
@RestController
public class ScriptletController {

    private final ScriptletService scriptletService;

    @GetMapping(path = design.api.v1.scriptlets.Jars)
    public EitherResponse<ErrorResponse, List<String>> getScriptletJars() {
        log.info("read scriptlet jars");

        return scriptletService.getScriptletJars()
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.scriptlets.ScriptletClasses)
    public EitherResponse<ErrorResponse, List<String>> getScriptletClasses(
            @PathVariable(design.api.Params.JAR_FILE_NAME) final String jarFile) {
        log.info("read jar {} classes", jarFile);

        return scriptletService.getScriptletClasses(jarFile)
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.scriptlets.ScriptletClassMetadata, params = "type=all")
    public EitherResponse<ErrorResponse, ScriptletMetadataView> getScriptletClassMetadata(
            @PathVariable(design.api.Params.JAR_FILE_NAME) final String jarFile,
            @PathVariable(design.api.Params.SCRIPTLET_CLASS_NAME) final String scriptletClass) {
        log.info("read class {} metadata - jar {}", scriptletClass, jarFile);

        return scriptletService.getScriptletMetadataStructTypes(jarFile, scriptletClass)
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.scriptlets.ScriptletClassMetadata, params = "type=input")
    public EitherResponse<ErrorResponse, Map<String, List<StructTypeFieldView>>> getScriptletClassInputMetadata(
            @PathVariable(design.api.Params.JAR_FILE_NAME) final String jarFile,
            @PathVariable(design.api.Params.SCRIPTLET_CLASS_NAME) final String scriptletClass) {
        log.info("read class {} input metadata - jar {}", scriptletClass, jarFile);

        return scriptletService.getScriptletInputMetadata(jarFile, scriptletClass)
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.scriptlets.ScriptletClassMetadata, params = "type=output")
    public EitherResponse<ErrorResponse, List<StructTypeFieldView>> getScriptletClassOutputMetadata(
            @PathVariable(design.api.Params.JAR_FILE_NAME) final String jarFile,
            @PathVariable(design.api.Params.SCRIPTLET_CLASS_NAME) final String scriptletClass) {
        log.info("read class {} output metadata - jar {}", scriptletClass, jarFile);

        return scriptletService.getScriptletOutputMetadata(jarFile, scriptletClass)
                .fold(EitherResponse::badRequest, EitherResponse::okResponse);
    }
}
