package org.bruh.PGRemapper;

import org.antlr.v4.runtime.RuleContext;
import org.bruh.PGRemapper.mapping.fast.FastMappings;
import org.bruh.PGRemapper.antlr4.PGMapParser;
import org.bruh.PGRemapper.mapping.fast.FieldKey;
import org.bruh.PGRemapper.mapping.fast.MethodKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public final class FastMappingsBuilder {

    private record Pair<K, V>(K first, V second) {
    }

    public static FastMappings build(PGMapParser parser) {
        return buildMappings(parser.mappings());
    }

    private static FastMappings buildMappings(PGMapParser.MappingsContext ctx) {
        var classMappings = new HashMap<String, String>();
        var fieldMappings = new HashMap<FieldKey, String>();
        var methodMappings = new HashMap<MethodKey, String>();
        for (var mapping : ctx.mapping()) {
            var classMapping = buildClassMapping(mapping.class_mapping());
            classMappings.put(classMapping.first(), classMapping.second());
            fieldMappings.putAll(mapping.field_mapping().stream()
                    .map(f -> buildFieldMapping(f, classMapping.first))
                    .collect(Collectors.toMap(Pair::first, Pair::second)));
            methodMappings.putAll(mapping.func_mapping().stream()
                    .map(m -> buildMethodMapping(m, classMapping.first))
                    .collect(Collectors.toMap(Pair::first, Pair::second)));
            methodMappings.putAll(mapping.func_mapping_ln().stream()
                    .map(m -> buildMethodMapping(m, classMapping.first))
                    .collect(Collectors.toMap(Pair::first, Pair::second)));
        }
        return new FastMappings(classMappings, fieldMappings, methodMappings);
    }

    private static Pair<String, String> buildClassMapping(PGMapParser.Class_mappingContext ctx) {
        return new Pair<>(
                Util.jvmClassName(ctx.pinfo_name(0).getText()),
                Util.jvmClassName(ctx.pinfo_name(1).getText())
        );
    }

    private static Pair<FieldKey, String> buildFieldMapping(PGMapParser.Field_mappingContext ctx, String owner) {
        return new Pair<>(
                new FieldKey(owner, ctx.ID(0).getText()),
                ctx.ID(1).getText()
        );
    }

    private static Pair<MethodKey, String> buildMethodMapping(PGMapParser.Func_mapping_lnContext ctx, String owner) {
        return buildMethodMapping(ctx.func_mapping(), owner);
    }

    private static Pair<MethodKey, String> buildMethodMapping(PGMapParser.Func_mappingContext ctx, String owner) {
        var retType = Util.jvmClassName(ctx.func().type().getText());
        var argTypes = buildFuncArgs(ctx.func().func_args());
        return new Pair<>(
                new MethodKey(owner, ctx.func().func_name().getText(), Util.jvmSignature(retType, argTypes)),
                ctx.func_name().getText()
        );
    }

    private static List<String> buildFuncArgs(PGMapParser.Func_argsContext ctx) {
        if (ctx == null) return new ArrayList<>();
        return ctx.type().stream().map(RuleContext::getText).map(Util::jvmClassName).toList();
    }

}
