package org.company.price.application.utils;

import lombok.experimental.UtilityClass;

import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public final class ApplicationDateParser {

    private static final DateTimeFormatter DMY_HMS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static LocalDateTime parse(String input) {

        return Try.of(() -> LocalDateTime.parse(input, DMY_HMS))
                .getOrElseThrow(() -> new DateTimeParseException("Unrecognized date/time format", input, 0));
    }
}