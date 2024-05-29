package org.bruh.PGRemapper.mapping;

public record FieldMapping(OrigObfPair name, OrigObfPair type, OrigObfPair owner) {

    public FieldMapping reverse() {
        return new FieldMapping(name.reverse(), type.reverse(), owner.reverse());
    }

}
