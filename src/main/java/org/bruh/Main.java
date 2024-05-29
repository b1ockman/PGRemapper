package org.bruh;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.bruh.PGRemapper.FastMappingsBuilder;
import org.bruh.PGRemapper.MappingsBuilder;
import org.bruh.PGRemapper.MappingsRemapper;
import org.bruh.PGRemapper.SuperClassResolver;
import org.bruh.PGRemapper.antlr4.PGMapLexer;
import org.bruh.PGRemapper.antlr4.PGMapParser;
import org.bruh.PGRemapper.mapping.fast.FastMappings;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Usage: <reverse> <mappings> <lib> <input> <output>");
            System.exit(1);
        }
        remapFile(
                Boolean.parseBoolean(args[0]),
                new File(args[1]),
                new File(args[2]),
                new File(args[3]),
                new File(args[4])
        );
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void remapFile(boolean reverseMapping, File mappingsFile, File libFile, File inputFile, File outputFile) throws IOException {
        var charStream = CharStreams.fromPath(mappingsFile.toPath());
        var lexer = new PGMapLexer(charStream);
        var parser = new PGMapParser(new CommonTokenStream(lexer));
        FastMappings fastMappings;
        if (reverseMapping) {
            var mappings = MappingsBuilder.build(parser);
            mappings = mappings.reverse();
            fastMappings = FastMappings.fromMappings(mappings);
        } else
            fastMappings = FastMappingsBuilder.build(parser);
        var superClassResolver = SuperClassResolver.fromFile(libFile);
        var mappingsRemapper = new MappingsRemapper(fastMappings, superClassResolver);
        outputFile.delete();
        outputFile.createNewFile();
        try (var zipInput = new ZipInputStream(new FileInputStream(inputFile));
             var zipOutput = new ZipOutputStream(new FileOutputStream(outputFile))) {
            ZipEntry entry;
            while ((entry = zipInput.getNextEntry()) != null) {
                var bytes = zipInput.readAllBytes();
                var entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    var reader = new ClassReader(bytes);
                    var writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                    var remapper = new ClassRemapper(writer, mappingsRemapper);
                    reader.accept(remapper, 0);
                    var className = reader.getClassName();
                    var mappedName = mappingsRemapper.mappings.classMappings().getOrDefault(className, className);
                    var fileName = mappedName + ".class";
                    zipOutput.putNextEntry(new ZipEntry(fileName));
                    zipOutput.write(writer.toByteArray());
                    zipOutput.closeEntry();
                    System.out.printf("Remapped %s -> %s\n", entryName, fileName);
                } else {
                    zipOutput.putNextEntry(new ZipEntry(entryName));
                    var inputStream = new ByteArrayInputStream(bytes);
                    inputStream.transferTo(zipOutput);
                    zipOutput.closeEntry();
                    System.out.printf("Copied %s\n", entryName);
                }
            }
        }
    }

}