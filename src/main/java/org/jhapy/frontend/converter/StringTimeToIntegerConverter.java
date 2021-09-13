package org.jhapy.frontend.converter;

import com.vaadin.flow.data.binder.ErrorMessageProvider;
import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.AbstractStringToNumberConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class StringTimeToIntegerConverter
        extends AbstractStringToNumberConverter<Integer> {

    public StringTimeToIntegerConverter(String errorMessage) {
        this(null, errorMessage);
    }
    public StringTimeToIntegerConverter(Integer emptyValue, String errorMessage) {
        super(emptyValue, errorMessage);
    }
    public StringTimeToIntegerConverter(ErrorMessageProvider errorMessageProvider) {
        this(null, errorMessageProvider);
    }
    public StringTimeToIntegerConverter(Integer emptyValue,
                                        ErrorMessageProvider errorMessageProvider) {
        super(emptyValue, errorMessageProvider);
    }

    @Override
    public Result<Integer> convertToModel(String value, ValueContext context) {
        if ( StringUtils.isBlank(value) )
            return Result.ok(null);
        var duration =
                Arrays.stream(value.split(":"))
                        .mapToInt(Integer::parseInt)
                        .reduce(0, (n, m) -> n * 60 + m);

        return Result.ok(duration);
    }

}