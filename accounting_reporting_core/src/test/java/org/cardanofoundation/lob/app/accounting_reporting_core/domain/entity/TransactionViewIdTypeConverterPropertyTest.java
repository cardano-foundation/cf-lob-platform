package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;


import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Assertions;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

class TransactionViewIdTypeConverterPropertyTest {

    private final TransactionTypeConverter converter = new TransactionTypeConverter();

    @Property
    void roundTripProperty(@ForAll("transactionTypeLists") List<TransactionType> originalTypes) {
        Integer dbValue = converter.convertToDatabaseColumn(originalTypes);
        List<TransactionType> convertedBackTypes = converter.convertToEntityAttribute(dbValue);
        Assertions.assertEquals(new HashSet<>(originalTypes), new HashSet<>(convertedBackTypes));
    }

    @Property
    void nonNegativeIntegerProperty(@ForAll("transactionTypeLists") List<TransactionType> types) {
        Assertions.assertTrue(converter.convertToDatabaseColumn(types) >= 0);
    }

    @Property
    void uniqueRepresentationProperty(@ForAll("transactionTypeLists") List<TransactionType> types) {
        Integer dbValue = converter.convertToDatabaseColumn(types);
        List<TransactionType> convertedBackTypes = converter.convertToEntityAttribute(dbValue);
        Assertions.assertEquals(new HashSet<>(types).size(), new HashSet<>(convertedBackTypes).size());
    }

    @Provide
    Arbitrary<List<TransactionType>> transactionTypeLists() {
        Arbitrary<TransactionType> types = Arbitraries.of(TransactionType.values());

        return types.list().uniqueElements().ofMinSize(0).ofMaxSize(TransactionType.values().length);
    }

}
