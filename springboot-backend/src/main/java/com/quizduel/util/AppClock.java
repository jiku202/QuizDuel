package com.quizduel.util;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** "Today" as a YYYY-MM-DD string, in the configured time zone (default Asia/Manila). */
@Component
public class AppClock {
    private final ZoneId zone;

    public AppClock(@Value("${app.timezone}") String timezone) {
        this.zone = ZoneId.of(timezone);
    }

    public String today() {
        return LocalDate.now(zone).toString();
    }
}
