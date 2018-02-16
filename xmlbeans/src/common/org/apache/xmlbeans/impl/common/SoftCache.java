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

import java.util.HashMap;
import java.lang.ref.SoftReference;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Apr 26, 2005
 */
public class SoftCache
{
    private HashMap map = new HashMap();

    public Object get(Object key)
    {
        SoftReference softRef = (SoftReference)map.get(key);

        if (softRef==null)
            return null;

        return softRef.get();
    }

    public Object put(Object key, Object value)
    {
        SoftReference softRef = (SoftReference)map.put(key, new SoftReference(value));

        if (softRef==null)
            return null;

        Object oldValue = softRef.get();
        softRef.clear();

        return oldValue;
    }

    public Object remove(Object key)
    {
        SoftReference softRef = (SoftReference)map.remove(key);

        if (softRef==null)
            return null;

        Object oldValue = softRef.get();
        softRef.clear();

        return oldValue;
    }
}