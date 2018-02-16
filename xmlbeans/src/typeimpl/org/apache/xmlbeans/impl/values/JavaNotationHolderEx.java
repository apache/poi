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
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.PrefixResolver;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;

public abstract class JavaNotationHolderEx extends JavaNotationHolder
{
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    public JavaNotationHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected int get_wscanon_rule()
    {
        return schemaType().getWhiteSpaceRule();
    }

    protected void set_text(String s)
    {
        if (_validateOnSet())
        {
            if (!check(s, _schemaType))
                throw new XmlValueOutOfRangeException();

            if (!_schemaType.matchPatternFacet(s))
                throw new XmlValueOutOfRangeException();
        }

        super.set_text(s);
    }

    protected void set_notation(String v)
    { set_text(v); }

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
            v = JavaNotationHolder.validateLexical(value.getStringValue(), _voorVc, NamespaceContext.getCurrent());

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
                    new Object[] { "NOTATION", v, QNameHelper.readable(sType) });
            }
        }

        check(v, sType);

        return name;
    }

    private static boolean check(String v, SchemaType sType)
    {
        // check against length
        XmlObject len = sType.getFacet(SchemaType.FACET_LENGTH);
        if (len != null)
        {
            int m = ((XmlObjectBase)len).getBigIntegerValue().intValue();
            if (!(v.length() != m))
                return false;
        }

        // check against min length
        XmlObject min = sType.getFacet(SchemaType.FACET_MIN_LENGTH);
        if (min != null)
        {
            int m = ((XmlObjectBase)min).getBigIntegerValue().intValue();
            if (!(v.length() >= m))
                return false;
        }

        // check against min length
        XmlObject max = sType.getFacet(SchemaType.FACET_MAX_LENGTH);
        if (max != null)
        {
            int m = ((XmlObjectBase)max).getBigIntegerValue().intValue();
            if (!(v.length() <= m))
                return false;
        }

        return true;
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
                new Object[] { "NOTATION", v, QNameHelper.readable(sType) });
        }
    }

}
