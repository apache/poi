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
import org.apache.xmlbeans.SimpleValue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

public abstract class JavaIntHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_INT; }

    private int _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets raw text value
    public String compute_text(NamespaceManager nsm) { return Long.toString(_value); }
    protected void set_text(String s)
    {
        try { set_int(XsTypeConverter.lexInt(s)); }
        catch (Exception e) { throw new XmlValueOutOfRangeException(XmlErrorCodes.INT, new Object[] { s }); }
    }
    protected void set_nil()
    {
        _value = 0;
    }
    // numerics: fractional
    public BigDecimal getBigDecimalValue() { check_dated(); return new BigDecimal((double) _value); }
    public BigInteger getBigIntegerValue() { check_dated(); return BigInteger.valueOf(_value); }
    public long getLongValue() { check_dated(); return _value; }
    public int getIntValue() { check_dated(); return _value; }

    static final BigInteger _max = BigInteger.valueOf(Integer.MAX_VALUE);
    static final BigInteger _min = BigInteger.valueOf(Integer.MIN_VALUE);

    // setters
    protected void set_BigDecimal(BigDecimal v) { set_BigInteger(v.toBigInteger()); }
    protected void set_BigInteger(BigInteger v)
    {
        if (v.compareTo(_max) > 0 || v.compareTo(_min) < 0)
            throw new XmlValueOutOfRangeException();
        set_int(v.intValue());
    }
    protected void set_long(long l)
    {
        if (l > Integer.MAX_VALUE || l < Integer.MIN_VALUE)
            throw new XmlValueOutOfRangeException();
        set_int((int)l);
    }
    protected void set_int(int i)
    {
        _value = i;
    }

    // comparators
    protected int compare_to(XmlObject i)
    {
        if (((SimpleValue)i).instanceType().getDecimalSize() > SchemaType.SIZE_INT)
            return -i.compareTo(this);

        return _value == ((XmlObjectBase)i).intValue() ? 0 :
               _value < ((XmlObjectBase)i).intValue() ? -1 : 1;
    }

    protected boolean equal_to(XmlObject i)
    {
        if (((SimpleValue)i).instanceType().getDecimalSize() > SchemaType.SIZE_INT)
            return i.valueEquals(this);

        return _value == ((XmlObjectBase)i).intValue();
    }

    /**
     * Note, this is carefully aligned with hash codes for all xsd:decimal
     * primitives.
     */
    protected int value_hash_code()
    {
        return _value;
    }
}
