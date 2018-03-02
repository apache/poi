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
package org.apache.xmlbeans.impl.store;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Constructor;

import org.apache.xmlbeans.impl.common.XPath;

public final class PathDelegate
{
    private static HashMap _constructors = new HashMap();

    private PathDelegate()
    {}

    private static synchronized void init(String implClassName)
    {
        // default to Saxon
        if (implClassName == null)
            implClassName = "org.apache.xmlbeans.impl.xpath.saxon.XBeansXPath";
        Class selectPathInterfaceImpl = null;
        boolean engineAvailable = true;
        try
        {
            selectPathInterfaceImpl = Class.forName(implClassName);
        }
        catch (ClassNotFoundException e)
        {
            engineAvailable = false;
        }
        catch (NoClassDefFoundError e)
        {
            engineAvailable = false;
        }

        if (engineAvailable)
        {
            try
            {
                Constructor constructor = selectPathInterfaceImpl.getConstructor(
                    new Class[] {String.class, String.class, Map.class, String.class});
                _constructors.put(implClassName, constructor);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public static synchronized SelectPathInterface createInstance(String implClassName,
            String xpath, String contextVar, Map namespaceMap)
    {
        if (_constructors.get(implClassName) == null)
            init(implClassName);

        if (_constructors.get(implClassName) == null)
            return null;

        Constructor constructor = (Constructor)_constructors.get(implClassName);
        try
        {
            Object defaultNS = namespaceMap.get(XPath._DEFAULT_ELT_NS);
            if (defaultNS != null)
                namespaceMap.remove(XPath._DEFAULT_ELT_NS);
            return (SelectPathInterface)constructor.newInstance(
                new Object[] {xpath, contextVar, namespaceMap, (String)defaultNS});
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    public static interface SelectPathInterface
    {
        public List selectPath(Object node);
    }
}
