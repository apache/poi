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

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeSystem;
import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.impl.common.IOUtil;
import org.apache.xmlbeans.impl.common.XmlErrorWatcher;
import org.apache.xmlbeans.impl.schema.SchemaTypeCodePrinter;
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemCompiler;
import org.apache.xmlbeans.impl.util.FilerImpl;
import org.apache.xmlbeans.SchemaCodePrinter;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.Filer;
import repackage.Repackager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SchemaCodeGenerator
{
    /**
     * Saves a SchemaTypeSystem to the specified directory.
     *
     * @param system the <code>SchemaTypeSystem</code> to save
     * @param classesDir the destination directory for xsb's
     * @param sourceFile if present, the TypeSystemHolder source will be
     *                   generated in this file for subsequent compilation,
     *                   if null then the source will be generated in a temp
     *                   directory and then compiled to the destination dir
     * @param repackager the repackager to use when generating the holder class
     * @param options options. Can be null
     * @deprecated Use {@link SchemaTypeSystem.save()} instead.
     */
    public static void saveTypeSystem(SchemaTypeSystem system, File classesDir,
        File sourceFile, Repackager repackager, XmlOptions options)
        throws IOException
    {
        Filer filer = new FilerImpl(classesDir, null, repackager, false, false);
        system.save(filer);
    }

    static void deleteObsoleteFiles(File rootDir, File srcDir, Set seenFiles)
    {
        if (!(rootDir.isDirectory() && srcDir.isDirectory()))
            throw new IllegalArgumentException();
        String absolutePath = srcDir.getAbsolutePath();
        // Do a sanity check to make sure we don't delete by mistake some important dir
        if (absolutePath.length() <= 5)
            return;
        if (absolutePath.startsWith("/home/") &&
            (absolutePath.indexOf("/", 6) >= absolutePath.length() - 1 ||
                absolutePath.indexOf("/", 6) < 0))
            return;

        // Go recursively starting with srcDir and delete all files that are
        // not in the given Set
        File[] files = srcDir.listFiles();
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].isDirectory())
                deleteObsoleteFiles(rootDir, files[i], seenFiles);
            else if (seenFiles.contains(files[i]))
                ;
            else
            {
                deleteXmlBeansFile(files[i]);
                deleteDirRecursively(rootDir, files[i].getParentFile());
            }
        }
    }

    private static void deleteXmlBeansFile(File file)
    {
        if (file.getName().endsWith(".java"))
            file.delete();
    }

    private static void deleteDirRecursively(File root, File dir)
    {
        String[] list = dir.list();
        while (list != null && list.length == 0 && !dir.equals(root))
        {
            dir.delete();
            dir = dir.getParentFile();
            list = dir.list();
        }
    }

    protected static File createTempDir() throws IOException
    {

// Some beta builds of JDK1.5 are having troubles creating temp directories
// if the java.io.tmpdir doesn't exist.  This seems to help.
try {
  File tmpDirFile = new File(SystemProperties.getProperty("java.io.tmpdir"));
  tmpDirFile.mkdirs();
} catch(Exception e) { e.printStackTrace(); }

        File tmpFile = File.createTempFile("xbean", null);
        String path = tmpFile.getAbsolutePath();
        if (!path.endsWith(".tmp"))
            throw new IOException("Error: createTempFile did not create a file ending with .tmp");
        path = path.substring(0, path.length() - 4);
        File tmpSrcDir = null;

        for (int count = 0; count < 100; count++)
        {
            String name = path + ".d" + (count == 0 ? "" : Integer.toString(count++));

            tmpSrcDir = new File(name);

            if (!tmpSrcDir.exists())
            {
                boolean created = tmpSrcDir.mkdirs();
                assert created : "Could not create " + tmpSrcDir.getAbsolutePath();
                break;
            }
        }
        tmpFile.deleteOnExit();

        return tmpSrcDir;
    }

    protected static void tryHardToDelete(File dir)
    {
        tryToDelete(dir);
        if (dir.exists())
            tryToDeleteLater(dir);
    }

    private static void tryToDelete(File dir)
    {
        if (dir.exists())
        {
            if (dir.isDirectory())
            {
                String[] list = dir.list(); // can return null if I/O error
                if (list != null)
                    for (int i = 0; i < list.length; i++)
                        tryToDelete(new File(dir, list[i]));
            }
            if (!dir.delete())
                return; // don't try very hard, because we're just deleting tmp
        }
    }

    private static Set deleteFileQueue = new HashSet();
    private static int triesRemaining = 0;

    private static boolean tryNowThatItsLater()
    {
        List files;

        synchronized (deleteFileQueue)
        {
            files = new ArrayList(deleteFileQueue);
            deleteFileQueue.clear();
        }

        List retry = new ArrayList();

        for (Iterator i = files.iterator(); i.hasNext(); )
        {
            File file = (File)i.next();
            tryToDelete(file);
            if (file.exists())
                retry.add(file);
        }

        synchronized (deleteFileQueue)
        {
            if (triesRemaining > 0)
                triesRemaining -= 1;

            if (triesRemaining <= 0 || retry.size() == 0) // done?
                triesRemaining = 0;
            else
                deleteFileQueue.addAll(retry); // try again?

            return (triesRemaining <= 0);
        }
    }

    private static void giveUp()
    {
        synchronized (deleteFileQueue)
        {
            deleteFileQueue.clear();
            triesRemaining = 0;
        }
    }

    private static void tryToDeleteLater(File dir)
    {
        synchronized (deleteFileQueue)
        {
            deleteFileQueue.add(dir);
            if (triesRemaining == 0)
            {
                new Thread()
                {
                    public void run()
                    {
                        // repeats tryNow until triesRemaining == 0
                        try
                        {
                            for (;;)
                            {
                                if (tryNowThatItsLater())
                                    return; // succeeded
                                Thread.sleep(1000 * 3); // wait three seconds
                            }
                        }
                        catch (InterruptedException e)
                        {
                            giveUp();
                        }
                    }
                };
            }

            if (triesRemaining < 10)
                triesRemaining = 10;
        }
    }

}
