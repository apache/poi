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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlAnySimpleType;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class XmlValueRef
{
    XmlAnySimpleType _obj;
    SchemaType.Ref _typeref;
    Object _initVal;

    public XmlValueRef(XmlAnySimpleType xobj)
    {
        if (xobj == null)
            throw new IllegalArgumentException();
        _obj = xobj;
    }

    XmlValueRef(SchemaType.Ref typeref, Object initVal)
    {
        if (typeref == null)
            throw new IllegalArgumentException();
        _typeref = typeref;
        _initVal = initVal;
    }

    synchronized XmlAnySimpleType get()
    {
        if (_obj == null)
        {
            SchemaType type = _typeref.get();
            if (type.getSimpleVariety() != SchemaType.LIST)
                _obj = type.newValue(_initVal);
            else
            {
                List actualVals = new ArrayList();
                for (Iterator i = ((List)_initVal).iterator(); i.hasNext(); )
                {
                    XmlValueRef ref = (XmlValueRef)i.next();
                    actualVals.add(ref.get());
                }
                _obj = type.newValue(actualVals);
            }
        }
        return _obj;
    }
}
