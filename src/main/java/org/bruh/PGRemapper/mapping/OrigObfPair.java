package org.bruh.PGRemapper.mapping;

import org.bruh.PGRemapper.Util;

import java.util.Map;

public record OrigObfPair(String orig, String obf) {

    public OrigObfPair reverse() {
        return new OrigObfPair(obf, orig);
    }

    public static OrigObfPair fromTSCM(String type, Map<String, String> strClassMappings) {
        return new OrigObfPair(type, Util.getMappedType(type, strClassMappings));
    }

}