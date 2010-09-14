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

import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.io.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.lang.reflect.Field;

/**
 * Build a 'lite' version of the ooxml-schemas.jar
 *
 * @author Yegor Kozlov
 */
public final class OOXMLLite {

    private static final Field _classes;
    static {
        try {
            _classes = ClassLoader.class.getDeclaredField("classes");
            _classes.setAccessible(true);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

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

    public static void main(String[] args) throws IOException {

        String dest = null, test = null, ooxml = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-dest")) dest = args[++i];
            else if (args[i].equals("-test")) test = args[++i];
            else if (args[i].equals("-ooxml")) ooxml = args[++i];
        }
        OOXMLLite builder = new OOXMLLite(dest, test, ooxml);
        builder.build();
    }

    void build() throws IOException{

        List<String> lst = new ArrayList<String>();
        //collect unit tests
        System.out.println("Collecting unit tests from " + _testDir);
        collectTests(_testDir, _testDir, lst, ".+?\\.Test.+?\\.class$");

        TestSuite suite = new TestSuite();
        for (String arg : lst) {
            //ignore inner classes defined in tests
            if (arg.indexOf('$') != -1) continue;

            String cls = arg.replace(".class", "");
            try {
                Class test = Class.forName(cls);
                suite.addTestSuite(test);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        //run tests
        TestRunner.run(suite);

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
                for(Class fc : cls.getDeclaredClasses()){
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
        for(Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ){
            JarEntry je = e.nextElement();
            if(je.getName().matches("schemaorg_apache_xmlbeans/system/\\w+/\\w+\\.xsb")) {
                 File destFile = new File(_destDest, je.getName());
                 copyFile(jar.getInputStream(je), destFile);
            }
        }
        jar.close();
    }

    /**
     * Recursively collect classes from the supplied directory
     *
     * @param arg   the directory to search in
     * @param out   output
     * @param ptrn  the pattern (regexp) to filter found files
     */
    private static void collectTests(File root, File arg, List<String> out, String ptrn) {
        if (arg.isDirectory()) {
            for (File f : arg.listFiles()) {
                collectTests(root, f, out, ptrn);
            }
        } else {
            String path = arg.getAbsolutePath();
            String prefix = root.getAbsolutePath();
            String cls = path.substring(prefix.length() + 1).replace(File.separator, ".");
            if(cls.matches(ptrn)) out.add(cls);
        }
    }

    /**
     *
     * @param ptrn the pattern to filter output 
     * @return the classes loaded by the system class loader keyed by class name
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Class<?>> getLoadedClasses(String ptrn) {
        ClassLoader appLoader = ClassLoader.getSystemClassLoader();
        try {
            Vector<Class<?>> classes = (Vector<Class<?>>) _classes.get(appLoader);
            Map<String, Class<?>> map = new HashMap<String, Class<?>>();
            for (Class<?> cls : classes) {
                String jar = cls.getProtectionDomain().getCodeSource().getLocation().toString();
                if(jar.indexOf(ptrn) != -1) map.put(cls.getName(), cls);
            }
            return map;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static void copyFile(InputStream srcStream, File destFile) throws IOException {
        File destDirectory = destFile.getParentFile();
        destDirectory.mkdirs();
        OutputStream destStream = new FileOutputStream(destFile);
        try {
            IOUtils.copy(srcStream, destStream);
        } finally {
            destStream.close();
        }
    }

}