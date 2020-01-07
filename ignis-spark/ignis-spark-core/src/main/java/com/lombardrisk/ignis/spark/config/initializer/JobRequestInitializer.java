package com.lombardrisk.ignis.spark.config.initializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.spark.api.JobRequest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.IOException;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;

public class JobRequestInitializer<T extends JobRequest> implements ApplicationContextInitializer<GenericApplicationContext> {

    private String jobFileName;
    private final ObjectMapper objectMapper;
    private final Class<T> jobRequestClass;

    public JobRequestInitializer(
            final String jobFileName,
            final ObjectMapper objectMapper,
            final Class<T> jobRequestClass) {
        this.jobFileName = jobFileName;
        this.objectMapper = objectMapper;
        this.jobRequestClass = jobRequestClass;
    }

    public JobRequestInitializer(
            final String jobFileName,
            final Class<T> jobRequestClass) {
        this(jobFileName, MAPPER, jobRequestClass);
    }

    @Override
    public void initialize(@NotNull final GenericApplicationContext context) {
        context.registerBean(jobRequestClass, this::convertJobFileToJobRequest);
    }

    private T convertJobFileToJobRequest() {
        try {
            File jobFile = new File(jobFileName);
            return objectMapper.readValue(jobFile, jobRequestClass);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
