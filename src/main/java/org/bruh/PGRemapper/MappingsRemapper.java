package org.bruh.PGRemapper;

import org.bruh.PGRemapper.mapping.fast.FastMappings;
import org.bruh.PGRemapper.mapping.fast.FieldKey;
import org.bruh.PGRemapper.mapping.fast.MethodKey;
import org.objectweb.asm.commons.Remapper;

public class MappingsRemapper extends Remapper {

    public final FastMappings mappings;
    public final SuperClassResolver superClassResolver;

    public MappingsRemapper(FastMappings mappings, SuperClassResolver superClassResolver) {
        this.mappings = mappings;
        this.superClassResolver = superClassResolver;
    }

    @Override
    public String map(String name) {
        return mappings.classMappings().getOrDefault(name, name);
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        for (var it = superClassResolver.iterateSuperClasses(owner); it.hasNext(); ) {
            var superClass = it.next();
            var fieldKey = new FieldKey(superClass, name);
            if (!mappings.fieldMappings().containsKey(fieldKey)) continue;
            return mappings.fieldMappings().get(fieldKey);
        }
        return name;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        for (var it = superClassResolver.iterateSuperClasses(owner); it.hasNext(); ) {
            var superClass = it.next();
            var methodKey = new MethodKey(superClass, name, descriptor);
            if (!mappings.methodMappings().containsKey(methodKey)) continue;
            return mappings.methodMappings().get(methodKey);
        }
        return name;
    }

}
