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

package org.apache.poi.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.internal.TextListener;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

/**
 * Build a 'lite' version of the ooxml-schemas.jar
 *
 * @author Yegor Kozlov
 */
public final class OOXMLLite {

    /**
     * Destination directory to copy filtered classes
     */
    private File _destDest;

    /**
     * Directory with the compiled ooxml tests
     */
    private File _testDir;

    /**
     * Reference to the ooxml-schemas.jar
     */
    private File _ooxmlJar;


    OOXMLLite(String dest, String test, String ooxmlJar) {
        _destDest = new File(dest);
        _testDir = new File(test);
        _ooxmlJar = new File(ooxmlJar);
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String dest = null, test = null, ooxml = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-dest")) dest = args[++i];
            else if (args[i].equals("-test")) test = args[++i];
            else if (args[i].equals("-ooxml")) ooxml = args[++i];
        }
        OOXMLLite builder = new OOXMLLite(dest, test, ooxml);
        builder.build();
    }

    void build() throws IOException, ClassNotFoundException {
        List<Class<?>> lst = new ArrayList<Class<?>>();
        //collect unit tests
        String exclude = StringUtil.join("|",
                "BaseTestXWorkbook",
                "BaseTestXSheet",
                "BaseTestXRow",
                "BaseTestXCell",
                "BaseTestXSSFPivotTable",
                "TestSXSSFWorkbook\\$\\d",
                "TestUnfixedBugs",
                "MemoryUsage",
                "TestDataProvider",
                "TestDataSamples",
                "All.+Tests",
                "ZipFileAssert",
                "AesZipFileZipEntrySource",
                "TempFileRecordingSXSSFWorkbookWithCustomZipEntrySource",
                "PkiTestUtils",
                "TestCellFormatPart\\$\\d",
                "TestSignatureInfo\\$\\d",
                "TestCertificateEncryption\\$CertData",
                "TestPOIXMLDocument\\$OPCParser",
                "TestPOIXMLDocument\\$TestFactory",
                "TestXSLFTextParagraph\\$DrawTextParagraphProxy",
                "TestXSSFExportToXML\\$\\d",
                "TestXSSFExportToXML\\$DummyEntityResolver",
                "TestFormulaEvaluatorOnXSSF\\$Result",
                "TestFormulaEvaluatorOnXSSF\\$SS",
                "TestMultiSheetFormulaEvaluatorOnXSSF\\$Result",
                "TestMultiSheetFormulaEvaluatorOnXSSF\\$SS",
                "TestXSSFBugs\\$\\d"
        );
        System.out.println("Collecting unit tests from " + _testDir);
        collectTests(_testDir, _testDir, lst, ".+.class$", ".+(" + exclude + ").class");
        System.out.println("Found " + lst.size() + " classes");
        
        //run tests
        JUnitCore jUnitCore = new JUnitCore();
        jUnitCore.addListener(new TextListener(System.out));
        Result result = jUnitCore.run(lst.toArray(new Class<?>[lst.size()]));
        if (!result.wasSuccessful()) {
            throw new RuntimeException("Tests did not succeed, cannot build ooxml-lite jar");
        }

        //see what classes from the ooxml-schemas.jar are loaded
        System.out.println("Copying classes to " + _destDest);
        Map<String, Class<?>> classes = getLoadedClasses(_ooxmlJar.getName());
        for (Class<?> cls : classes.values()) {
            String className = cls.getName();
            String classRef = className.replace('.', '/') + ".class";
            File destFile = new File(_destDest, classRef);
            copyFile(cls.getResourceAsStream('/' + classRef), destFile);

            if(cls.isInterface()){
                /**
                 * Copy classes and interfaces declared as members of this class
                 */
                for(Class<?> fc : cls.getDeclaredClasses()){
                    className = fc.getName();
                    classRef = className.replace('.', '/') + ".class";
                    destFile = new File(_destDest, classRef);
                    copyFile(fc.getResourceAsStream('/' + classRef), destFile);
                }
            }
        }

        //finally copy the compiled .xsb files
        System.out.println("Copying .xsb resources");
        JarFile jar = new  JarFile(_ooxmlJar);
        Pattern p = Pattern.compile("schemaorg_apache_xmlbeans/(system|element)/.*\\.xsb");
        try {
            for(Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ){
                JarEntry je = e.nextElement();
                if(p.matcher(je.getName()).matches()) {
                     File destFile = new File(_destDest, je.getName());
                     copyFile(jar.getInputStream(je), destFile);
                }
            }
        } finally {
            jar.close();
        }
    }

    private static boolean checkForTestAnnotation(Class<?> testclass) {
        for (Method m : testclass.getDeclaredMethods()) {
            if(m.isAnnotationPresent(Test.class)) {
                return true;
            }
        }
        
        // also check super classes
        if(testclass.getSuperclass() != null) {
            for (Method m : testclass.getSuperclass().getDeclaredMethods()) {
                if(m.isAnnotationPresent(Test.class)) {
                    return true;
                }
            }
        }
        
        System.out.println("Class " + testclass.getName() + " does not derive from TestCase and does not have a @Test annotation");

        // Should we also look at superclasses to find cases
        // where we have abstract base classes with derived tests?
        // if(checkForTestAnnotation(testclass.getSuperclass())) return true;

        return false;
    }

    /**
     * Recursively collect classes from the supplied directory
     *
     * @param arg   the directory to search in
     * @param out   output
     * @param ptrn  the pattern (regexp) to filter found files
     */
    private static void collectTests(File root, File arg, List<Class<?>> out, String ptrn, String exclude)
    throws ClassNotFoundException {
        if (arg.isDirectory()) {
            File files[] = arg.listFiles();
            if (files != null) {
                for (File f : files) {
                    collectTests(root, f, out, ptrn, exclude);
                }
            }
        } else {
            String path = arg.getAbsolutePath();
            String prefix = root.getAbsolutePath();
            String cls = path.substring(prefix.length() + 1).replace(File.separator, ".");
            if(!cls.matches(ptrn)) return;
            if (cls.matches(exclude)) return;
            //ignore inner classes defined in tests
            if (cls.indexOf('$') != -1) {
                System.out.println("Inner class " + cls + " not included");
                return;
            }

            cls = cls.replace(".class", "");

            try {
                Class<?> testclass = Class.forName(cls);
                if (TestCase.class.isAssignableFrom(testclass)
                    || checkForTestAnnotation(testclass)) {
                    out.add(testclass);
                }
            } catch (Throwable e) { // NOSONAR
                System.out.println("Class " + cls + " is not in classpath");
                return;
            }
        }
    }

    /**
     *
     * @param ptrn the pattern to filter output
     * @return the classes loaded by the system class loader keyed by class name
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Class<?>> getLoadedClasses(String ptrn) {
        // make the field accessible, we defer this from static initialization to here to 
        // allow JDKs which do not have this field (e.g. IBM JDK) to at least load the class
        // without failing, see https://issues.apache.org/bugzilla/show_bug.cgi?id=56550
        final Field _classes = AccessController.doPrivileged(new PrivilegedAction<Field>() {
            @SuppressForbidden("TODO: Reflection works until Java 8 on Oracle/Sun JDKs, but breaks afterwards (different classloader types, access checks)")
            public Field run() {
                try {
                    Field fld = ClassLoader.class.getDeclaredField("classes");
                    fld.setAccessible(true);
                    return fld;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });

        ClassLoader appLoader = ClassLoader.getSystemClassLoader();
        try {
            Vector<Class<?>> classes = (Vector<Class<?>>) _classes.get(appLoader);
            Map<String, Class<?>> map = new HashMap<String, Class<?>>();
            for (Class<?> cls : classes) {
                // e.g. proxy-classes, ...
                ProtectionDomain pd = cls.getProtectionDomain();
                if (pd == null) continue;
                CodeSource cs = pd.getCodeSource();
                if (cs == null) continue;
                URL loc = cs.getLocation();
                if (loc == null) continue;
                
                String jar = loc.toString();
                if(jar.indexOf(ptrn) != -1) map.put(cls.getName(), cls);
            }
            return map;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyFile(InputStream srcStream, File destFile) throws IOException {
        File destDirectory = destFile.getParentFile();
        if (!(destDirectory.exists() || destDirectory.mkdirs())) {
            throw new RuntimeException("Can't create destination directory: "+destDirectory);
        }
        ;
        OutputStream destStream = new FileOutputStream(destFile);
        try {
            IOUtils.copy(srcStream, destStream);
        } finally {
            destStream.close();
        }
    }

}