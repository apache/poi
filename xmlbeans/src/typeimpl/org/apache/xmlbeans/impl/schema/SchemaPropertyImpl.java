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

import org.apache.xmlbeans.SchemaComponent;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaGlobalAttribute;
import org.apache.xmlbeans.SchemaProperty;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaField;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.NamespaceContext;

import java.math.BigInteger;
import java.util.Set;

import javax.xml.namespace.QName;

public class SchemaPropertyImpl implements SchemaProperty
{
    private QName _name;
    private SchemaType.Ref _typeref;
    private boolean _isAttribute;
    private SchemaType.Ref _containerTypeRef;
    private String _javaPropertyName;
    private BigInteger _minOccurs;
    private BigInteger _maxOccurs;
    private int _hasNillable;
    private int _hasDefault;
    private int _hasFixed;
    private String _defaultText;
    private boolean _isImmutable;
    private SchemaType.Ref _javaBasedOnTypeRef;
    private boolean _extendsSingleton;
    private boolean _extendsArray;
    private boolean _extendsOption;
    private int _javaTypeCode;
    private QNameSet _javaSetterDelimiter;
    private XmlValueRef _defaultValue;
    private Set _acceptedNames;

    private void mutate()
        { if (_isImmutable) throw new IllegalStateException(); }

    public void setImmutable()
        { mutate(); _isImmutable = true; }

    public SchemaType getContainerType()
        { return _containerTypeRef.get(); }

    public void setContainerTypeRef(SchemaType.Ref typeref)
        { mutate(); _containerTypeRef = typeref; }

    public QName getName()
        { return _name; }

    public void setName(QName name)
        { mutate(); _name = name; }

    public String getJavaPropertyName()
        { return _javaPropertyName; }

    public void setJavaPropertyName(String name)
        { mutate(); _javaPropertyName = name; }

    public boolean isAttribute()
        { return _isAttribute; }

    public void setAttribute(boolean isAttribute)
        { mutate(); _isAttribute = isAttribute; }

    public boolean isReadOnly()
        { return false; }

    public SchemaType getType()
        { return _typeref.get(); }

    public void setTypeRef(SchemaType.Ref typeref)
        { mutate(); _typeref = typeref; }

    public SchemaType javaBasedOnType()
        { return _javaBasedOnTypeRef == null ? null : _javaBasedOnTypeRef.get(); }

    public boolean extendsJavaSingleton()
        { return _extendsSingleton; }

    public boolean extendsJavaArray()
        { return _extendsArray; }

    public boolean extendsJavaOption()
        { return _extendsOption; }

    public void setExtendsJava(SchemaType.Ref javaBasedOnTypeRef, boolean singleton, boolean option, boolean array)
    {
        mutate();
        _javaBasedOnTypeRef = javaBasedOnTypeRef;
        _extendsSingleton = singleton;
        _extendsOption = option;
        _extendsArray = array;
    }

    public QNameSet getJavaSetterDelimiter()
    {
        if (_isAttribute)
            return QNameSet.EMPTY;
        if (_javaSetterDelimiter == null)
            ((SchemaTypeImpl) getContainerType()).assignJavaElementSetterModel();
        assert _javaSetterDelimiter != null;
        return _javaSetterDelimiter;
    }

    void setJavaSetterDelimiter(QNameSet set)
        { _javaSetterDelimiter = set; }

    public QName[] acceptedNames()
    { 
        if (_acceptedNames == null)
            return new QName[] { _name };

        return (QName[])_acceptedNames.toArray(new QName[_acceptedNames.size()]); 
    }

    public void setAcceptedNames(Set set)
    {
        mutate(); 
        _acceptedNames = set;
    }
    public void setAcceptedNames(QNameSet set)
    { 
        mutate(); 
        _acceptedNames = set.includedQNamesInExcludedURIs();
    }

    public BigInteger getMinOccurs()
        { return _minOccurs; }

    public void setMinOccurs(BigInteger min)
        { mutate(); _minOccurs = min; }

    public BigInteger getMaxOccurs()
        { return _maxOccurs; }

    public void setMaxOccurs(BigInteger max)
        { mutate(); _maxOccurs = max; }

    public int hasNillable()
        { return _hasNillable; }

    public void setNillable(int when)
        { mutate(); _hasNillable = when; }

    public int hasDefault()
        { return _hasDefault; }

    public void setDefault(int when)
        { mutate(); _hasDefault = when; }

    public int hasFixed()
        { return _hasFixed; }

    public void setFixed(int when)
        { mutate(); _hasFixed = when; }

    public String getDefaultText()
        { return _defaultText; }

    public void setDefaultText(String val)
        { mutate(); _defaultText = val; }

    public XmlAnySimpleType getDefaultValue()
    {
        if (_defaultValue != null)
            return _defaultValue.get();
        return null;
    }
    
    public void setDefaultValue(XmlValueRef defaultRef)
    {
        mutate();
        _defaultValue = defaultRef;
    }

    public int getJavaTypeCode()
        { return _javaTypeCode; }

    public void setJavaTypeCode(int code)
        { mutate(); _javaTypeCode = code; }
}
