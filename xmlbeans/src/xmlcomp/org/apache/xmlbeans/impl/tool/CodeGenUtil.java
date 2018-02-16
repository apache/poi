/*   Copyright 2004 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.xmlbeans.impl.tool;

import org.apache.xmlbeans.SystemProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Arrays;
import java.io.File;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileFilter;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;

public class CodeGenUtil
{
    public static String DEFAULT_MEM_START = "8m";
    public static String DEFAULT_MEM_MAX = "256m";
    public static String DEFAULT_COMPILER = "javac";
    public static String DEFAULT_JAR = "jar";

    //workaround for Sun bug # 4723726
    public static URI resolve(URI base, URI child)
    {
        URI ruri = base.resolve(child);
        
        //fix up normalization bug
        if ("file".equals(ruri.getScheme()) && ! child.equals(ruri))
        {
            if (base.getPath().startsWith("//") && !ruri.getPath().startsWith("//"))
            {
                String path = "///".concat(ruri.getPath());
                try
                {
                    ruri = new URI("file", null, path, ruri.getQuery(), ruri.getFragment());
                }
                catch(URISyntaxException uris)
                {}
            }
        }
        return ruri;
    }
    
    static void addAllJavaFiles(List srcFiles, List args)
    {
        for (Iterator i = srcFiles.iterator(); i.hasNext(); )
        {
            File f = (File)i.next();
            if (!f.isDirectory())
            {
                args.add(quoteAndEscapeFilename(f.getAbsolutePath()));
            }
            else
            {
                List inside = (Arrays.asList(f.listFiles(
                    new FileFilter()
                    {
                        public boolean accept(File file)
                            { return (file.isFile() && file.getName().endsWith(".java")) || file.isDirectory(); }
                    }
                )));
                addAllJavaFiles(inside, args);
            }
        }
    }

    static private String quoteAndEscapeFilename(String filename)
    {
        // don't quote if there's no space
        if (filename.indexOf(" ") < 0)
            return filename;

        // bizarre.  javac expects backslash escaping if we quote the classpath
        // bizarre also.  replaceAll expects replacement backslashes to be double escaped.
        return "\"" + filename.replaceAll("\\\\", "\\\\\\\\") + "\"";
    }

    static private String quoteNoEscapeFilename(String filename)
    {
        // don't quote if there's no space, and don't quote on linux
        if (filename.indexOf(" ") < 0 || File.separatorChar == '/')
            return filename;

        return "\"" + filename + "\"";
    }

    /**
     * Invokes javac on the generated source files in order to turn them
     * into binary files in the output directory.  This will return a list of
     * <code>GenFile</code>s for all of the classes produced or null if an
     * error occurred.
     *
     * @deprecated
     */
    static public boolean externalCompile(List srcFiles, File outdir, File[] cp, boolean debug)
    {
        return externalCompile(srcFiles, outdir, cp, debug, DEFAULT_COMPILER, null, DEFAULT_MEM_START, DEFAULT_MEM_MAX, false, false);
    }

    // KHK: temporary to avoid build break
    static public boolean externalCompile(List srcFiles, File outdir, File[] cp, boolean debug, String javacPath, String memStart, String memMax,  boolean quiet, boolean verbose)
    {
        return externalCompile(srcFiles, outdir, cp, debug, javacPath, null, memStart, memMax, quiet, verbose);
    }
    
    /**
     * Invokes javac on the generated source files in order to turn them
     * into binary files in the output directory.  This will return a list of
     * <code>GenFile</code>s for all of the classes produced or null if an
     * error occurred.
     */
    static public boolean externalCompile(List srcFiles, File outdir, File[] cp, boolean debug, String javacPath, String genver, String memStart, String memMax,  boolean quiet, boolean verbose)
    {
        List args = new ArrayList();

        File javac = findJavaTool(javacPath == null ? DEFAULT_COMPILER : javacPath);
        assert (javac.exists()) : "compiler not found " + javac;
        args.add(javac.getAbsolutePath());

        if (outdir == null)
        {
            outdir = new File(".");
        }
        else
        {
            args.add("-d");
            args.add(quoteAndEscapeFilename(outdir.getAbsolutePath()));
        }

        if (cp == null)
        {
            cp = systemClasspath();
        }

        if (cp.length > 0)
        {
            StringBuffer classPath = new StringBuffer();
            // Add the output directory to the classpath.  We do this so that
            // javac will be able to find classes that were compiled
            // previously but are not in the list of sources this time.
            classPath.append(outdir.getAbsolutePath());

            // Add everything on our classpath.
            for (int i = 0; i < cp.length; i++)
            {
                classPath.append(File.pathSeparator);
                classPath.append(cp[i].getAbsolutePath());
            }

            args.add("-classpath");

            // bizarre.  javac expects backslash escaping if we quote the classpath
            args.add(quoteAndEscapeFilename(classPath.toString()));
        }

        if (genver == null)
            genver = "1.4";

        args.add("-source");
        args.add(genver);

        args.add("-target");
        args.add(genver);

        args.add(debug ? "-g" : "-g:none");

        if (verbose)
            args.add("-verbose");

        addAllJavaFiles(srcFiles, args);

        File clFile = null;
        try
        {
            clFile = File.createTempFile("javac", "");
            FileWriter fw = new FileWriter(clFile);
            Iterator i = args.iterator();
            for (i.next(); i.hasNext();)
            {
                String arg = (String)i.next();
                fw.write(arg);
                fw.write('\n');
            }
            fw.close();
            List newargs = new ArrayList();
            newargs.add(args.get(0));
            
            if (memStart != null && memStart.length() != 0)
                newargs.add("-J-Xms" + memStart);
            if (memMax != null && memMax.length() != 0)
                newargs.add("-J-Xmx" + memMax);
            
            newargs.add("@" + clFile.getAbsolutePath());
            args = newargs;
        }
        catch (Exception e)
        {
            System.err.println("Could not create command-line file for javac");
        }

        try
        {
            String[] strArgs = (String[]) args.toArray(new String[args.size()]);

            if (verbose)
            {
                System.out.print("compile command:");
                for (int i = 0; i < strArgs.length; i++)
                    System.out.print(" " + strArgs[i]);
                System.out.println();
            }

            final Process proc = Runtime.getRuntime().exec(strArgs);

            StringBuffer errorBuffer = new StringBuffer();
            StringBuffer outputBuffer = new StringBuffer();

            ThreadedReader out = new ThreadedReader(proc.getInputStream(), outputBuffer);
            ThreadedReader err = new ThreadedReader(proc.getErrorStream(), errorBuffer);

            proc.waitFor();

            if (verbose || proc.exitValue() != 0)
            {
                if (outputBuffer.length() > 0) {
                    System.out.println(outputBuffer.toString());
                    System.out.flush();
                }
                if (errorBuffer.length() > 0) {
                    System.err.println(errorBuffer.toString());
                    System.err.flush();
                }
                
                if (proc.exitValue() != 0)
                    return false;
            }
        }
        catch (Throwable e)
        {
            System.err.println(e.toString());
            System.err.println(e.getCause());
            e.printStackTrace(System.err);
            return false;
        }

        if (clFile != null)
            clFile.delete();

        return true;
    }

    public static File[] systemClasspath()
    {
        List cp = new ArrayList();
        String[] systemcp = SystemProperties.getProperty("java.class.path").split(File.pathSeparator);
        for (int i = 0; i < systemcp.length; i++)
        {
            cp.add(new File(systemcp[i]));
        }
        return (File[])cp.toArray(new File[cp.size()]);
    }

  /**
   * @deprecated Use org.apache.xmlbeans.impl.common.JarHelper instead.
   */
    static public boolean externalJar(File srcdir, File outfile)
    {
        return externalJar(srcdir, outfile, DEFAULT_JAR, false, false);
    }

  /**
   * @deprecated Use org.apache.xmlbeans.impl.common.JarHelper instead.
   */
    static public boolean externalJar(File srcdir, File outfile, String jarPath, boolean quiet, boolean verbose)
    {
        List args = new ArrayList();

        File jar = findJavaTool(jarPath == null ? DEFAULT_JAR : jarPath);
        assert (jar.exists()) : "jar not found " + jar;
        args.add(jar.getAbsolutePath());

        args.add("cf");
        args.add(quoteNoEscapeFilename(outfile.getAbsolutePath()));

        args.add("-C");
        args.add(quoteNoEscapeFilename(srcdir.getAbsolutePath()));

        args.add(".");

        try
        {
            String[] strArgs = (String[]) args.toArray(new String[args.size()]);

            if (verbose)
            {
                System.out.print("jar command:");
                for (int i = 0; i < strArgs.length; i++)
                    System.out.print(" " + strArgs[i]);
                System.out.println();
            }

            final Process proc = Runtime.getRuntime().exec(strArgs);

            StringBuffer errorBuffer = new StringBuffer();
            StringBuffer outputBuffer = new StringBuffer();

            ThreadedReader out = new ThreadedReader(proc.getInputStream(), outputBuffer);
            ThreadedReader err = new ThreadedReader(proc.getErrorStream(), errorBuffer);

            proc.waitFor();

            if (verbose || proc.exitValue() != 0)
            {
                if (outputBuffer.length() > 0) {
                    System.out.println(outputBuffer.toString());
                    System.out.flush();
                }
                if (errorBuffer.length() > 0) {
                    System.err.println(errorBuffer.toString());
                    System.err.flush();
                }

                if (proc.exitValue() != 0)
                    return false;
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    /**
     * Look for tool in current directory and ${JAVA_HOME}/../bin and
     * try with .exe file extension.
     */ 
    private static File findJavaTool(String tool)
    {
        File toolFile = new File(tool);
        if (toolFile.isFile()) {
            return toolFile;
        }

        File result = new File(tool + ".exe");
        if (result.isFile()) {
            return result;
        }
        
        String home = SystemProperties.getProperty("java.home");

        String sep  = File.separator;
        result = new File(home + sep + ".." + sep + "bin", tool);
        
        if (result.isFile()) {
            return result;
        }
        
        result = new File(result.getPath() + ".exe");
        if (result.isFile()) {
            return result;
        }

        result = new File(home + sep + "bin", tool);
        if (result.isFile()) {
            return result;
        }

        result = new File(result.getPath() + ".exe");
        if (result.isFile()) {
            return result;
        }

        // just return the original toolFile and hope that it is on the PATH.
        return toolFile;
    }

    /**
     * Reads the given input stream into the given buffer until there is
     * nothing left to read.
     */
    static private class ThreadedReader
    {
        public ThreadedReader(InputStream stream, final StringBuffer output)
        {
            final BufferedReader reader =
                new BufferedReader(new InputStreamReader(stream));

            Thread readerThread = new Thread(new Runnable() {
                public void run()
                {
                    String s;
                    try
                    {
                        while ((s = reader.readLine()) != null)
                            output.append(s + "\n");
                    }
                    catch (Exception e)
                    {}
                }
            });
            readerThread.start();
        }
    }
}
