package org.cardanofoundation.lob.app.accounting_reporting_core.domain.core;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@RequiredArgsConstructor
@ToString
public class FatalError {

    private final FatalError.Code code;
    private final String subCode;

    private final Map<String, Object> bag;

    public enum Code {
        ADAPTER_ERROR
    }

}
