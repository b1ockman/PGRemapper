package org.bruh.PGRemapper;

import org.antlr.v4.runtime.RuleContext;
import org.bruh.PGRemapper.antlr4.PGMapParser;
import org.bruh.PGRemapper.mapping.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MappingsBuilder {

    private MappingsBuilder() {}

    public static Mappings build(PGMapParser parser) {
        return buildMappings(parser.mappings());
    }

    private static Mappings buildMappings(PGMapParser.MappingsContext ctx) {
        var classMappings = new ArrayList<ClassMapping>();
        var fieldMappings = new ArrayList<FieldMapping>();
        var methodMappings = new ArrayList<MethodMapping>();
        var strClassMappings = ctx.mapping().stream().collect(Collectors.toMap(
                m -> Util.jvmClassName(m.class_mapping().pinfo_name(0).getText()),
                m -> Util.jvmClassName(m.class_mapping().pinfo_name(1).getText())
        ));
        for (var mapping : ctx.mapping()) {
            var origCurClassName = Util.jvmClassName(mapping.class_mapping().pinfo_name(0).getText());
            var obfCurClassName = Util.jvmClassName(mapping.class_mapping().pinfo_name(1).getText());
            var curClassName = new OrigObfPair(origCurClassName, obfCurClassName);
            var curFieldMappings = mapping.field_mapping().stream()
                    .map(f -> buildFieldMapping(f, curClassName, strClassMappings)).toList();
            var curMethodMappings1 = mapping.func_mapping().stream()
                    .map(m -> buildMethodMapping(m, curClassName, strClassMappings));
            var curMethodMappings2 = mapping.func_mapping_ln().stream()
                    .map(m -> buildMethodMapping(m, curClassName, strClassMappings));
            var curMethodMappings = Stream.concat(curMethodMappings1, curMethodMappings2).toList();
            var curClassMapping = buildClassMapping(
                    mapping.class_mapping(),
                    curFieldMappings.stream().map(FieldMapping::name).toList(),
                    curMethodMappings.stream().map(MethodMapping::name).toList()
            );
            classMappings.add(curClassMapping);
            fieldMappings.addAll(curFieldMappings);
            methodMappings.addAll(curMethodMappings);
        }
        return new Mappings(classMappings, fieldMappings, methodMappings);
    }

    private static ClassMapping buildClassMapping(PGMapParser.Class_mappingContext ctx, List<OrigObfPair> fields,
                                          List<OrigObfPair> methods) {
        var origName = Util.jvmClassName(ctx.pinfo_name(0).getText());
        var obfName = Util.jvmClassName(ctx.pinfo_name(1).getText());
        var name = new OrigObfPair(origName, obfName);
        return new ClassMapping(name, fields, methods);
    }

    private static FieldMapping buildFieldMapping(PGMapParser.Field_mappingContext ctx, OrigObfPair owner,
                                          Map<String, String> strClassMappings) {
        var origName = ctx.ID(0).getText();
        var obfName = ctx.ID(1).getText();
        var name = new OrigObfPair(origName, obfName);
        var type = Util.jvmClassName(ctx.type().getText());
        var typePair = OrigObfPair.fromTSCM(type, strClassMappings);
        return new FieldMapping(name, typePair, owner);
    }

    private static MethodMapping buildMethodMapping(PGMapParser.Func_mapping_lnContext ctx, OrigObfPair owner,
                                            Map<String, String> strClassMappings) {
        return buildMethodMapping(ctx.func_mapping(), owner, strClassMappings);
    }

    private static MethodMapping buildMethodMapping(PGMapParser.Func_mappingContext ctx, OrigObfPair owner,
                                            Map<String, String> strClassMappings) {
        var origName = ctx.func().func_name().getText();
        var obfName = ctx.func_name().getText();
        var name = new OrigObfPair(origName, obfName);
        var retType = Util.jvmClassName(ctx.func().type().getText());
        var retTypePair = OrigObfPair.fromTSCM(retType, strClassMappings);
        var argTypes = buildFuncArgs(ctx.func().func_args(), strClassMappings);
        return new MethodMapping(name, retTypePair, argTypes, owner);
    }

    private static List<OrigObfPair> buildFuncArgs(PGMapParser.Func_argsContext ctx, Map<String, String> strClassMappings) {
        if (ctx == null) return new ArrayList<>();
        return ctx.type().stream().map(RuleContext::getText).map(Util::jvmClassName)
                .map(t -> OrigObfPair.fromTSCM(t, strClassMappings)).toList();
    }

}
