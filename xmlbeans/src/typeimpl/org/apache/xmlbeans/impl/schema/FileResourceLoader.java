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

package org.apache.xmlbeans.impl.schema;

import org.apache.xmlbeans.ResourceLoader;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;

public class FileResourceLoader implements ResourceLoader
{
    private File _directory;
    private ZipFile _zipfile;

    public FileResourceLoader(File file) throws IOException
    {
        if (file.isDirectory())
            _directory = file;
        else
        {
            _zipfile = new ZipFile(file);
        }
    }

    public InputStream getResourceAsStream(String resourceName)
    {
        try
        {
            if (_zipfile != null)
            {
                ZipEntry entry = _zipfile.getEntry(resourceName);
                if (entry == null)
                    return null;
                return _zipfile.getInputStream(entry);
            }
            else
            {
                return new FileInputStream(new File(_directory, resourceName));
            }
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public void close()
    {
        if (_zipfile != null)
        {
            try
            {
                _zipfile.close();
            }
            catch (IOException e)
            {
                // oh well.
            }
            _zipfile = null;
        }
    }
}

