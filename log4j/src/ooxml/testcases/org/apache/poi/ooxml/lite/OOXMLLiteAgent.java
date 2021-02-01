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

package org.apache.poi.ooxml.lite;

import static net.bytebuddy.matcher.ElementMatchers.named;

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

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;

/**
 * OOXMLLiteAgent is the replacement for the former OOXMLLite, because in Java 12
 * it isn't possible to access the privates :) of the ClassLoader
 */
public class OOXMLLiteAgent {

    public static void premain(String agentArgs, Instrumentation inst) throws IOException {
        String[] args = (agentArgs == null ? "" : agentArgs).split("\\|", 2);
        String logBase = args.length >= 1 ? args[0] : "ooxml-lite-report";

        XsbLogger.load(logBase+".xsb");

        ClazzLogger log = new ClazzLogger();
        log.load(logBase+".clazz");
        log.setPattern(args.length >= 2 ? args[1] : ".*/schemas/.*");
        inst.addTransformer(log);

        new AgentBuilder.Default()
        // .with(AgentBuilder.Listener.StreamWriting.toSystemOut())
            .type(named("org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl$XsbReader"))
            .transform((builder, type, cl, m) ->
                builder
                .constructor(ElementMatchers.any())
                .intercept(MethodDelegation.to(XsbLogger.class).andThen(SuperMethodCall.INSTANCE))
            )
            .installOn(inst);
    }

    /**
     * This logger intercepts the loading of XmlBeans .xsb
     *
     * when ran in the ant junitlauncher, it's not possible to have the interceptor methods as
     * instance method of ClazzLogger. the junit test will fail ... though it works ok in IntelliJ
     * probably because of classpath vs. modulepath instantiation
     */
    public static class XsbLogger {
        private static Path logPath;
        private static final Set<Integer> hashes = new HashSet<>();

        static void load(String path) throws IOException {
            logPath = Paths.get(path);
            if (Files.exists(logPath)) {
                try (Stream<String> stream = Files.lines(logPath)) {
                    stream.forEach((s) -> hashes.add(s.hashCode()));
                }
            }
        }

        // SchemaTypeSystemImpl.XsbReader::new is delegated to here - method name doesn't matter
        public static void loadXsb(SchemaTypeSystemImpl parent, String handle) {
            write(logPath, handle, hashes);
        }

        public static void loadXsb(SchemaTypeSystemImpl parent, String handle, int filetype) {
            loadXsb(parent, handle);
        }
    }

    /**
     * This logger is used to log the used XmlBeans classes
     */
    public static class ClazzLogger implements ClassFileTransformer {
        Path logPath;
        Pattern includes;
        final Set<Integer> hashes = new HashSet<>();

        void setPattern(String regex) {
            includes = Pattern.compile(regex);
        }

        void load(String path) throws IOException {
            this.logPath = Paths.get(path);
            if (Files.exists(this.logPath)) {
                try (Stream<String> stream = Files.lines(this.logPath)) {
                    stream.forEach((s) -> hashes.add(s.hashCode()));
                }
            }
        }

        public byte[] transform(ClassLoader loader, String className, Class redefiningClass, ProtectionDomain domain, byte[] bytes) {
            if (logPath != null && className != null && includes.matcher(className).find()) {
                write(logPath, className, hashes);
            }
            return bytes;
        }
    }


    static void write(Path path, String item, Set<Integer> hashes) {
        if (!hashes.contains(item.hashCode())) {
            try {
                // TODO: check if this is atomic ... as transform() is probably called synchronized, it doesn't matter anyway
                Files.write(path, (item+"\n").getBytes(StandardCharsets.ISO_8859_1), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                hashes.add(item.hashCode());
            } catch (IOException ignored) {
            }
        }
    }
}
