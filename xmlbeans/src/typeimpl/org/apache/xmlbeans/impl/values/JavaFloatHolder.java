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
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

import java.math.BigDecimal;
import java.math.BigInteger;

public abstract class JavaFloatHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_FLOAT; }

    private float _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets+sets raw text value
    protected String compute_text(NamespaceManager nsm) {
        return serialize(_value);
    }

    public static String serialize(float f)
    {
        if (f == Float.POSITIVE_INFINITY)
            return "INF";
        else if (f == Float.NEGATIVE_INFINITY)
            return "-INF";
        else if (f == Float.NaN)
            return "NaN";
        else
            return Float.toString(f);
    }
    protected void set_text(String s)
    {
        set_float(validateLexical(s,_voorVc));
    }
    public static float validateLexical(String v, ValidationContext context)
    {
        try
        {
            return XsTypeConverter.lexFloat(v);
        }
        catch(NumberFormatException e)
        {
            context.invalid(XmlErrorCodes.FLOAT, new Object[]{v});

            return Float.NaN;
        }
    }
    protected void set_nil()
    {
        _value = 0.0f;
    }
    // numerics: fractional
    public BigDecimal getBigDecimalValue() { check_dated(); return new BigDecimal(_value); }
    public double getDoubleValue() { check_dated(); return _value; }
    public float getFloatValue() { check_dated(); return _value; }

    // setters
    protected void set_double(double v) { set_float((float)v); }
    protected void set_float(float v) { _value = v; }
    protected void set_long(long v) { set_float((float)v); }
    protected void set_BigDecimal(BigDecimal v) { set_float(v.floatValue()); }
    protected void set_BigInteger(BigInteger v) { set_float(v.floatValue()); }

    // comparators
    protected int compare_to(XmlObject f)
    {
        return compare(_value,((XmlObjectBase)f).floatValue());
    }

    static int compare(float thisValue, float thatValue)
    {
        if (thisValue < thatValue) return -1;
        if (thisValue > thatValue) return  1;

        int thisBits = Float.floatToIntBits(thisValue);
        int thatBits = Float.floatToIntBits(thatValue);

        return thisBits == thatBits ? 0 : thisBits < thatBits ? -1 : 1;
    }

    protected boolean equal_to(XmlObject f)
    {
        return compare(_value, ((XmlObjectBase)f).floatValue()) == 0;
    }

    protected int value_hash_code()
    {
        return Float.floatToIntBits(_value);
    }
}
