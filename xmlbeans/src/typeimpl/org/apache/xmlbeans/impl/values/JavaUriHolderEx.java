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
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;

public class JavaUriHolderEx extends JavaUriHolder
{
    private SchemaType _schemaType;

    public SchemaType schemaType()
        { return _schemaType; }

    public JavaUriHolderEx(SchemaType type, boolean complex)
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

//    // setters
//    protected void set_uri(URI uri)
//    {
//        if (!check(uri.toString(), _schemaType))
//            throw new XmlValueOutOfRangeException();
//
//        super.set_uri(uri);
//    }

    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        XmlAnyUriImpl.validateLexical(v, context);
        
        XmlObject[] vals = sType.getEnumerationValues();

        if (vals != null)
        {
            int i;
            
            for ( i = 0 ; i < vals.length ; i++ )
            {
                String e = ((SimpleValue)vals[i]).getStringValue();

                if (e.equals( v ))
                    break;
            }
            
            if (i >= vals.length)
                context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                    new Object[] { "anyURI", v, QNameHelper.readable(sType) });
        }
        
        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                // TODO - describe string and pattern here in error
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "anyURI", v, QNameHelper.readable(sType) });
            }
        }

        XmlObject x;
        int i;

        if ((x = sType.getFacet(SchemaType.FACET_LENGTH)) != null)
            if ((i = ((SimpleValue)x).getBigIntegerValue().intValue()) != v.length())
                context.invalid(XmlErrorCodes.DATATYPE_LENGTH_VALID$STRING,
                    new Object[] { "anyURI", v, new Integer(i), QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MIN_LENGTH)) != null)
            if ((i = ((SimpleValue)x).getBigIntegerValue().intValue()) > v.length())
                context.invalid(XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$STRING,
                    new Object[] { "anyURI", v, new Integer(i), QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_LENGTH)) != null)
            if ((i = ((SimpleValue)x).getBigIntegerValue().intValue()) < v.length())
                context.invalid(XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$STRING,
                    new Object[] { "anyURI", v, new Integer(i), QNameHelper.readable(sType) });
    }
    
    private static boolean check(String v, SchemaType sType)
    {
        int length = v==null ? 0 : v.length();
        // check against length
        XmlObject len = sType.getFacet(SchemaType.FACET_LENGTH);
        if (len != null)
        {
            int m = ((SimpleValue)len).getBigIntegerValue().intValue();
            if (!(length != m))
                return false;
        }

        // check against min length
        XmlObject min = sType.getFacet(SchemaType.FACET_MIN_LENGTH);
        if (min != null)
        {
            int m = ((SimpleValue)min).getBigIntegerValue().intValue();
            if (!(length >= m))
                return false;
        }

        // check against min length
        XmlObject max = sType.getFacet(SchemaType.FACET_MAX_LENGTH);
        if (max != null)
        {
            int m = ((SimpleValue)max).getBigIntegerValue().intValue();
            if (!(length <= m))
                return false;
        }

        return true;
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(stringValue(), schemaType(), ctx);
    }
}
