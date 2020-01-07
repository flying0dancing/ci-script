package com.lombardrisk.ignis.web.common.response;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;

@UtilityClass
public class ResponseUtils {

    public static ResponseEntity toDownloadResponseEntity(
            final ByteArrayOutputStream outputStream,
            final String filename,
            final String contentType) {

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");

        byte[] bytes = outputStream.toByteArray();
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(bytes.length)
                .body(new ByteArrayResource(bytes));
    }
}
