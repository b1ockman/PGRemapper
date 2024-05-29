package org.bruh.PGRemapper.mapping;

import java.util.List;

public record Mappings(List<ClassMapping> classMappings, List<FieldMapping> fieldMappings,
                       List<MethodMapping> methodMappings) {

    public Mappings reverse() {
        var newClassMappings = classMappings.stream().map(ClassMapping::reverse).toList();
        var newFieldMappings = fieldMappings.stream().map(FieldMapping::reverse).toList();
        var newMethodMappings = methodMappings.stream().map(MethodMapping::reverse).toList();
        return new Mappings(newClassMappings, newFieldMappings, newMethodMappings);
    }

}
