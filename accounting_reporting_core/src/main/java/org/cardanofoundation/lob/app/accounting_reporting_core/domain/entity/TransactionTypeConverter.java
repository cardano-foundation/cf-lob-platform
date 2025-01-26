package org.cardanofoundation.lob.app.accounting_reporting_core.domain.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import lombok.val;

import org.cardanofoundation.lob.app.accounting_reporting_core.domain.core.TransactionType;

@Converter(autoApply = true)
public class TransactionTypeConverter implements AttributeConverter<List<TransactionType>, Integer> {

    private static final Function<TransactionType, Integer> typeToNumber = TransactionTypeMapper.createTypeToNumber();

    @Override
    public Integer convertToDatabaseColumn(List<TransactionType> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return 0;
        }

        int value = 0;
        for (val type : attribute) {
            value |= typeToNumber.apply(type);
        }

        return value;
    }

    @Override
    public List<TransactionType> convertToEntityAttribute(Integer dbData) {
        if (dbData == null || dbData == 0) {
            return new ArrayList<>();
        }

        List<TransactionType> transactionTypes = new ArrayList<>();
        for (val type : TransactionType.values()) {
            val typeValue = typeToNumber.apply(type);

            if ((dbData & typeValue) == typeValue) {
                transactionTypes.add(type);
            }
        }

        return transactionTypes;
    }

}
