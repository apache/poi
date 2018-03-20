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

import org.apache.xmlbeans.SystemProperties;

import org.xml.sax.EntityResolver;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Author: Cezar Andrei (cezar.andrei at bea.com)
 * Date: Dec 3, 2003
 */
public class ResolverUtil
{
    private static EntityResolver _entityResolver = null;

    static
    {
        try
        {
            String erClassName = SystemProperties.getProperty("xmlbean.entityResolver");
            if (erClassName != null) {
                Object o = Class.forName(erClassName).newInstance();
                _entityResolver = (EntityResolver)o;
            }
        }
        catch (Exception e)
        {
            _entityResolver = null;
        }
    }

    public static EntityResolver getGlobalEntityResolver()
    {
        return _entityResolver;
    }

    public static EntityResolver resolverForCatalog(String catalogFile)
    {
        if (catalogFile==null)
            return null;

        try
        {
            Class cmClass = Class.forName("org.apache.xml.resolver.CatalogManager");
            Constructor cstrCm = cmClass.getConstructor(new Class[] {});
            Object cmObj = cstrCm.newInstance(new Object[] {});
            Method cmMethod = cmClass.getMethod("setCatalogFiles", new Class[] {String.class});
            cmMethod.invoke(cmObj, new String[] {catalogFile});

            Class crClass = Class.forName("org.apache.xml.resolver.tools.CatalogResolver");
            Constructor cstrCr = crClass.getConstructor(new Class[] {cmClass});
            Object crObj = cstrCr.newInstance(new Object[] {cmObj});

            return (EntityResolver)crObj;
        }
        catch( Exception e )
        {
            return null;
        }
    }
}
