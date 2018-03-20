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

import org.apache.xmlbeans.XmlQName;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.values.XmlQNameImpl;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Cezar Andrei (cezar.andrei at bea.com) Date: Jul 16, 2004
 */
public class Type
{
    private QName _name;

    private int _kind = SIMPLE_TYPE_SIMPLE_CONTENT;
    // _kind value space
    public static final int SIMPLE_TYPE_SIMPLE_CONTENT   = 1; // ie no atts, no elems, just text
    public static final int COMPLEX_TYPE_SIMPLE_CONTENT  = 2; // ie atts*, no elems, text*   - simple type extension
    public static final int COMPLEX_TYPE_COMPLEX_CONTENT = 3; // ie atts*, elems, no text
    public static final int COMPLEX_TYPE_MIXED_CONTENT   = 4; // ie atts*, elems, text
    public static final int COMPLEX_TYPE_EMPTY_CONTENT   = 5; // no elems, no text, just atts

    private int _topParticleForComplexOrMixedContent = PARTICLE_SEQUENCE;
    // _topParticleForComplexOrMixedContent
    public static final int PARTICLE_SEQUENCE         = 1;
    public static final int PARTICLE_CHOICE_UNBOUNDED = 2;

    private List _elements;  // size>0 COMPLEX
    private List _attributes; // size>0 COMPLEX

    private Type _extensionType;
    private boolean _isGlobal = false;

    private List _enumerationValues;
    private boolean _acceptsEnumerationValue = true;
    private List _enumerationQNames;                    // This is a special case where the lexical representation
                                                        // is not enough for a value, the QNames need to be remembered
                                                        // in case the _extensionType is QName

    protected Type()
    {}

    public static Type createNamedType(QName name, int contentType)
    {
        assert name!=null;
        Type type = new Type();
        type.setName(name);
        type.setContentType(contentType);
        return type;
    }

    public static Type createUnnamedType(int contentType)
    {
        assert contentType==SIMPLE_TYPE_SIMPLE_CONTENT ||
            contentType==COMPLEX_TYPE_SIMPLE_CONTENT ||
            contentType==COMPLEX_TYPE_COMPLEX_CONTENT ||
            contentType==COMPLEX_TYPE_MIXED_CONTENT ||
            contentType==COMPLEX_TYPE_EMPTY_CONTENT: "Unknown contentType: " + contentType;
        Type type = new Type();
        type.setContentType(contentType);
        return type;
    }


    public QName getName()
    {
        return _name;
    }

    public void setName(QName name)
    {
        this._name = name;
    }

    /**
     * @return
     *   SIMPLE_TYPE_SIMPLE_CONTENT   // ie no atts, no elems, just text
     *   COMPLEX_TYPE_SIMPLE_CONTENT  // ie atts*, no elems, text*   - simple type extension
     *   COMPLEX_TYPE_COMPLEX_CONTENT // ie atts*, elems, no text
     *   COMPLEX_TYPE_MIXED_CONTENT   // ie atts*, elems, text
     *   COMPLEX_TYPE_EMPTY_CONTENT   // no elems, no text, just atts
     */
    public int getContentType()
    {
        return _kind;
    }

    /**
     * @param kind 4 kinds:
     *   SIMPLE_TYPE_SIMPLE_CONTENT   // ie no atts, no elems, just text
     *   COMPLEX_TYPE_SIMPLE_CONTENT  // ie atts*, no elems, text*   - simple type extension
     *   COMPLEX_TYPE_COMPLEX_CONTENT // ie atts*, elems, no text
     *   COMPLEX_TYPE_MIXED_CONTENT   // ie atts*, elems, text
     *   COMPLEX_TYPE_EMPTY_CONTENT   // no elems, no text, just atts
     */
    public void setContentType(int kind)
    {
        this._kind = kind;
    }

    public List getElements()
    {
        ensureElements();
        return _elements;
    }

    public void addElement(Element element)
    {
        ensureElements();
        _elements.add(element);
    }

    public void setElements(List elements)
    {
        ensureElements();
        _elements.clear();
        _elements.addAll(elements);
    }

    private void ensureElements()
    {
        if (_elements==null)
            _elements = new ArrayList();
    }

    public List getAttributes()
    {
        ensureAttributes();
        return _attributes;
    }

    public void addAttribute(Attribute attribute)
    {
        ensureAttributes();
        _attributes.add(attribute);
    }

    public Attribute getAttribute(QName name)
    {
        for (int i = 0; i < _attributes.size(); i++)
        {
            Attribute attribute = (Attribute) _attributes.get(i);
            if (attribute.getName().equals(name))
                return attribute;
        }
        return null;
    }

    private void ensureAttributes()
    {
        if (_attributes==null)
            _attributes = new ArrayList();
    }

