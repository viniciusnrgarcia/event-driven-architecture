package br.com.vnrg.paymentconnectormq.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

public final class GeneratorID {
    private GeneratorID() {
    }

    public static String stringUUID() {
        return UUID().toString();
    }

    public static UUID UUID() {
        return UUID.randomUUID();
    }

    public static String basic(List<Object> values) {
        var basicId = "";
        var it = values.iterator();
        while (it.hasNext()) {
            var i = it.next();
            if (i != null) {
                if (i instanceof String) {
                    basicId += i;
                    continue;
                }
                if (i instanceof LocalDate) {
                    basicId += ((LocalDate) i).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    continue;
                }
                if (i instanceof LocalDateTime) {
                    basicId += ((LocalDateTime) i).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
                    continue;
                }
                basicId += i.toString();
            }
        }
        return basicId;
    }
}
