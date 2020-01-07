package com.lombardrisk.ignis.data.common.error;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@AllArgsConstructor(onConstructor = @__({ @JsonCreator }))
public class ErrorResponse implements Serializable {

    private static final long serialVersionUID = -4418664903748434056L;

    @ApiModelProperty(required = true)
    private final String errorMessage;
    @ApiModelProperty(required = true)
    private final String errorCode;

    public static ErrorResponse valueOf(final String errorMessage, final String errorCode) {
        return new ErrorResponse(StringUtils.trimToNull(errorMessage), StringUtils.trimToNull(errorCode));
    }
}
