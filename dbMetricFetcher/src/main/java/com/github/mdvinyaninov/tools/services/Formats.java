package com.github.mdvinyaninov.tools.services;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Formats {
    public static final DateTimeFormatter DTF = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
}
