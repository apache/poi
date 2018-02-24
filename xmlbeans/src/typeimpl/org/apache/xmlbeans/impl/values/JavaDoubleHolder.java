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

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class JavaDoubleHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_DOUBLE; }

    double _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets/sets raw text value
    protected String compute_text(NamespaceManager nsm) { return serialize(_value); }

    public static String serialize(double d)
    {
        if (d == Double.POSITIVE_INFINITY)
            return "INF";
        else if (d == Double.NEGATIVE_INFINITY)
            return "-INF";
        else if (d == Double.NaN)
            return "NaN";
        else
            return Double.toString(d);
    }
    protected void set_text(String s)
    {
        set_double(validateLexical(s,_voorVc));
    }
    public static double validateLexical(String v, ValidationContext context)
    {
        try
        {
            return XsTypeConverter.lexDouble(v);
        }
        catch(NumberFormatException e)
        {
            context.invalid(XmlErrorCodes.DOUBLE, new Object[]{v});

            return Double.NaN;
        }
    }
    protected void set_nil()
    {
        _value = 0.0;
    }

    // numerics: fractional
    public BigDecimal getBigDecimalValue() { check_dated(); return new BigDecimal(_value); }
    public double getDoubleValue() { check_dated(); return _value; }
    public float getFloatValue() { check_dated(); return (float)_value; }

    // setters
    protected void set_double(double v) { _value = v; }
    protected void set_float(float v) { set_double((double)v); }
    protected void set_long(long v) { set_double((double)v); }
    protected void set_BigDecimal(BigDecimal v) { set_double(v.doubleValue()); }
    protected void set_BigInteger(BigInteger v) { set_double(v.doubleValue()); }

    // comparators
    protected int compare_to(XmlObject d)
    {
        return compare(_value,((XmlObjectBase)d).doubleValue());
    }
    static int compare(double thisValue, double thatValue)
    {
        if (thisValue < thatValue) return -1;
        if (thisValue > thatValue) return  1;

        long thisBits = Double.doubleToLongBits(thisValue);
        long thatBits = Double.doubleToLongBits(thatValue);

        return thisBits == thatBits ? 0 : thisBits < thatBits ? -1 : 1;
    }

    protected boolean equal_to(XmlObject d)
    {
        return compare(_value, ((XmlObjectBase)d).doubleValue()) == 0;
    }

    protected int value_hash_code()
    {
        long v = Double.doubleToLongBits(_value);
        return (int)((v >> 32) * 19 + v);
    }
}
