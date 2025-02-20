package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;

import lombok.Getter;
import lombok.ToString;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@ToString
public class FatalError {

    private final FatalError.Code code;
    private final String subCode;
    private final Map<String, Object> bag;

    @JsonCreator
    public FatalError(
            @JsonProperty("code") FatalError.Code code,
            @JsonProperty("subCode") String subCode,
            @JsonProperty("bag") Map<String, Object> bag) {
        this.code = code;
        this.subCode = subCode;
        this.bag = bag;
    }

    public enum Code {
        ADAPTER_ERROR
    }

}
