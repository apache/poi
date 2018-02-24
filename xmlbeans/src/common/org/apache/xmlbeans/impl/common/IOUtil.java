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

package org.apache.xmlbeans.impl.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.Writer;
import java.io.Reader;
import java.io.FileWriter;
import java.io.FileReader;
import java.net.URI;
import java.nio.channels.FileChannel;

public class IOUtil
{
    public static void copyCompletely(InputStream input, OutputStream output)
        throws IOException
    {
        //if both are file streams, use channel IO
        if ((output instanceof FileOutputStream) && (input instanceof FileInputStream))
        {
            try
            {
                FileChannel target = ((FileOutputStream) output).getChannel();
                FileChannel source = ((FileInputStream) input).getChannel();
                
                source.transferTo(0, Integer.MAX_VALUE, target);
                
                source.close();
                target.close();
                
                return;
            }
            catch (Exception e)
            { /* failover to byte stream version */ }
        }
        
        byte[] buf = new byte[8192];
        while (true)
        {
            int length = input.read(buf);
            if (length < 0)
                break;
            output.write(buf, 0, length);
        }
        
        try { input.close(); } catch (IOException ignore) {}
        try { output.close(); } catch (IOException ignore) {}
    }

    public static void copyCompletely(Reader input, Writer output)
        throws IOException
    {
        char[] buf = new char[8192];
        while (true)
        {
            int length = input.read(buf);
            if (length < 0)
                break;
            output.write(buf, 0, length);
        }

        try { input.close(); } catch (IOException ignore) {}
        try { output.close(); } catch (IOException ignore) {}
    }

    public static void copyCompletely(URI input, URI output)
        throws IOException
    {
        try
        {
            InputStream in = null;
            try
            {
                File f = new File(input);
                if (f.exists())
                    in = new FileInputStream(f);
            }
            catch (Exception notAFile)
            {}
            
            File out = new File(output);
            File dir = out.getParentFile();
            dir.mkdirs();
            
            if (in == null)
                in = input.toURL().openStream();
                
            IOUtil.copyCompletely(in, new FileOutputStream(out));
        }
        catch (IllegalArgumentException e)
        {
            throw new IOException("Cannot copy to " + output);
        }
    }

    public static File createDir(File rootdir, String subdir)
    {
        File newdir = (subdir == null) ? rootdir : new File(rootdir, subdir);
        boolean created = (newdir.exists() && newdir.isDirectory()) || newdir.mkdirs();
        assert(created) : "Could not create " + newdir.getAbsolutePath();
        return newdir;
    }
}
