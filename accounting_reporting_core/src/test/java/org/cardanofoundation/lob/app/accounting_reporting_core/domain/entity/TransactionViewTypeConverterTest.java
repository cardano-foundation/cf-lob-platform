package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import static org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType.*;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

public class TransactionViewTypeConverterTest {

    private final TransactionTypeConverter converter = new TransactionTypeConverter();

    @Test
    public void testConvertToDatabaseColumnWithNull() {
        Assertions.assertEquals(0, converter.convertToDatabaseColumn(null));
    }

    @Test
    public void testConvertToDatabaseColumnWithEmptyList() {
        Assertions.assertEquals(0, converter.convertToDatabaseColumn(Arrays.asList()));
    }

    @Test
    public void testConvertToDatabaseColumnWithMultipleValues() {
        List<TransactionType> types = Arrays.asList(CardCharge, VendorPayment, BillCredit);
        // Expected value is the bitwise OR of 1, 256, and 512
        Assertions.assertEquals(769, converter.convertToDatabaseColumn(types));
    }

    @Test
    public void testConvertToEntityAttributeWithNull() {
        Assertions.assertTrue(converter.convertToEntityAttribute(null).isEmpty());
    }

    @Test
    public void testConvertToEntityAttributeWithZero() {
        Assertions.assertTrue(converter.convertToEntityAttribute(0).isEmpty());
    }

    @Test
    public void testConvertToEntityAttributeWithMultipleValues() {
        // Testing with the value 769, which should match CardCharge, VendorPayment, and BillCredit
        List<TransactionType> expectedTypes = Arrays.asList(CardCharge, VendorPayment, BillCredit);
        Assertions.assertEquals(expectedTypes, converter.convertToEntityAttribute(769));
    }

}
