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

import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.impl.values.NamespaceContext;

import java.math.BigInteger;

import javax.xml.namespace.QName;

public class SchemaParticleImpl implements SchemaParticle
{
    private int _particleType;
    private BigInteger _minOccurs;
    private BigInteger _maxOccurs;
    private SchemaParticle[] _particleChildren;
    private boolean _isImmutable;
    private QNameSet _startSet;
    private QNameSet _excludeNextSet;
    private boolean _isSkippable;
    private boolean _isDeterministic;
    private int _intMinOccurs;
    private int _intMaxOccurs;
    private QNameSet _wildcardSet;
    private int _wildcardProcess;
    private String _defaultText;
    private boolean _isDefault;
    private boolean _isFixed;
    private QName _qName;
    private boolean _isNillable;
    private SchemaType.Ref _typeref;
    protected XmlObject _parseObject;
    private Object _userData;
    private XmlValueRef _defaultValue;

    protected void mutate()
        { if (_isImmutable) throw new IllegalStateException(); }

    public void setImmutable()
        { mutate(); _isImmutable = true; }

    public boolean hasTransitionRules()
        { return (_startSet != null); }

    public boolean hasTransitionNotes()
        { return (_excludeNextSet != null); }

    public void setTransitionRules(QNameSet start,
                                   boolean isSkippable)
    {
        _startSet = start;
        _isSkippable = isSkippable;
    }

    public void setTransitionNotes(QNameSet excludeNext, boolean isDeterministic)
    {
        _excludeNextSet = excludeNext;
        _isDeterministic = isDeterministic;
    }

    public boolean canStartWithElement(QName name)
        { return name != null && _startSet.contains(name); }

    public QNameSet acceptedStartNames()
        { return _startSet; }

    public QNameSet getExcludeNextSet()
        { return _excludeNextSet; }

    public boolean isSkippable()
        { return _isSkippable; }

    public boolean isDeterministic()
        { return _isDeterministic; }

    public int getParticleType()
        { return _particleType; }

    public void setParticleType(int pType)
        { mutate(); _particleType = pType; }

    public boolean isSingleton()
        { return _maxOccurs != null &&
                 _maxOccurs.compareTo(BigInteger.ONE) == 0 &&
                 _minOccurs.compareTo(BigInteger.ONE) == 0; }

    public BigInteger getMinOccurs()
        { return _minOccurs; }

    public void setMinOccurs(BigInteger min)
        { mutate(); _minOccurs = min; _intMinOccurs = pegBigInteger(min); }

    public int getIntMinOccurs()
        { return _intMinOccurs; }

    public BigInteger getMaxOccurs()
        { return _maxOccurs; }

    public int getIntMaxOccurs()
        { return _intMaxOccurs; }

    public void setMaxOccurs(BigInteger max)
        { mutate(); _maxOccurs = max; _intMaxOccurs = pegBigInteger(max); }

    public SchemaParticle[] getParticleChildren()
    {
        if (_particleChildren == null)
        {
            assert _particleType != SchemaParticle.ALL &&
                _particleType != SchemaParticle.SEQUENCE &&
                _particleType != SchemaParticle.CHOICE;
            return null;
        }
        SchemaParticle[] result = new SchemaParticle[_particleChildren.length];
        System.arraycopy(_particleChildren, 0, result, 0, _particleChildren.length);
        return result;
    }

    public void setParticleChildren(SchemaParticle[] children)
        { mutate(); _particleChildren = children; }

    public SchemaParticle getParticleChild(int i)
        { return _particleChildren[i]; }

    public int countOfParticleChild()
        { return _particleChildren == null ? 0 : _particleChildren.length; }

    public void setWildcardSet(QNameSet set)
        { mutate(); _wildcardSet = set; }

    public QNameSet getWildcardSet()
        { return _wildcardSet; }

    public void setWildcardProcess(int process)
        { mutate(); _wildcardProcess = process; }

    public int getWildcardProcess()
        { return _wildcardProcess; }

    private static final BigInteger _maxint = BigInteger.valueOf(Integer.MAX_VALUE);

    private static final int pegBigInteger(BigInteger bi)
    {
        if (bi == null)
            return Integer.MAX_VALUE;
        if (bi.signum() <= 0)
            return 0;
        if (bi.compareTo(_maxint) >= 0)
            return Integer.MAX_VALUE;
        return bi.intValue();
    }

    public QName getName()
        { return _qName; }

    public void setNameAndTypeRef(QName formname, SchemaType.Ref typeref)
        { mutate(); _qName = formname; _typeref = typeref; }

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

    public boolean isAttribute()
        { return false; }

    public SchemaType getType()
        { if (_typeref == null) return null; return _typeref.get(); }

    public String getDefaultText()
        { return _defaultText; }

    public boolean isDefault()
        { return _isDefault; }

    public boolean isFixed()
        { return _isFixed; }

    public void setDefault(String deftext, boolean isFixed, XmlObject parseObject)
    {
        mutate();
        _defaultText = deftext;
        _isDefault = (deftext != null);
        _isFixed = isFixed;
        _parseObject = parseObject;
    }

    public boolean isNillable()
        { return _isNillable; }

    public void setNillable(boolean nillable)
        { mutate(); _isNillable = nillable; }

    public XmlAnySimpleType getDefaultValue()
    {
        if (_defaultValue != null)
            return _defaultValue.get();
        if (_defaultText != null && XmlAnySimpleType.type.isAssignableFrom(getType()))
        {
            if (_parseObject != null && XmlQName.type.isAssignableFrom(getType()))
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
    {
        mutate();
        _defaultValue = defaultRef;
    }

    public Object getUserData()
    {
        return _userData;
    }

    public void setUserData(Object data)
    {
        _userData = data;
    }
}
