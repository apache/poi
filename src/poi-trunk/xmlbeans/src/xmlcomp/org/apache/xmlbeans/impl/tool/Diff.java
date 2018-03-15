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
import org.apache.xmlbeans.impl.schema.SchemaTypeSystemImpl;

import java.io.*;
import java.util.List;
import java.util.Arrays;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.Enumeration;
import java.util.zip.ZipEntry;

public class Diff
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: diff <jarname1> <jarname2> to compare two jars");
            System.out.println("  or   diff <dirname1> <dirname2> to compare two dirs");
            return;
        }
        File file1 = new File(args[0]);
        if (!file1.exists())
        {
            System.out.println("File \"" + args[0] + "\" not found.");
            return;
        }
        File file2 = new File(args[1]);
        if (!file2.exists())
        {
            System.out.println("File \"" + args[1] + "\" not found.");
            return;
        }
        List result = new ArrayList();
        if (file1.isDirectory())
        {
            if (!file2.isDirectory())
            {
                System.out.println("Both parameters have to be directories if the first parameter is a directory.");
                return;
            }
            dirsAsTypeSystems(file1, file2, result);
        }
        else
        {
            if (file2.isDirectory())
            {
                System.out.println("Both parameters have to be jar files if the first parameter is a jar file.");
                return;
            }
            try
            {
            JarFile jar1 = new JarFile(file1);
            JarFile jar2 = new JarFile(file2);
            jarsAsTypeSystems(jar1, jar2, result);
            }
            catch (IOException ioe)
            { ioe.printStackTrace(); }
        }
        if (result.size() < 1)
            System.out.println("No differences encountered.");
        else
        {
            System.out.println("Differences:");
            for (int i = 0; i < result.size(); i++)
                System.out.println(result.get(i).toString());
        }
    }

    /**
     * Diffs the contents of two jars, looking only at the schema typesystems
     * saved inside those jars
     */
    public static void jarsAsTypeSystems(JarFile jar1, JarFile jar2, List diffs)
    {
        Enumeration entries1 = jar1.entries();
        Enumeration entries2 = jar2.entries();
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        for (; entries1.hasMoreElements(); )
        {
            ZipEntry ze = (ZipEntry) entries1.nextElement();
            String name = ze.getName();
            if (name.startsWith("schema" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN + "/system/s") && name.endsWith(".xsb"))
                list1.add(ze);
        }
        for (; entries2.hasMoreElements(); )
        {
            ZipEntry ze = (ZipEntry) entries2.nextElement();
            String name = ze.getName();
            if (name.startsWith("schema" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN + "/system/s") && name.endsWith(".xsb"))
                list2.add(ze);
        }
        ZipEntry[] files1 = (ZipEntry[]) list1.toArray(new ZipEntry[list1.size()]);
        ZipEntry[] files2 = (ZipEntry[]) list2.toArray(new ZipEntry[list2.size()]);
        ZipEntryNameComparator comparator = new ZipEntryNameComparator();
        Arrays.sort(files1, comparator);
        Arrays.sort(files2, comparator);
        int i1 = 0;
        int i2 = 0;
        while (i1 < files1.length && i2 < files2.length)
        {
            String name1 = files1[i1].getName();
            String name2 = files2[i2].getName();
            int dif = name1.compareTo(name2);
            if (dif == 0)
            {
                // Compare the files
                zipEntriesAsXsb(files1[i1], jar1, files2[i2], jar2, diffs);
                i1++; i2++; // Move to next pair
            }
            else if (dif < 0)
            {
                // dir1 contains a file that dir2 doesn't
                diffs.add("Jar \"" + jar1.getName() + "\" contains an extra file: \"" +
                    name1 + "\"");
                i1++;
            }
            else if (dif > 0)
            {
                // dir2 contains a file that dir1 doesn't
                diffs.add("Jar \"" + jar2.getName() + "\" contains an extra file: \"" +
                    name2 + "\"");
                i2++;
            }
        }
        while (i1 < files1.length)
        {
            diffs.add("Jar \"" + jar1.getName() + "\" contains an extra file: \"" +
                files1[i1].getName() + "\"");
            i1++;
        }
        while (i2 < files2.length)
        {
            diffs.add("Jar \"" + jar2.getName() + "\" contains an extra file: \"" +
                files2[i2].getName() + "\"");
            i2++;
        }
    }

    /**
     * Diffs the contents of two dirs looking only at the xsb files
     * contained in these two dirs
     * Updated diffs with a list of differences (for the time being, strings
     * describing the difference)
     */
    public static void dirsAsTypeSystems(File dir1, File dir2, List diffs)
    {
        assert dir1.isDirectory() : "Parameters must be directories";
        assert dir2.isDirectory() : "Parameters must be directories";

        /**
         * Navigate three directories deep to get to the type system.
         * Assume the schema[METADATA_PACKAGE_LOAD]/system/* structure
         */
        File temp1 = new File(dir1, "schema" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN + "/system");
        File temp2 = new File(dir2, "schema" + SchemaTypeSystemImpl.METADATA_PACKAGE_GEN + "/system");
        if (temp1.exists() && temp2.exists())
        {
            File[] files1 = temp1.listFiles();
            File[] files2 = temp2.listFiles();
            if (files1.length == 1 && files2.length == 1)
            {
                temp1 = files1[0];
                temp2 = files2[0];
            }
            else
            {
                if (files1.length == 0)
                    temp1 = null;
                if (files2.length == 0)
                    temp2 = null;
                if (files1.length > 1)
                {
                    diffs.add("More than one typesystem found in dir \"" +
                        dir1.getName() + "\"");
                    return;
                }
                if (files2.length > 1)
                {
                    diffs.add("More than one typesystem found in dir \"" +
                        dir2.getName() + "\"");
                    return;
                }                    
            }
        }
        else
        {
            if (!temp1.exists())
                temp1 = null;
            if (!temp2.exists())
                temp2 = null;
        }
        if (temp1 == null && temp2 == null)
            return;
        else if (temp1 == null || temp2 == null)
        {
            if (temp1 == null)
                diffs.add("No typesystems found in dir \"" + dir1 + "\"");
            if (temp2 == null)
                diffs.add("No typesystems found in dir \"" + dir2 + "\"");
            return;
        }
        else
        {
            dir1 = temp1;
            dir2 = temp2;
        }

        boolean diffIndex = isDiffIndex();
        XsbFilenameFilter xsbName = new XsbFilenameFilter();
        File[] files1 = dir1.listFiles(xsbName);
        File[] files2 = dir2.listFiles(xsbName);
        FileNameComparator comparator = new FileNameComparator();
        Arrays.sort(files1, comparator);
        Arrays.sort(files2, comparator);
        int i1 = 0;
        int i2 = 0;
        while (i1 < files1.length && i2 < files2.length)
        {
            String name1 = files1[i1].getName();
            String name2 = files2[i2].getName();
            int dif = name1.compareTo(name2);
            if (dif == 0)
            {
                if (diffIndex || !files1[i1].getName().equals("index.xsb"))
                    filesAsXsb(files1[i1], files2[i2], diffs); // Compare the files
                i1++; i2++; // Move to next pair
            }
            else if (dif < 0)
            {
                // dir1 contains a file that dir2 doesn't
                diffs.add("Dir \"" + dir1.getName() + "\" contains an extra file: \"" +
                    name1 + "\"");
                i1++;
            }
            else if (dif > 0)
            {
                // dir2 contains a file that dir1 doesn't
                diffs.add("Dir \"" + dir2.getName() + "\" contains an extra file: \"" +
                    name2 + "\"");
                i2++;
            }
        }
        while (i1 < files1.length)
        {
            diffs.add("Dir \"" + dir1.getName() + "\" contains an extra file: \"" +
                files1[i1].getName() + "\"");
            i1++;
        }
        while (i2 < files2.length)
        {
            diffs.add("Dir \"" + dir2.getName() + "\" contains an extra file: \"" +
                files2[i2].getName() + "\"");
            i2++;
        }
    }

    private static boolean isDiffIndex()
    {
        String prop = SystemProperties.getProperty("xmlbeans.diff.diffIndex");
        if (prop == null)
            return true;
        if ("0".equals(prop) || "false".equalsIgnoreCase(prop))
            return false;
        return true;
    }

    /**
     * Diffs the two given files assuming they are in xsb format
     * Updates diffs with differences in string format
     */
    public static void filesAsXsb(File file1, File file2, List diffs)
    {
        assert file1.exists() : "File \"" + file1.getAbsolutePath() + "\" does not exist.";
        assert file2.exists() : "File \"" + file2.getAbsolutePath() + "\" does not exist.";
        try
        {
        FileInputStream stream1 = new FileInputStream(file1);
        FileInputStream stream2 = new FileInputStream(file2);
        streamsAsXsb(stream1, file1.getName(), stream2, file2.getName(), diffs);
        }
        catch (FileNotFoundException fnfe)
        { }
        catch (IOException ioe)
        { }
    }

    public static void zipEntriesAsXsb(ZipEntry file1, JarFile jar1,
        ZipEntry file2, JarFile jar2, List diffs)
    {
        try
        {
        InputStream stream1 = jar1.getInputStream(file1);
        InputStream stream2 = jar2.getInputStream(file2);
        streamsAsXsb(stream1, file1.getName(), stream2, file2.getName(), diffs);
        }
        catch (IOException ioe)
        { }
    }

    public static void streamsAsXsb(InputStream stream1, String name1,
        InputStream stream2, String name2, List diffs)
        throws IOException
    {
        ByteArrayOutputStream buf1 = new ByteArrayOutputStream();
        ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
        XsbDumper.dump(stream1, "", new PrintStream(buf1));
        XsbDumper.dump(stream2, "", new PrintStream(buf2));
        stream1.close();
        stream2.close();
        readersAsText(new StringReader(buf1.toString()), name1,
            new StringReader(buf2.toString()), name2, diffs);
    }

    public static void readersAsText(Reader r1, String name1, Reader r2, String name2,
        List diffs)
        throws IOException
    {
        org.apache.xmlbeans.impl.util.Diff.readersAsText(r1, name1, r2, name2, diffs);
    }

    private static class XsbFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            return name.endsWith(".xsb");
        }
    }

    private static class ZipEntryNameComparator implements Comparator
    {
        public boolean equals(Object object)
        {
            return this == object;
        }

        public int compare(Object object1, Object object2)
        {
            assert (object1 instanceof ZipEntry) : "Must pass in a java.util.zip.ZipEntry as argument";
            assert (object2 instanceof ZipEntry) : "Must pass in a java.util.zip.ZipEntry as argument";

            String name1 = ((ZipEntry) object1).getName();
            String name2 = ((ZipEntry) object2).getName();
            return name1.compareTo(name2);
        }
    }

    private static class FileNameComparator implements Comparator
    {
        public boolean equals(Object object)
        {
            return this == object;
        }

        public int compare(Object object1, Object object2)
        {
            assert (object1 instanceof File) : "Must pass in a java.io.File as argument";
            assert (object2 instanceof File) : "Must pass in a java.io.File as argument";

            String name1 = ((File) object1).getName();
            String name2 = ((File) object2).getName();
            return name1.compareTo(name2);
        }
    }
}
