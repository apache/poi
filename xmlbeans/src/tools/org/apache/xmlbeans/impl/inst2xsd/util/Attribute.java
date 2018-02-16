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
package org.apache.xmlbeans.impl.inst2xsd.util;

import javax.xml.namespace.QName;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 18, 2004
 */
public class Attribute
{
    private QName _name;
    private Type _type;
    private Attribute _ref = null;
    private boolean _isGlobal = false;
    private boolean _isOptional = false;

    public QName getName()
    {
        return _name;
    }

    public void setName(QName name)
    {
        _name = name;
    }

    public Type getType()
    {
        return isRef() ? getRef().getType() : _type;
    }

    public void setType(Type type)
    {
        assert !isRef();
        _type = type;
    }

    public boolean isRef()
    {
        return _ref!=null;
    }

    public Attribute getRef()
    {
        return _ref;
    }

    public void setRef(Attribute ref)
    {
        assert !isGlobal();
        _ref = ref;
        _type = null;
    }

    public boolean isGlobal()
    {
        return _isGlobal;
    }

    public void setGlobal(boolean isGlobal)
    {
        _isGlobal = isGlobal;
    }

    public boolean isOptional()
    {
        return _isOptional;
    }

    public void setOptional(boolean isOptional)
    {
        assert isOptional && !isGlobal() : "Global attributes cannot be optional.";
        _isOptional = isOptional;
    }

    public String toString()
    {
        return "\n    Attribute{" +
            "_name=" + _name +
            ", _type=" + _type +
            ", _ref=" + (_ref!=null) +
            ", _isGlobal=" + _isGlobal +
            ", _isOptional=" + _isOptional +
            "}";
    }
}
