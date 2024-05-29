package org.bruh.PGRemapper;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipFile;

public record SuperClassResolver(Map<String, List<String>> superClasses) {

    public static SuperClassResolver fromFile(File file) throws IOException {
        var superClasses = new HashMap<String, List<String>>();
        try (var zipFile = new ZipFile(file)) {
            for (var entry : Collections.list(zipFile.entries())) {
                if (!entry.getName().endsWith(".class")) continue;
                try (var istream = zipFile.getInputStream(entry)) {
                    new ClassReader(istream).accept(new ClassVisitor(Opcodes.ASM9) {
                        @Override
                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                            var superNames = new ArrayList<String>();
                            if (superName != null)
                                superNames.add(superName);
                            if (interfaces != null && interfaces.length > 0)
                                superNames.addAll(List.of(interfaces));
                            superClasses.put(name, superNames);
                        }
                    }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
                }
            }
        }
        return new SuperClassResolver(superClasses);
    }

    public Iterator<String> iterateSuperClasses(String name) {
        return new Iterator<>() {

            private final Deque<String> queue = new ArrayDeque<>();
            private final Set<String> queued = new HashSet<>();

            {
                queue.addLast(name);
            }

            @Override
            public boolean hasNext() {
                return !queue.isEmpty();
            }

            @Override
            public String next() {
                var target = queue.removeFirst();
                var targetSuperClasses = superClasses.get(target);
                if (targetSuperClasses == null)
                    return target;
                for (var superClass : targetSuperClasses) {
                    if (queued.contains(superClass)) continue;
                    queue.addLast(superClass);
                    queued.add(superClass);
                }
                return target;
            }

        };
    }

}
