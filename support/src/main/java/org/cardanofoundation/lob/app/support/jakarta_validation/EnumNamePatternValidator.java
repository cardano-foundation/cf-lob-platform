package org.cardanofoundation.lob.app.support.jakarta_validation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import lombok.val;

public class EnumNamePatternValidator implements ConstraintValidator<EnumNamePattern, Enum<?>> {

    private Pattern pattern;

    @Override
    public void initialize(EnumNamePattern annotation) {
        try {
            pattern = Pattern.compile(annotation.regexp());
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Given regex is invalid", e);
        }
    }

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        val m = pattern.matcher(value.name());

        return m.matches();
    }
}
