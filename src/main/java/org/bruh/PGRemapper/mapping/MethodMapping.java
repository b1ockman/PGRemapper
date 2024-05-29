package org.bruh.PGRemapper.mapping;

import org.bruh.PGRemapper.Util;

import java.util.List;

public record MethodMapping(OrigObfPair name, OrigObfPair retType, List<OrigObfPair> argTypes,
                            OrigObfPair owner) {

    public MethodMapping reverse() {
        return new MethodMapping(name.reverse(), retType.reverse(),
                argTypes.stream().map(OrigObfPair::reverse).toList(), owner.reverse());
    }

    public String getOrigSig() {
        return Util.jvmSignature(retType.orig(), argTypes.stream().map(OrigObfPair::orig).toList());
    }

    public String getObfSig() {
        return Util.jvmSignature(retType.obf(), argTypes.stream().map(OrigObfPair::obf).toList());
    }

}
