package org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.service.internal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.TransactionDataSearchResult;
import org.cardanofoundation.lob.app.netsuite_altavia_erp_adapter.domain.core.TxLine;
import org.zalando.problem.Problem;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class NetSuiteParser {

    private final ObjectMapper objectMapper;

    public Either<Problem, List<TxLine>> parseSearchResults(String jsonString) {
        try {
            val transactionDataSearchResult = objectMapper.readValue(jsonString, TransactionDataSearchResult.class);

            // TODO how to handle pagination and more results to fetch?

            if (transactionDataSearchResult.more()) {
                log.warn("More data available in the search result, pagination not implemented yet!");

                return Either.left(Problem.builder()
                        .withTitle("PAGINATION_NOT_IMPLEMENTED")
                        .withDetail("More data available in the search result, pagination not implemented yet!")
                        .build()
                );
            }

            return Either.right(transactionDataSearchResult.lines());
        } catch (JsonProcessingException e) {
            log.error("Error parsing NetSuite search result: {}", e.getMessage(), e);

            return Either.left(Problem.builder()
                    .withTitle("JSON_PARSE_ERROR")
                    .withDetail(STR."JSON rrror parsing NetSuite search error: \{e.getMessage()}")
                    .build());
        }
    }

}
