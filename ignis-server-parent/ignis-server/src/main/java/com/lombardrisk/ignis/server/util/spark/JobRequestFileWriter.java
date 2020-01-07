package com.lombardrisk.ignis.server.util.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.server.util.TempDirectory;
import com.lombardrisk.ignis.spark.api.JobRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
public class JobRequestFileWriter {

    private final ObjectMapper objectMapper;
    private final TempDirectory tempDirectory;

    public JobRequestFileWriter(
            final ObjectMapper objectMapper,
            final TempDirectory tempDirectory) {

        this.objectMapper = objectMapper;
        this.tempDirectory = tempDirectory;
    }

    File writeJobFile(final JobRequest jobRequest) throws IOException {
        String serviceRequestId = String.valueOf(jobRequest.getServiceRequestId());
        Path jobFilePath = tempDirectory.resolvePath(serviceRequestId);
        File jobFile = new File(jobFilePath.toString(), String.format("job_%s.json", serviceRequestId));

        log.trace("Writing job request to [{}]", jobFile.getAbsolutePath());
        objectMapper.writeValue(jobFile, jobRequest);

        return jobFile;
    }
}
