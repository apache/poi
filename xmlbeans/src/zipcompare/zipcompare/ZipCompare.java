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

package zipcompare;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.*;
import java.io.IOException;
import java.io.InputStream;

public class ZipCompare
{
    public static void main(String[] args)
    {
        if (args.length != 2)
        {
            System.out.println("Usage: zipcompare [file1] [file2]");
            System.exit(1);
        }

        ZipFile file1;
        try { file1 = new ZipFile(args[0]); }
        catch (IOException e) { System.out.println("Could not open zip file " + args[0] + ": " + e); System.exit(1); return; }

        ZipFile file2;
        try { file2 = new ZipFile(args[1]); }
        catch (IOException e) { System.out.println("Could not open zip file " + args[0] + ": " + e); System.exit(1); return; }

        System.out.println("Comparing " + args[0] + " with " + args[1] + ":");

        Set set1 = new LinkedHashSet();
        for (Enumeration e = file1.entries(); e.hasMoreElements(); )
            set1.add(((ZipEntry)e.nextElement()).getName());

        Set set2 = new LinkedHashSet();
        for (Enumeration e = file2.entries(); e.hasMoreElements(); )
            set2.add(((ZipEntry)e.nextElement()).getName());

        int errcount = 0;
        int filecount = 0;
        for (Iterator i = set1.iterator(); i.hasNext(); )
        {
            String name = (String)i.next();
            if (!set2.contains(name))
            {
                System.out.println(name + " not found in " + args[1]);
                errcount += 1;
                continue;
            }
            try
            {
                set2.remove(name);
                if (!streamsEqual(file1.getInputStream(file1.getEntry(name)), file2.getInputStream(file2.getEntry(name))))
                {
                    System.out.println(name + " does not match");
                    errcount += 1;
                    continue;
                }
            }
            catch (Exception e)
            {
                System.out.println(name + ": IO Error " + e);
                e.printStackTrace();
                errcount += 1;
                continue;
            }
            filecount += 1;
        }
        for (Iterator i = set2.iterator(); i.hasNext(); )
        {
            String name = (String)i.next();
            System.out.println(name + " not found in " + args[0]);
            errcount += 1;
        }
        System.out.println(filecount + " entries matched");
        if (errcount > 0)
        {
            System.out.println(errcount + " entries did not match");
            System.exit(1);
        }
        System.exit(0);
    }

    static boolean streamsEqual(InputStream stream1, InputStream stream2) throws IOException
    {
        byte[] buf1 = new byte[4096];
        byte[] buf2 = new byte[4096];
        boolean done1 = false;
        boolean done2 = false;

        try
        {
        while (!done1)
        {
            int off1 = 0;
            int off2 = 0;

            while (off1 < buf1.length)
            {
                int count = stream1.read(buf1, off1, buf1.length - off1);
                if (count < 0)
                {
                    done1 = true;
                    break;
                }
                off1 += count;
            }
            while (off2 < buf2.length)
            {
                int count = stream2.read(buf2, off2, buf2.length - off2);
                if (count < 0)
                {
                    done2 = true;
                    break;
                }
                off2 += count;
            }
            if (off1 != off2 || done1 != done2)
                return false;
            for (int i = 0; i < off1; i++)
            {
                if (buf1[i] != buf2[i])
                    return false;
            }
        }
        return true;
        }
        finally { stream1.close(); stream2.close(); }
    }
}
