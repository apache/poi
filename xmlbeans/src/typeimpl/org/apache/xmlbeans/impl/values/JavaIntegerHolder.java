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
import org.apache.xmlbeans.impl.common.ValidationContext;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

public abstract class JavaIntegerHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_INTEGER; }

    private BigInteger _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets/sets raw text value
    protected String compute_text(NamespaceManager nsm) { return _value.toString(); }
    protected void set_text(String s)
    {
        set_BigInteger(lex(s, _voorVc));
    }
    public static BigInteger lex(String s, ValidationContext vc)
    {
        if (s.length() > 0 && s.charAt( 0 ) == '+' )
            s = s.substring(1);

        try { return new BigInteger(s); }
        catch (Exception e) { vc.invalid(XmlErrorCodes.INTEGER, new Object[] { s }); return null; }
    }
    protected void set_nil()
    {
        _value = null;
    }
    // numerics: fractional
    public BigDecimal getBigDecimalValue() { check_dated(); return _value == null ? null : new BigDecimal(_value); }
    public BigInteger getBigIntegerValue() { check_dated(); return _value; }

    // setters
    protected void set_BigDecimal(BigDecimal v) { _value = v.toBigInteger(); }
    protected void set_BigInteger(BigInteger v) { _value = v; }

    // comparators
    protected int compare_to(XmlObject i)
    {
        if (((SimpleValue)i).instanceType().getDecimalSize() > SchemaType.SIZE_BIG_INTEGER)
            return -i.compareTo(this);

        return _value.compareTo(((XmlObjectBase)i).bigIntegerValue());
    }

    protected boolean equal_to(XmlObject i)
    {
        if (((SimpleValue)i).instanceType().getDecimalSize() > SchemaType.SIZE_BIG_INTEGER)
            return i.valueEquals(this);

        return _value.equals(((XmlObjectBase)i).bigIntegerValue());
    }

    static private BigInteger _maxlong = BigInteger.valueOf(Long.MAX_VALUE);
    static private BigInteger _minlong = BigInteger.valueOf(Long.MIN_VALUE);

    /**
     * Note, this is carefully aligned with hash codes for all xsd:decimal
     * primitives.
     */
    protected int value_hash_code()
    {
        if (_value.compareTo(_maxlong) > 0 ||
            _value.compareTo(_minlong) < 0)
            return _value.hashCode();

        long longval = _value.longValue();

        return (int)((longval >> 32) * 19 + longval);
    }
}
