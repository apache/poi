/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.ooxml.util;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * OOXMLLiteAgent is the replacement for the former OOXMLLite, because in Java 12
 * it isn't possible to access the privates :) of the ClassLoader
 */
public class OOXMLLiteAgent {

    static class LoggingTransformer implements ClassFileTransformer {
        final Path path;
        final Pattern includes;
        final Set<Integer> fileHashes = new HashSet<>();

        public LoggingTransformer(String agentArgs) {
            String[] args = (agentArgs == null ? "" : agentArgs).split("\\|", 2);
            path = Paths.get(args.length >= 1 ? args[0] : "ooxml-lite.out");
            includes = Pattern.compile(args.length >= 2 ? args[1] : ".*/schemas/.*");

            try {
                if (Files.exists(path)) {
                    try (Stream<String> stream = Files.lines(path)) {
                        stream.forEach((s) -> fileHashes.add(s.hashCode()));
                    }
                } else {
                    Files.createFile(path);
                }
            } catch (IOException ignored) {
            }
        }

        public byte[] transform(ClassLoader loader, String className, Class redefiningClass, ProtectionDomain domain, byte[] bytes) {
            if (path != null && className != null && !fileHashes.contains(className.hashCode()) && includes.matcher(className).find()) {
                try {
                    // TODO: check if this is atomic ... as transform() is probably called synchronized, it doesn't matter anyway
                    Files.write(path, (className+"\n").getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.APPEND);
                    fileHashes.add(className.hashCode());
                } catch (IOException ignroed) {
                }
            }
            return bytes;
        }
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new LoggingTransformer(agentArgs));
    }
}
