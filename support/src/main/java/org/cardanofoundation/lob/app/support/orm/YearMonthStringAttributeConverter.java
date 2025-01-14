package org.cardanofoundation.lob.app.support.orm;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.annotation.Nullable;
import java.time.YearMonth;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Converter(autoApply = true)
public class YearMonthStringAttributeConverter implements AttributeConverter<YearMonth, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable YearMonth attribute) {
        if (attribute == null) {
            return null;
        }
        int month = attribute.getMonthValue();

        return String.format("%d-%02d", attribute.getYear(), month);
    }

    @Override
    @Nullable
    public YearMonth convertToEntityAttribute(@Nullable String dbData) {
        if (!isBlank(dbData)) {
            String[] parts = dbData.split("-");
            if (parts.length == 2) {
                int year = Integer.parseInt(parts[0]);
                // Adjust month to be 1-based when converting back
                int month = Integer.parseInt(parts[1]);

                return YearMonth.of(year, month);
            }
        }

        return null;
    }

}