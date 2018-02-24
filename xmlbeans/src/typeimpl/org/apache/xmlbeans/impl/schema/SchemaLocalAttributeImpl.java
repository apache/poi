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

import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaAnnotation;
import org.apache.xmlbeans.SchemaBookmark;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.soap.SOAPArrayType;
import org.apache.xmlbeans.soap.SchemaWSDLArrayType;
import org.apache.xmlbeans.impl.values.NamespaceContext;

import java.math.BigInteger;

public class SchemaLocalAttributeImpl implements SchemaLocalAttribute, SchemaWSDLArrayType
{
    public SchemaLocalAttributeImpl()
    {
    }

    public void init(QName name, SchemaType.Ref typeref, int use, String deftext, XmlObject parseObject, XmlValueRef defvalue, boolean isFixed, SOAPArrayType wsdlArray, SchemaAnnotation ann, Object userData)
    {
        if (_xmlName != null || _typeref != null)
            throw new IllegalStateException("Already initialized");
        _use = use;
        _typeref = typeref;
        _defaultText = deftext;
        _parseObject = parseObject;
        _defaultValue = defvalue;
        _isDefault = (deftext != null);
        _isFixed = isFixed;
        _xmlName = name;
        _wsdlArrayType = wsdlArray;
        _annotation = ann;
        _userData = userData;
    }

    private String _defaultText;
    /* package */ XmlValueRef _defaultValue;
    private boolean _isFixed;
    private boolean _isDefault;
    private QName _xmlName;
    private SchemaType.Ref _typeref;
    private SOAPArrayType _wsdlArrayType;
    private int _use;
    private SchemaAnnotation _annotation;
    protected XmlObject _parseObject; // for QName resolution
    private Object _userData;

    public boolean isTypeResolved()
    {
        return (_typeref != null);
    }

    public void resolveTypeRef(SchemaType.Ref typeref)
    {
        if (_typeref != null)
            throw new IllegalStateException();
        _typeref = typeref;
    }

    public int getUse()
        { return _use; }

    public QName getName()
        { return _xmlName; }

    public String getDefaultText()
        { return _defaultText; }

    public boolean isDefault()
        { return _isDefault; }

    public boolean isFixed()
        { return _isFixed; }

    public boolean isAttribute()
        { return true; }

    public SchemaAnnotation getAnnotation()
        { return _annotation; }

    public SchemaType getType()
        { return _typeref.get(); }

    public SchemaType.Ref getTypeRef()
        { return _typeref; }

    public BigInteger getMinOccurs()
        { return _use == REQUIRED ? BigInteger.ONE : BigInteger.ZERO; }

    public BigInteger getMaxOccurs()
        { return _use == PROHIBITED ? BigInteger.ZERO : BigInteger.ONE; }

    public boolean isNillable()
        { return false; }

    public SOAPArrayType getWSDLArrayType()
        { return _wsdlArrayType; }
    
    public XmlAnySimpleType getDefaultValue()
    {
        if (_defaultValue != null)
            return _defaultValue.get();
        if (_defaultText != null && XmlAnySimpleType.type.isAssignableFrom(getType()))
        {
            if (_parseObject != null)
            {
                try
                {
                    NamespaceContext.push(new NamespaceContext(_parseObject));
                    return getType().newValue(_defaultText);
                }
                finally
                {
                    NamespaceContext.pop();
                }
            }
            return getType().newValue(_defaultText);
        }
        return null;
    }

    public void setDefaultValue(XmlValueRef defaultRef)
        { _defaultValue = defaultRef; }

    public Object getUserData()
    {   return _userData; }
}
