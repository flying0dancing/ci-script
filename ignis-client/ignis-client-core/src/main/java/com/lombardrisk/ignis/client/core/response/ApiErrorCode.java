package com.lombardrisk.ignis.client.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ApiErrorCode {

    private String errorCode;
    private String errorMessage;
}
