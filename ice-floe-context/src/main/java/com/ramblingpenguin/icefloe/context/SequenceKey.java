package com.ramblingpenguin.icefloe.context;

import java.io.Serializable;
import java.util.UUID;

public record SequenceKey<INPUT, OUTPUT>(Class<INPUT> inType, Class<OUTPUT> outType, String id) implements Serializable {

    public static <INPUT, OUTPUT> SequenceKey<INPUT, OUTPUT> newUUID(Class<INPUT> inType, Class<OUTPUT> outType) {
        return new SequenceKey<>(inType, outType, UUID.randomUUID().toString());
    }
}
