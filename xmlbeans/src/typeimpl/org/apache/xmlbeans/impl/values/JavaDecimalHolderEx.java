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
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;


import java.math.BigDecimal;

public abstract class JavaDecimalHolderEx extends JavaDecimalHolder
{
    private SchemaType _schemaType;

    public SchemaType schemaType()
        { return _schemaType; }

    public JavaDecimalHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected void set_text(String s)
    {
        if (_validateOnSet())
            validateLexical(s, _schemaType, _voorVc);

        BigDecimal v = null;
        try {
            v = new BigDecimal(s);
        }
        catch (NumberFormatException e)
        {
            _voorVc.invalid(XmlErrorCodes.DECIMAL, new Object[] { s });
        }

        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);

        super.set_BigDecimal(v);
    }
    
    protected void set_BigDecimal(BigDecimal v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);
        super.set_BigDecimal(v);
    }
    
    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaDecimalHolder.validateLexical(v, context);
        
        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                // TODO - describe string and pattern here in error
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "decimal", v, QNameHelper.readable(sType) });
            }
        }
    }
    
    /**
     * Performs facet validation only.
     */

    public static void validateValue(BigDecimal v, SchemaType sType, ValidationContext context)
    {
        // fractional digits
        XmlObject fd = sType.getFacet(SchemaType.FACET_FRACTION_DIGITS);
        if (fd != null)
        {
            int scale = ((XmlObjectBase)fd).getBigIntegerValue().intValue();
            try
            {
                // used only for side-effect - this does not change v despite
                // the name of the method
                v.setScale(scale);
            }
            catch(ArithmeticException e)
            {
                // ArithmeticException will be thrown if cannot represent as an Integer
                // with this scale - i.e. would need a fraction which would correspond
                // to digits beyond the allowed number
                context.invalid(XmlErrorCodes.DATATYPE_FRACTION_DIGITS_VALID,
                    new Object[] { new Integer(v.scale()), v.toString(), new Integer(scale), QNameHelper.readable(sType) });
                return;
            }
        }

        // total digits
        XmlObject td = sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        if (td != null)
        {
            String temp = v.unscaledValue().toString();
            int tdf = ((XmlObjectBase)td).getBigIntegerValue().intValue();
            int origLen = temp.length();
            int len = origLen;
            if (origLen > 0)
            {
                // don't count leading minus
                if (temp.charAt(0) == '-')
                {
                    len -= 1;
                }

                // don't count trailing zeros if we can absorb them into scale
                int insignificantTrailingZeros = 0;
                int vScale = v.scale();
                for(int j = origLen-1;
                    temp.charAt(j) == '0' && j > 0 && insignificantTrailingZeros < vScale;
                    j--)
                {
                    insignificantTrailingZeros++;
                }

                len -= insignificantTrailingZeros;
            }

            if (len > tdf)
            {
                context.invalid(XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID,
                    new Object[] { new Integer(len), v.toString(), new Integer(tdf), QNameHelper.readable(sType) });
                return;
            }
        }

        // min ex
        XmlObject mine = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (mine != null)
        {
            BigDecimal m = ((XmlObjectBase)mine).getBigDecimalValue();
            if (v.compareTo(m) <= 0)
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID,
                    new Object[] { "decimal", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // min in
        XmlObject mini = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (mini != null)
        {
            BigDecimal m = ((XmlObjectBase)mini).getBigDecimalValue();
            if (v.compareTo(m) < 0)
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID,
                    new Object[] { "decimal", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // max in
        XmlObject maxi = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (maxi != null)
        {
            BigDecimal m = ((XmlObjectBase)maxi).getBigDecimalValue();
            if (v.compareTo(m) > 0)
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID,
                    new Object[] { "decimal", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // max ex
        XmlObject maxe = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (maxe != null)
        {
            BigDecimal m = ((XmlObjectBase)maxe).getBigDecimalValue();
            if (v.compareTo(m) >= 0)
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID,
                    new Object[] { "decimal", v, m, QNameHelper.readable(sType) });
                return;
            }
        }

        // enumeration
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (v.equals(((XmlObjectBase)vals[i]).getBigDecimalValue()))
                    return;
            context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "decimal", v, QNameHelper.readable(sType) });
        }
    }
    
    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(getBigDecimalValue(), schemaType(), ctx);
    }

}
