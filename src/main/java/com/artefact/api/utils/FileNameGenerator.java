package com.artefact.api.utils;

import java.util.UUID;

public final class FileNameGenerator {
    public static String generateFileName(String format) {
        String imageId = UUID.randomUUID().toString();
        return String.format("%s.%s", imageId, format);
    }
}
