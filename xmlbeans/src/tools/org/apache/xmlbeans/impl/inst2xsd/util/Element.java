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
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 16, 2004
 */
public class Element
{
    private QName _name = null; // if isRef is true is the name of the referenced name
    private Element _ref = null;
    private boolean _isGlobal = false;
    private int _minOccurs = 1;
    private int _maxOccurs = 1;
    public static final int UNBOUNDED = -1;
    private boolean _isNillable = false;
    private Type _type = null;
    private String _comment = null;

    public QName getName()
    {
        return _name;
    }

    public void setName(QName name)
    {
        _name = name;
    }

    public boolean isRef()
    {
        return _ref!=null;
    }

    public Element getRef()
    {
        return _ref;
    }

    public void setRef(Element ref)
    {
        assert _isGlobal==false;
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
        _minOccurs = 1;
        _maxOccurs = 1;
    }

    public int getMinOccurs()
    {
        return _minOccurs;
    }

    public void setMinOccurs(int minOccurs)
    {
        _minOccurs = minOccurs;
    }

    public int getMaxOccurs()
    {
        return _maxOccurs;
    }

    public void setMaxOccurs(int maxOccurs)
    {
        _maxOccurs = maxOccurs;
    }

    public boolean isNillable()
    {
        return _isNillable;
    }

    public void setNillable(boolean isNillable)
    {
        _isNillable = isNillable;
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

    public String getComment()
    {
        return _comment;
    }

    public void setComment(String comment)
    {
        _comment = comment;
    }

    public String toString()
    {
        return "\n  Element{" +
            " _name = " + _name +
            ", _ref = " + (_ref!=null) +
            ", _isGlobal = " + _isGlobal +
            ", _minOccurs = " + _minOccurs +
            ", _maxOccurs = " + _maxOccurs +
            ", _isNillable = " + _isNillable +
            ", _comment = " + _comment +
            ",\n    _type = " + ( _type==null ? "null" :
                (_type.isGlobal() ? _type.getName().toString() : _type.toString())) +
            "\n  }";
    }
}