    public boolean isComplexType()
    {
        return (_kind==COMPLEX_TYPE_COMPLEX_CONTENT ||
            _kind==COMPLEX_TYPE_MIXED_CONTENT||
            _kind==COMPLEX_TYPE_SIMPLE_CONTENT);
    }

    public boolean hasSimpleContent()
    {
        return (_kind==SIMPLE_TYPE_SIMPLE_CONTENT ||
            _kind==COMPLEX_TYPE_SIMPLE_CONTENT);
    }

    /**
     * @return PARTICLE_SEQUENCE or PARTICLE_CHOICE_UNBOUNDED
     */
    public int getTopParticleForComplexOrMixedContent()
    {
        return _topParticleForComplexOrMixedContent;
    }

    /**
     * @param topParticleForComplexOrMixedContent  PARTICLE_SEQUENCE or PARTICLE_CHOICE_UNBOUNDED
     */
    public void setTopParticleForComplexOrMixedContent(int topParticleForComplexOrMixedContent)
    {
        this._topParticleForComplexOrMixedContent = topParticleForComplexOrMixedContent;
    }

    public boolean isGlobal()
    {
        return _isGlobal;
    }

    public void setGlobal(boolean isGlobal)
    {
        assert isGlobal && getName()!=null;
        _isGlobal = isGlobal;
    }

    public Type getExtensionType()
    {
        return _extensionType;
    }

    public void setExtensionType(Type extendedType)
    {
        assert _kind == COMPLEX_TYPE_SIMPLE_CONTENT : "Extension used only for type which are COMPLEX_TYPE_SIMPLE_CONTENT";
        assert extendedType!=null && extendedType.getName()!=null : "Extended type must be a named type.";
        _extensionType = extendedType;
    }

    public List getEnumerationValues()
    {
        ensureEnumerationValues();
        return _enumerationValues;
    }

    public List getEnumerationQNames()
    {
        ensureEnumerationValues();
        return _enumerationQNames;
    }

    public void addEnumerationValue(String enumerationValue, final XmlCursor xc)
    {
        assert _kind==SIMPLE_TYPE_SIMPLE_CONTENT || _kind==COMPLEX_TYPE_SIMPLE_CONTENT : "Enumerations possible only on simple content";
        ensureEnumerationValues();
        if (_acceptsEnumerationValue && !_enumerationValues.contains(enumerationValue))
        {
            _enumerationValues.add(enumerationValue);
            if (_name.equals(XmlQName.type.getName()))
            {
                // check for QName
                PrefixResolver prefixResolver = new PrefixResolver()
                {
                    public String getNamespaceForPrefix(String prefix)
                    {  return xc.namespaceForPrefix(prefix); }
                };

                QName qname = XmlQNameImpl.validateLexical(enumerationValue, null, prefixResolver);

                assert qname!=null : "The check for QName should allready have happened.";
                _enumerationQNames.add(qname);
            }
        }
    }

    private void ensureEnumerationValues()
    {
        if (_enumerationValues==null)
        {
            _enumerationValues = new ArrayList();
            _enumerationQNames = new ArrayList();
        }
    }

    public boolean isEnumeration()
    {
        return _acceptsEnumerationValue && _enumerationValues!=null && _enumerationValues.size()>1;
    }

    public boolean isQNameEnumeration()
    {
        return isEnumeration() && _name.equals(XmlQName.type.getName()) && _enumerationQNames!=null && _enumerationQNames.size()>1;
    }

    public void closeEnumeration()
    {
        _acceptsEnumerationValue=false;
    }

    public String toString()
    {
        return "Type{" +
            "_name = " + _name +
            ", _extensionType = " + _extensionType +
            ", _kind = " + _kind +
            ", _elements = " + _elements +
            ", _attributes = " + _attributes +
            "}";
    }

    public void addAllEnumerationsFrom(Type from)
    {
        assert _kind==SIMPLE_TYPE_SIMPLE_CONTENT || _kind==COMPLEX_TYPE_SIMPLE_CONTENT : "Enumerations possible only on simple content";
        ensureEnumerationValues();

        if (_name.equals(XmlQName.type.getName()) && from._name.equals(XmlQName.type.getName()))
        {
            for (int i = 0; i < from.getEnumerationValues().size(); i++)
            {
                String enumValue = (String) from.getEnumerationValues().get(i);
                QName enumQNameValue = (QName) from.getEnumerationQNames().get(i);
                if (_acceptsEnumerationValue && !_enumerationQNames.contains(enumQNameValue))
                {
                        _enumerationValues.add(enumValue);
                        _enumerationQNames.add(enumQNameValue);
                }
            }
        }
        else
        {
            for (int i = 0; i < from.getEnumerationValues().size(); i++)
            {
                String enumValue = (String) from.getEnumerationValues().get(i);
                if (_acceptsEnumerationValue && !_enumerationValues.contains(enumValue))
                {
                    _enumerationValues.add(enumValue);
                }
            }
        }
    }
}
