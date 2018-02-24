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

import java.math.BigInteger;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlPositiveInteger;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;

public class JavaIntegerHolderEx extends JavaIntegerHolder
{
    public JavaIntegerHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }
    
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    protected void set_text(String s)
    {
        BigInteger v = lex(s, _voorVc);
        
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);
    
        if (_validateOnSet())
            validateLexical(s, _schemaType, _voorVc);

        super.set_BigInteger(v);
    }
    
    protected void set_BigInteger(BigInteger v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);

        super.set_BigInteger(v);
    }
    
    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaDecimalHolder.validateLexical(v, context);
        if ( v.lastIndexOf('.')>=0 )
            context.invalid(XmlErrorCodes.INTEGER,
                new Object[] { v });

        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "integer", v, QNameHelper.readable(sType) });
            }
        }
    }

    private static void validateValue(BigInteger v, SchemaType sType, ValidationContext context)
    {
        // total digits
        XmlPositiveInteger td = (XmlPositiveInteger)sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        if (td != null)
        {
            String temp = v.toString();
            int len = temp.length();
            if (len > 0 && temp.charAt(0) == '-')
                len -= 1;
            if (len > td.getBigIntegerValue().intValue())
            {
                context.invalid(XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID,
                    new Object[] { new Integer(len), temp, new Integer(td.getBigIntegerValue().intValue()), QNameHelper.readable(sType) });
                return;
            }
        }

        // min ex
        XmlObject mine = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (mine != null)
        {
            BigInteger m = getBigIntegerValue(mine);
            if (!(v.compareTo(m) > 0))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID,
                    new Object[] { "integer", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // min in
        XmlObject mini = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (mini != null)
        {
            BigInteger m = getBigIntegerValue(mini);
            if (!(v.compareTo(m) >= 0))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID,
                    new Object[] { "integer", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // max in
        XmlObject maxi = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (maxi != null)
        {
            BigInteger m = getBigIntegerValue(maxi);
            if (!(v.compareTo(m) <= 0))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID,
                    new Object[] { "integer", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // max ex
        XmlObject maxe = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (maxe != null)
        {
            BigInteger m = getBigIntegerValue(maxe);
            if (!(v.compareTo(m) < 0))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID,
                    new Object[] { "integer", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // enumeration
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
            {
                if (v.equals(getBigIntegerValue(vals[i])))
                    return;
            }
            context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "integer", v, QNameHelper.readable(sType) });
        }
    }

    private static BigInteger getBigIntegerValue(XmlObject o)
    {
        SchemaType s = o.schemaType();
        switch (s.getDecimalSize()) 
        {
            case SchemaType.SIZE_BIG_DECIMAL:
                return ((XmlObjectBase)o).bigDecimalValue().toBigInteger();
            case SchemaType.SIZE_BIG_INTEGER:
                return ((XmlObjectBase)o).bigIntegerValue();
            default:
                throw new IllegalStateException("Bad facet type for Big Int: " + s);
        }
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(getBigIntegerValue(), schemaType(), ctx);
    }
    
}
