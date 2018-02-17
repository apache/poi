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

import java.lang.ref.SoftReference;
import java.util.ArrayList;

import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.SystemProperties;

/**
 * This class encapsulates the caching strategy for XmlBeans.
 * By subclassing this, a client of XmlBeans can implement caches that are
 * more suitable for different applications using information that XmlBeans
 * cannot know.
 * <p/>
 * This class works as a singleton and as a default implementation for the cache.
 * You can set a particular implementation using the "xmlbean.systemcacheimpl"
 * system property or using the static {@link set} method.
 * Subclasses of this need to be thread-safe. An implementation can be replaced
 * at any time, so use of static variables is discouraged to ensure proper cleanup.
 */
public class SystemCache
{
    private static SystemCache INSTANCE = new SystemCache();

    static
    {
        String cacheClass = SystemProperties.getProperty("xmlbean.systemcacheimpl");
        Object impl = null;
        if (cacheClass != null)
        {
            try
            {
                impl = Class.forName(cacheClass).newInstance();
                if (!(impl instanceof SystemCache))
                throw new ClassCastException("Value for system property " +
                    "\"xmlbean.systemcacheimpl\" points to a class (" + cacheClass +
                    ") which does not derive from SystemCache");
            }
            catch (ClassNotFoundException cnfe)
            {
                throw new RuntimeException("Cache class " + cacheClass +
                    " specified by \"xmlbean.systemcacheimpl\" was not found.",
                    cnfe);
            }
            catch (InstantiationException ie)
            {
                throw new RuntimeException("Could not instantiate class " +
                    cacheClass + " as specified by \"xmlbean.systemcacheimpl\"." +
                    " An empty constructor may be missing.", ie);
            }
            catch (IllegalAccessException iae)
            {
                throw new RuntimeException("Could not instantiate class " +
                    cacheClass + " as specified by \"xmlbean.systemcacheimpl\"." +
                    " A public empty constructor may be missing.", iae);
            }
        }
        if (impl != null)
            INSTANCE = (SystemCache) impl;
    }

    public static synchronized final void set(SystemCache instance)
    {
        INSTANCE = instance;
    }

    public static final SystemCache get()
    {
        return INSTANCE;
    }

    public SchemaTypeLoader getFromTypeLoaderCache(ClassLoader cl)
    {
        return null;
    }

    public void addToTypeLoaderCache(SchemaTypeLoader stl, ClassLoader cl)
    {
        return;
    }

    private ThreadLocal tl_saxLoaders = new ThreadLocal();

    public Object getSaxLoader()
    {
        SoftReference s = (SoftReference) tl_saxLoaders.get();
        if (s == null)
            return null;
        else
            return s.get();
    }

    public void setSaxLoader(Object saxLoader)
    {
        tl_saxLoaders.set(new SoftReference(saxLoader));
    }
}
