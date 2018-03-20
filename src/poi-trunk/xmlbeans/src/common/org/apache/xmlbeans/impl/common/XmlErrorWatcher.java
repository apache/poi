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

import org.apache.xmlbeans.XmlError;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.Collections;

public class XmlErrorWatcher extends AbstractCollection
{
    private Collection _underlying;
    private XmlError _firstError;

    public XmlErrorWatcher(Collection underlying)
    {
        _underlying = underlying;
    }

    public boolean add(Object o)
    {
        if (_firstError == null && o instanceof XmlError && ((XmlError)o).getSeverity() == XmlError.SEVERITY_ERROR)
            _firstError = (XmlError)o;
        if (_underlying == null)
            return false;
        return _underlying.add(o);
    }

    public Iterator iterator()
    {
        if (_underlying == null)
            return Collections.EMPTY_LIST.iterator();

        return _underlying.iterator();
    }

    public int size()
    {
        if (_underlying == null)
            return 0;

        return _underlying.size();
    }

    public boolean hasError()
    {
        return _firstError != null;
    }

    public XmlError firstError()
    {
        return _firstError;
    }
}
