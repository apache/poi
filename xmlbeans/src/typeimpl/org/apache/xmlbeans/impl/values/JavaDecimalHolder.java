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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

public class JavaDecimalHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_DECIMAL; }

    private BigDecimal _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // sets/gets raw text value
    protected String compute_text(NamespaceManager nsm) { return XsTypeConverter.printDecimal(_value); }
    protected void set_text(String s)
    {
        if (_validateOnSet())
            validateLexical(s, _voorVc);

        try {
            set_BigDecimal(new BigDecimal(s));
        }
        catch (NumberFormatException e)
        {
            _voorVc.invalid(XmlErrorCodes.DECIMAL, new Object[] { s });
        }
    }
    protected void set_nil()
    {
        _value = null;
    }

    /**
     * Performs lexical validation only.
     */

    public static void validateLexical(String v, ValidationContext context)
    {
        // TODO - will want to validate Chars with built in white space handling
        //        However, this fcn sometimes takes a value with wsr applied
        //        already
        int l = v.length();
        int i = 0;

        if (i < l)
        {
            int ch = v.charAt(i);

            if (ch == '+' || ch == '-')
                i++;
        }

        boolean sawDot = false;
        boolean sawDigit = false;

        for ( ; i < l ; i++ )
        {
            int ch = v.charAt(i);

            if (ch == '.')
            {
                if (sawDot)
                {
                    context.invalid(XmlErrorCodes.DECIMAL,
                        new Object[] { "saw '.' more than once: " + v });
                    return;
                }

                sawDot = true;
            }
            else if (ch >= '0' && ch <= '9')
            {
                sawDigit = true;
            }
            else
            {
                // TODO - may need to escape error char
                context.invalid(XmlErrorCodes.DECIMAL,
                    new Object[] { "unexpected char '" + ch + "'" });
                return;
            }
        }

        if (!sawDigit)
        {
            context.invalid(XmlErrorCodes.DECIMAL,
                new Object[] { "expected at least one digit" });
            return;
        }
    }

    // numerics: fractional
    public BigDecimal getBigDecimalValue() { check_dated(); return _value; }

    // setters
    protected void set_BigDecimal(BigDecimal v) { _value = v; }

    // comparators
    protected int compare_to(XmlObject decimal)
    {
        return _value.compareTo(((XmlObjectBase)decimal).bigDecimalValue());
    }
    protected boolean equal_to(XmlObject decimal)
    {
        return (_value.compareTo(((XmlObjectBase)decimal).bigDecimalValue())) == 0;
    }

    static private BigInteger _maxlong = BigInteger.valueOf(Long.MAX_VALUE);
    static private BigInteger _minlong = BigInteger.valueOf(Long.MIN_VALUE);

    /**
     * Note, this is carefully aligned with hash codes for all xsd:decimal
     * primitives.
     */
    protected int value_hash_code()
    {
        if (_value.scale() > 0)
        {
            if (_value.setScale(0, BigDecimal.ROUND_DOWN).compareTo(_value) != 0)
                return decimalHashCode();
        }

        BigInteger intval = _value.toBigInteger();

        if (intval.compareTo(_maxlong) > 0 ||
            intval.compareTo(_minlong) < 0)
            return intval.hashCode();

        long longval = intval.longValue();

        return (int)((longval >> 32) * 19 + longval);
    }

    /**
     * This method will has BigDecimals with the same arithmetic value to
     * the same hash code (eg, 2.3 & 2.30 will have the same hash.)
     * This differs from BigDecimal.hashCode()
     */
    protected int decimalHashCode() {
        assert _value.scale() > 0;

        // Get decimal value as string, and strip off zeroes on the right
        String strValue = _value.toString();
        int i;
        for (i = strValue.length() - 1 ; i >= 0 ; i --)
            if (strValue.charAt(i) != '0') break;

        assert strValue.indexOf('.') < i;

        // Return the canonicalized string hashcode
        return strValue.substring(0, i + 1).hashCode();
    }
}
