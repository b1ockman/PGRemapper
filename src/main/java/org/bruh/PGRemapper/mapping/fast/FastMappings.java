package org.bruh.PGRemapper.mapping.fast;

import org.bruh.PGRemapper.mapping.Mappings;

import java.util.Map;
import java.util.stream.Collectors;

public record FastMappings(Map<String, String> classMappings, Map<FieldKey, String> fieldMappings,
                           Map<MethodKey, String> methodMappings) {

    public static FastMappings fromMappings(Mappings mappings) {
        var classMappings = mappings.classMappings().stream()
                .collect(Collectors.toMap(c -> c.name().orig(), c -> c.name().obf()));
        var fieldMappings = mappings.fieldMappings().stream().collect(Collectors.toMap(
                f -> new FieldKey(f.owner().orig(), f.name().orig()),
                f -> f.name().obf()
        ));
        var methodMappings = mappings.methodMappings().stream().collect(Collectors.toMap(
                m -> new MethodKey(m.owner().orig(), m.name().orig(), m.getOrigSig()),
                m -> m.name().obf()
        ));
        return new FastMappings(classMappings, fieldMappings, methodMappings);
    }

}
