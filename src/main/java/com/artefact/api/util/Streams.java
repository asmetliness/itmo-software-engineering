package com.artefact.api.util;

import java.util.stream.StreamSupport;

public class Streams {

    /**
     * Converts Iterable to stream
     */
    public static <T> java.util.stream.Stream<T> from(final Iterable<T> iterable) {
        return toStream(iterable);
    }


    private static <T> java.util.stream.Stream<T> toStream(final Iterable<T> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false);
    }
}
