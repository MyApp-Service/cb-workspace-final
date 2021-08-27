package com.tsb.cb.common;

import java.time.Duration;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.LongFunction;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import io.github.resilience4j.core.lang.Nullable;

@ConfigurationPropertiesBinding
@Order(0)
@Component
public class StringToDurationConverter implements Converter<String, Duration> {

    @Nullable
    private static Duration simpleParse(String rawTime) {
        String time = rawTime.toLowerCase();
        return tryParse(time, "ns", Duration::ofNanos)
            .orElseGet(() -> tryParse(time, "ms", Duration::ofMillis)
                .orElseGet(() -> tryParse(time, "s", Duration::ofSeconds)
                    .orElseGet(() -> tryParse(time, "m", Duration::ofMinutes)
                        .orElseGet(() -> tryParse(time, "h", Duration::ofHours)
                            .orElseGet(() -> tryParse(time, "d", Duration::ofDays)
                                .orElse(null))))));
    }

    private static Optional<Duration> tryParse(String time, String unit,
        LongFunction<Duration> toDuration) {
        if (time.endsWith(unit)) {
            String trim = time.substring(0, time.lastIndexOf(unit)).trim();
            try {
                return Optional.of(toDuration.apply(Long.parseLong(trim)));
            } catch (NumberFormatException ignore) {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public Duration convert(@Nullable String source) {
        if (source == null || "".equals(source)) {
            return null;
        }
        if (!Character.isDigit(source.charAt(0))) {
            return null;
        }
        Duration duration = simpleParse(source);
        try {
            return duration == null ? Duration.parse(source) : duration;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot convert '" + source + "' to Duration", e);
        }
    }
}
