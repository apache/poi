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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PathResourceLoader implements ResourceLoader
{
    private ResourceLoader[] _path;

    public PathResourceLoader(ResourceLoader[] loaderpath) throws IOException
    {
        _path = new ResourceLoader[loaderpath.length];
        System.arraycopy(loaderpath, 0, _path, 0, _path.length);
    }

    public PathResourceLoader(File[] filepath)
    {
        List pathlist = new ArrayList();
        for (int i = 0; i < filepath.length; i++)
        {
            try
            {
                ResourceLoader path = new FileResourceLoader(filepath[i]);
                pathlist.add(path);
            }
            catch (IOException e)
            {
                continue; // can't read a file on classpath? skip it.
            }
        }
        _path = (ResourceLoader[])pathlist.toArray(new ResourceLoader[pathlist.size()]);
    }

    public InputStream getResourceAsStream(String resourceName)
    {
        for (int i = 0; i < _path.length; i++)
        {
            InputStream result = _path[i].getResourceAsStream(resourceName);
            if (result != null)
                return result;
        }
        return null;
    }

    public void close()
    {
        for (int i = 0; i < _path.length; i++)
        {
            try
            {
                _path[i].close();
            }
            catch (Exception e)
            {
                // oh well.
            }
        }
    }
}
