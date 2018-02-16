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
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.util.XsTypeConverter;

public abstract class JavaLongHolderEx extends JavaLongHolder
{
    public JavaLongHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }
        
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    protected void set_text(String s)
    {
        long v;

        try { v = XsTypeConverter.lexLong(s); }
        catch (Exception e) { throw new XmlValueOutOfRangeException(); }
        
        if (_validateOnSet())
        {
            validateValue(v, _schemaType, _voorVc);
            validateLexical(s, _schemaType, _voorVc);
        }

        super.set_long(v);
    }
    
    protected void set_long(long v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);

        super.set_long(v);
    }

    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaDecimalHolder.validateLexical(v, context);

        // check pattern
        if (sType.hasPatternFacet())
        {
            if (!sType.matchPatternFacet(v))
            {
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "long", v, QNameHelper.readable(sType) });
            }
        }
    }
    
    private static void validateValue(long v, SchemaType sType, ValidationContext context)
    {
        // total digits
        XmlObject td = sType.getFacet(SchemaType.FACET_TOTAL_DIGITS);
        if (td != null)
        {
            long m = getLongValue(td);
            String temp = Long.toString(v);
            int len = temp.length();
            if (len > 0 && temp.charAt(0) == '-')
                len -= 1;
            if (len > m)
            {
                context.invalid(XmlErrorCodes.DATATYPE_TOTAL_DIGITS_VALID,
                    new Object[] { new Integer(len), temp, new Long(m), QNameHelper.readable(sType) });
                return;
            }
        }

        // min ex
        XmlObject mine = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE);
        if (mine != null)
        {
            long m = getLongValue(mine);
            if (!(v > m))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID,
                    new Object[] { "long", new Long(v), new Long(m), QNameHelper.readable(sType) });
                return;
            }
        }

        // min in
        XmlObject mini = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE);
        if (mini != null)
        {
            long m = getLongValue(mini);
            if (!(v >= m))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID,
                    new Object[] { "long", new Long(v), new Long(m), QNameHelper.readable(sType) });
                return;
            }
        }

        // max in
        XmlObject maxi = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE);
        if (maxi != null)
        {
            long m = getLongValue(maxi);
            if (!(v <= m))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID,
                    new Object[] { "long", new Long(v), new Long(m), QNameHelper.readable(sType) });
                return;
            }
        }

        // max ex
        XmlObject maxe = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE);
        if (maxe != null)
        {
            long m = getLongValue(maxe);
            if (!(v < m))
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID,
                    new Object[] { "long", new Long(v), new Long(m), QNameHelper.readable(sType) });
                return;
            }
        }

        // enumeration
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
            {
                if (v == getLongValue(vals[i]))
                    return;
            }
            context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "long", new Long(v), QNameHelper.readable(sType) });
        }
    }

    private static long getLongValue(XmlObject o) {
        SchemaType s = o.schemaType();
        switch (s.getDecimalSize()) 
        {
            case SchemaType.SIZE_BIG_DECIMAL:
                return ((XmlObjectBase)o).getBigDecimalValue().longValue();
            case SchemaType.SIZE_BIG_INTEGER:
                return ((XmlObjectBase)o).getBigIntegerValue().longValue();
            case SchemaType.SIZE_LONG:
                return ((XmlObjectBase)o).getLongValue();
            default:
                throw new IllegalStateException("Bad facet type: " + s);
        }

    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(getLongValue(), schemaType(), ctx);
    }
}
