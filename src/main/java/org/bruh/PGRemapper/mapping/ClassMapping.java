package org.bruh.PGRemapper.mapping;

import java.util.List;

public record ClassMapping(OrigObfPair name, List<OrigObfPair> fields,
                           List<OrigObfPair> methods) {

    public ClassMapping reverse() {
        var newFields = fields.stream().map(OrigObfPair::reverse).toList();
        var newMethods = methods.stream().map(OrigObfPair::reverse).toList();
        return new ClassMapping(name.reverse(), newFields, newMethods);
    }

}
