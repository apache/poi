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
import java.util.Iterator;
import java.util.Collections;
import java.net.URI;

public class XmlErrorPrinter extends AbstractCollection
{
    private boolean _noisy;
    private URI _baseURI;

    public XmlErrorPrinter(boolean noisy, URI baseURI)
    {
        _noisy = noisy;
        _baseURI = baseURI;
    }

    public boolean add(Object o)
    {
        if (o instanceof XmlError)
        {
            XmlError err = (XmlError)o;
            if (err.getSeverity() == XmlError.SEVERITY_ERROR ||
                err.getSeverity() == XmlError.SEVERITY_WARNING)
                System.err.println(err.toString(_baseURI));
            else if (_noisy)
                System.out.println(err.toString(_baseURI));
        }
        return false;
    }

    public Iterator iterator()
    {
        return Collections.EMPTY_LIST.iterator();
    }

    public int size()
    {
        return 0;
    }
}

