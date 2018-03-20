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

package org.apache.xmlbeans.impl.values;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;

public abstract class JavaQNameHolderEx extends JavaQNameHolder
{
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    public JavaQNameHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected int get_wscanon_rule()
    {
        return schemaType().getWhiteSpaceRule();
    }

    protected void set_text(String s)
    {
        PrefixResolver resolver = NamespaceContext.getCurrent();

        if (resolver == null && has_store())
            resolver = get_store();

        QName v;
        if (_validateOnSet())
        {
            v = validateLexical(s, _schemaType, _voorVc, resolver);
            if (v != null)
                validateValue(v, _schemaType, _voorVc);
        }
        else
            v = JavaQNameHolder.validateLexical(s, _voorVc, resolver);

        super.set_QName(v);
    }

    protected void set_QName(QName name)
    {
        if (_validateOnSet())
            validateValue(name, _schemaType, _voorVc);
        super.set_QName( name );
    }

    protected void set_xmlanysimple(XmlAnySimpleType value)
    {
        QName v;
        if (_validateOnSet())
        {
            v = validateLexical(value.getStringValue(), _schemaType, _voorVc, NamespaceContext.getCurrent());

            if (v != null)
                validateValue(v, _schemaType, _voorVc);
        }
        else
            v = JavaQNameHolder.validateLexical(value.getStringValue(), _voorVc, NamespaceContext.getCurrent());

        super.set_QName(v);
    }

    public static QName validateLexical(String v, SchemaType sType, ValidationContext context, PrefixResolver resolver)
    {
        QName name = JavaQNameHolder.validateLexical(v, context, resolver);

        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                // TODO - describe string and pattern here in error
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "QName", v, QNameHelper.readable(sType) });
            }
        }

        /* length, minLength and maxLength facets deprecated - see http://www.w3.org/TR/xmlschema-2/#QName
        XmlObject x;
        int i;

        if ((x = sType.getFacet(SchemaType.FACET_LENGTH)) != null)
            if ((i = ((XmlObjectBase)x).bigIntegerValue().intValue()) != v.length())
                context.invalid(XmlErrorCodes.DATATYPE_LENGTH_VALID$STRING,
                    new Object[] { "QName", v, new Integer(i), QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MIN_LENGTH)) != null)
            if ((i = ((XmlObjectBase)x).bigIntegerValue().intValue()) > v.length())
                context.invalid(XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$STRING,
                    new Object[] { "QName", v, new Integer(i), QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_LENGTH)) != null)
            if ((i = ((XmlObjectBase)x).bigIntegerValue().intValue()) < v.length())
                context.invalid(XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$STRING,
                    new Object[] { "QName", v, new Integer(i), QNameHelper.readable(sType) });
        */

        return name;
    }

    public static void validateValue(QName v, SchemaType sType, ValidationContext context)
    {
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (v.equals(((XmlObjectBase)vals[i]).getQNameValue()))
                    return;
            context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "QName", v, QNameHelper.readable(sType) });
        }
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateValue(getQNameValue(), schemaType(), ctx);
    }

}
