package com.tsb.cb.common;

import java.time.Duration;
import java.time.format.DateTimeParseException;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.github.resilience4j.core.lang.Nullable;

@Component
@ConfigurationPropertiesBinding
@Order(0)
public class IntegerToDurationConverter implements Converter<Integer, Duration> {

    @Override
    public Duration convert(@Nullable Integer source) {
        if (source != null) {
            try {
                return Duration.ofMillis(source);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Cannot convert '" + source + "' to Duration",
                    e);
            }
        } else {
            return null;
        }

    }
}
