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
package org.apache.xmlbeans.impl.validator;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaLocalElement;
import org.apache.xmlbeans.SchemaParticle;
import org.apache.xmlbeans.SchemaLocalAttribute;
import org.apache.xmlbeans.SchemaAttributeModel;
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDuration;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.util.List;

/**
 * Extension of {@link ValidatingXMLStreamReader} to provide Post Schema Validation Info
 * over an XMLStreamReader.
 *
 * @author Cezar Andrei (cezar.andrei at bea.com)
 *         Date: Aug 17, 2004
 */
public class ValidatingInfoXMLStreamReader
    extends ValidatingXMLStreamReader
    implements XMLStreamReader
{
    public ValidatingInfoXMLStreamReader()
    {
        super();
    }

    private int _attCount = -1;
    private int _attIndex =  0;

    public int nextWithAttributes()
        throws XMLStreamException
    {
        if (_attIndex < _attCount)
        {
            validate_attribute(_attIndex);
            _attIndex ++;
            return XMLEvent.ATTRIBUTE;
        }
        else
            return next();
    }

    protected void validate_attributes(int attCount)
    {
        _attCount = attCount;
        _attIndex = 0;
    }


    /**
     * @return Returns the SchemaType of the current element.
     * This can be different than getCurrentElement().getType() if xsi:type attribute is used.
     * Null is returned if no schema type is available.
     * For attribute types use {@link #getCurrentAttribute()}.getType().
     * Warning: the returned SchemaType can be an {@link org.apache.xmlbeans.XmlBeans#NO_TYPE},
     * see {@link org.apache.xmlbeans.SchemaType#isNoType}. Or can be the parent type, for unrecognized elements
     * that are part of wildcards.
     */
    public SchemaType getCurrentElementSchemaType()
    {
        return _validator==null ? null : _validator.getCurrentElementSchemaType();
    }

    /**
     * @return Returns the curent local element, null if one is not available, see {@link #getCurrentWildcardElement()}.
     */
    public SchemaLocalElement getCurrentElement ( )
    {
        return _validator==null ? null : _validator.getCurrentElement();
    }

    /**
     * @return Returns the current particle, if this is a wildcard particle
     * {@link org.apache.xmlbeans.SchemaParticle#WILDCARD} method {@link #getCurrentElement()}
     * might return null if wildcard's processContents is skip or lax.
     */
    public SchemaParticle getCurrentWildcardElement()
    {
        return _validator==null ? null : _validator.getCurrentWildcardElement();
    }

    /**
     * @return Returns the curent local attribute, global attribute if the current attribute is part of an
     * attribute wildcard, or null if none is available.
     */
    public SchemaLocalAttribute getCurrentAttribute()
    {
        return _validator==null ? null : _validator.getCurrentAttribute();
    }

    /**
     * @return Returns the attribute model for attributes if available, else null is returned.
     */
    public SchemaAttributeModel getCurrentWildcardAttribute()
    {
        return _validator==null ? null : _validator.getCurrentWildcardAttribute();
    }

    public String getStringValue()
    {
        return _validator==null ? null : _validator.getStringValue();
    }

    public BigDecimal getDecimalValue()
    {
        return _validator==null ? null : _validator.getDecimalValue();
    }

    public boolean getBooleanValue()
    {
        return _validator==null ? false : _validator.getBooleanValue();
    }

    public float getFloatValue()
    {
        return _validator==null ? 0 : _validator.getFloatValue();
    }

    public double getDoubleValue()
    {
        return _validator==null ? 0 : _validator.getDoubleValue();
    }

    public QName getQNameValue()
    {
        return _validator==null ? null : _validator.getQNameValue();
    }

    public GDate getGDateValue()
    {
        return _validator==null ? null : _validator.getGDateValue();
    }

    public GDuration getGDurationValue()
    {
        return _validator==null ? null : _validator.getGDurationValue();
    }

    public byte[] getByteArrayValue()
    {
        return _validator==null ? null : _validator.getByteArrayValue();
    }

    public List getListValue()
    {
        return _validator==null ? null : _validator.getListValue();
    }

    public List getListTypes()
    {
        return _validator==null ? null : _validator.getListTypes();
    }

    public SchemaType getUnionType()
    {
        return _validator==null ? null : _validator.getUnionType();
    }
}
