package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.FieldType;
import org.zalando.problem.Problem;

import java.util.Map;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public class PreprocessorService {

    private final Map<FieldType, Function<String, Either<Problem, String>>> fieldProcessors;

    public Either<Problem, String> preProcess(String data, FieldType fieldType) {
        val fieldProcessorFun = fieldProcessors.get(fieldType);

        if (fieldProcessorFun == null) {
            throw new RuntimeException(STR."Field processor not found for field severity: \{fieldType}");
        }

        return fieldProcessorFun.apply(data);
    }

}
