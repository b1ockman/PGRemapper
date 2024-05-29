package org.bruh.PGRemapper;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Util {

    public static String jvmClassName(String name) {
        return name.replace(".", "/");
    }

    public static final Map<String, String> primSig = Map.of(
            "boolean", "Z",
            "byte", "B",
            "char", "C",
            "short", "S",
            "int", "I",
            "long", "J",
            "float", "F",
            "double", "D",
            "void", "V"
    );

    public static String jvmSignature(String type) {
        var arrCnt = StringUtils.countMatches(type, "[]");
        var arrPrefix = "[".repeat(arrCnt);
        type = type.replace("[]", "");
        if (primSig.containsKey(type))
            return arrPrefix + primSig.get(type);
        return arrPrefix + "L" + jvmClassName(type) + ";";
    }

    public static String jvmSignature(String ret, List<String> argTypes) {
        var argsSig = argTypes.stream().map(Util::jvmSignature).collect(Collectors.joining());
        return "(" + argsSig + ")" + jvmSignature(ret);
    }

    public static String getMappedType(String type, Map<String, String> strClassMappings) {
        var arrCnt = StringUtils.countMatches(type, "[]");
        type = type.replace("[]", "");
        var mappedType = strClassMappings.getOrDefault(type, type);
        return mappedType + "[]".repeat(arrCnt);
    }

}
