package org.cardanofoundation.lob.app.support.orm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.YearMonth;

import org.junit.jupiter.api.Test;

public class YearMonthStringAttributeConverterTest {

    private final YearMonthStringAttributeConverter converter = new YearMonthStringAttributeConverter();

    @Test
    public void convertToDatabaseColumn_NullAttribute_ReturnsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    public void convertToEntityAttribute_NullDbData_ReturnsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    public void convertToEntityAttribute_EmptyString_ReturnsNull() {
        assertNull(converter.convertToEntityAttribute(""));
    }

    @Test
    public void convertToEntityAttribute_InvalidFormat_ReturnsNull() {
        assertNull(converter.convertToEntityAttribute("2022"));
    }

    @Test
    public void convertToEntityAttribute_ValidString_ReturnsYearMonth() {
        YearMonth expected = YearMonth.of(2022, 10);

        assertEquals(expected, converter.convertToEntityAttribute("2022-10"));
    }

    @Test
    public void convertToDatabaseColumn_ValidAttribute_ReturnsFormattedString() {
        YearMonth yearMonth = YearMonth.of(2022, 10);

        assertEquals("2022-10", converter.convertToDatabaseColumn(yearMonth));
    }

}
