package com.artefact.api.utils;

import java.time.LocalDateTime;

// На мой взгляд стоит придумать какой-нибудь другой способ генерации названия
public final class FileNameGenerator {
    public static String generateFileName(Long id, String format) {
        String dateTime = LocalDateTime.now().toString();
        return String.format("%d_%s.%s", id, dateTime, format);
    }
}
