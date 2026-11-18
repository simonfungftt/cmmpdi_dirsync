package com.ctg.innovic.cmmpdi.dirsync.utils;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LdapTimeUtils {

    private static final DateTimeFormatter AD_GENERALIZED_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss.0'Z'")
                    .withZone(ZoneOffset.UTC);

    public static String toAdGeneralizedTime(Instant instant) {
        return AD_GENERALIZED_TIME_FORMATTER.format(instant);
    }
}
