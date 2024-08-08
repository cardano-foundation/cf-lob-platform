package org.cardanofoundation.lob.app.support.problem_support;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.zalando.problem.Problem;

@RequiredArgsConstructor
@ToString
@Getter

// TODO move to utils package???, it is not really specific to business domain but utility class
public class IdentifiableProblem {

    private final String id;
    private final Problem problem;
    private final IdType idType;

    public enum IdType {
        TRANSACTION,
        TRANSACTION_ITEM
    }

}