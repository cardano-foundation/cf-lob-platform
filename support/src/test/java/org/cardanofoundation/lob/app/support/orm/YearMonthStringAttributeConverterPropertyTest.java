package org.cardanofoundation.lob.app.support.orm;

import lombok.val;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

public class YearMonthStringAttributeConverterPropertyTest {

    private final YearMonthStringAttributeConverter converter = new YearMonthStringAttributeConverter();

    @Property
    void convertingYearMonthToStringAndBackResultsInOriginal(@ForAll @IntRange(min = 1900, max = 9999) int year,
                                                             @ForAll @IntRange(min = 1, max = 12) int month) {
        val original = YearMonth.of(year, month);

        val dbData = converter.convertToDatabaseColumn(original);
        val backConverted = converter.convertToEntityAttribute(dbData);

        assertThat(dbData).matches(STR."\{year}-\{String.format("%02d", month)}");

        assertThat(backConverted).isEqualTo(original);
        assertThat(backConverted.getYear()).isEqualTo(year);
        assertThat(backConverted.getMonthValue()).isEqualTo(month);
    }

    @Property
    void convertingInvalidStringToYearMonthResultsInNull(@ForAll("invalidStrings") String invalidString) {
        assertThat(converter.convertToEntityAttribute(invalidString)).isNull();
    }

    @Provide
    Arbitrary<String> invalidStrings() {
        return Arbitraries.strings().alpha().ofMinLength(1).filter(s -> !s.matches("\\d{4}-\\d{2}"));
    }

}
