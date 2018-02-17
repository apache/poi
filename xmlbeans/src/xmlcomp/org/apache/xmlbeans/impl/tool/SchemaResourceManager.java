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

import org.apache.xmlbeans.impl.common.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileFilter;
import java.io.OutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

public class SchemaResourceManager extends BaseSchemaResourceManager
{
    public static void printUsage()
    {
        System.out.println("Maintains \"xsdownload.xml\", an index of locally downloaded .xsd files");
        System.out.println("usage: sdownload [-dir directory] [-refresh] [-recurse] [-sync] [url/file...]");
        System.out.println("");
        System.out.println("URLs that are specified are downloaded if they aren't already cached.");
        System.out.println("In addition:");
        System.out.println("  -dir specifies the directory for the xsdownload.xml file (default .).");
        System.out.println("  -sync synchronizes the index to any local .xsd files in the tree.");
        System.out.println("  -recurse recursively downloads imported and included .xsd files.");
        System.out.println("  -refresh redownloads all indexed .xsd files.");
        System.out.println("If no files or URLs are specified, all indexed files are relevant.");
    }

    public static void main(String[] args) throws IOException
    {
        if (args.length == 0)
        {
            printUsage();
            System.exit(0);
            return;
        }

        Set flags = new HashSet();
        flags.add("h");
        flags.add("help");
        flags.add("usage");
        flags.add("license");
        flags.add("version");
        flags.add("sync");
        flags.add("refresh");
        flags.add("recurse");

        Set opts = new HashSet();
        opts.add("dir");
        CommandLine cl = new CommandLine(args, flags, opts);
        if (cl.getOpt("h") != null || cl.getOpt("help") != null || cl.getOpt("usage") != null)
        {
            printUsage();
            System.exit(0);
            return;
        }

        String[] badopts = cl.getBadOpts();
        if (badopts.length > 0)
        {
            for (int i = 0; i < badopts.length; i++)
                System.out.println("Unrecognized option: " + badopts[i]);
            printUsage();
            System.exit(0);
            return;
        }

        if (cl.getOpt("license") != null)
        {
            CommandLine.printLicense();
            System.exit(0);
            return;
        }

        if (cl.getOpt("version") != null)
        {
            CommandLine.printVersion();
            System.exit(0);
            return;
        }
        
        args = cl.args();

        boolean sync = (cl.getOpt("sync") != null);
        boolean refresh = (cl.getOpt("refresh") != null);
        boolean imports = (cl.getOpt("recurse") != null);
        String dir = cl.getOpt("dir");
        if (dir == null)
            dir = ".";
        File directory = new File(dir);

        SchemaResourceManager mgr;
        try
        {
            mgr = new SchemaResourceManager(directory);
        }
        catch (IllegalStateException e)
        {
            if (e.getMessage() != null)
                System.out.println(e.getMessage());
            else
                e.printStackTrace();
            System.exit(1);
            return;
        }

        List uriList = new ArrayList();
        List fileList = new ArrayList();
        for (int i = 0; i < args.length; i++)
        {
            if (looksLikeURL(args[i]))
            {
                uriList.add(args[i]);
            }
            else
            {
                fileList.add(new File(directory, args[i]));
            }
        }

        // deal with files that are not in the proper directory
        for (Iterator i = fileList.iterator(); i.hasNext(); )
        {
            File file = (File)i.next();
            if (!isInDirectory(file, directory))
            {
                System.err.println("File not within directory: " + file);
                i.remove();
            }
        }

        // deal with directories
        fileList = collectXSDFiles((File[])fileList.toArray(new File[0]));

        String[] uris = (String[])uriList.toArray(new String[0]);
        File[] files = (File[])fileList.toArray(new File[0]);
        String[] filenames = relativeFilenames(files, directory);

        if (uris.length + filenames.length > 0)
        {
            mgr.process(uris, filenames, sync, refresh, imports);
        }
        else
        {
            mgr.processAll(sync, refresh, imports);
        }

        mgr.writeCache();
        System.exit(0);
    }

    private static boolean looksLikeURL(String str)
    {
        return str.startsWith("http:") || str.startsWith("https:") || str.startsWith("ftp:") || str.startsWith("file:");
    }

    private static String relativeFilename(File file, File directory)
    {
        if (file == null || file.equals(directory))
            return ".";
        return relativeFilename(file.getParentFile(), directory) + "/" + file.getName();
    }

    private static String[] relativeFilenames(File[] files, File directory)
    {
        String[] result = new String[files.length];
        for (int i = 0; i < files.length; i++)
        {
            result[i] = relativeFilename(files[i], directory);
        }
        return result;
    }

    private static boolean isInDirectory(File file, File dir)
    {
        if (file == null)
            return false;
        if (file.equals(dir))
            return true;
        return isInDirectory(file.getParentFile(), dir);
    }





    public SchemaResourceManager(File directory)
    {
        _directory = directory;
        init();
    }

    private File _directory;


    // SOME METHODS TO OVERRIDE ============================

    /**
     * Sends messages to System.out.
     */
    protected void warning(String msg)
    {
        System.out.println(msg);
    }

    /**
     * Returns true if the given filename exists.  The filenames
     * are of the form "/foo/bar/zee.xsd" and should be construed
     * as rooted at the root of the project.
     */
    protected boolean fileExists(String filename)
    {
        return (new File(_directory, filename)).exists();
    }

    /**
     * Gets the data in the given filename as an InputStream.
     */
    protected InputStream inputStreamForFile(String filename) throws IOException
    {
        return new FileInputStream(new File(_directory, filename));
    }

    /**
     * Writes an entire file in one step.  An InputStream is passed and
     * copied to the file.
     */
    protected void writeInputStreamToFile(InputStream input, String filename) throws IOException
    {
        File targetFile = new File(_directory, filename);

        File parent = targetFile.getParentFile();
        if (!parent.exists())
            parent.mkdirs();
        OutputStream output = new FileOutputStream(targetFile);
        IOUtil.copyCompletely(input, output);
    }

    /**
     * Deletes a file.  Sometimes immediately after writing a new file
     * we notice that it's exactly the same as an existing file and
     * we delete it. We never delete a file that was given to us
     * by the user.
     */
    protected void deleteFile(String filename)
    {
        new File(_directory, filename).delete();
    }

    /**
     * Returns a list of all the XSD filesnames in the project.
     */
    protected String[] getAllXSDFilenames()
    {
        File[] allFiles = (File[])collectXSDFiles(new File[] { _directory }).toArray(new File[0]);
        return relativeFilenames(allFiles, _directory);
    }

    /**
     * Simple recursive file filter to do the above.
     */
    private static List collectXSDFiles(File[] dirs)
    {
        List files = new ArrayList();
        for (int i = 0; i < dirs.length; i++)
        {
            File f = dirs[i];
            if (!f.isDirectory())
            {
                files.add(f);
            }
            else
            {
                files.addAll(collectXSDFiles(
                    f.listFiles(new FileFilter()
                    {
                        public boolean accept(File file)
                            { return file.isDirectory() ||
                              file.isFile() &&
                              file.getName().endsWith(".xsd"); }
                    }
                )));
            }
        }
        return files;
    }

}
