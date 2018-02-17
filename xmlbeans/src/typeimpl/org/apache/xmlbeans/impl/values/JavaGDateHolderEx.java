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
import org.apache.xmlbeans.GDate;
import org.apache.xmlbeans.GDateSpecification;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;

import java.util.Date;
import java.util.Calendar;

public abstract class JavaGDateHolderEx extends XmlObjectBase
{
    public JavaGDateHolderEx(SchemaType type, boolean complex)
    {
        _schemaType = type;
        initComplexType(complex, false);
    }

    public SchemaType schemaType()
        { return _schemaType; }

    private SchemaType _schemaType;
    private GDate _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // sets/gets raw text value
    protected String compute_text(NamespaceManager nsm)
        { return _value == null ? "" : _value.toString(); }

    protected void set_text(String s)
    {
        GDate newVal;
        if (_validateOnSet())
            newVal = validateLexical(s, _schemaType, _voorVc);
        else
            newVal = lex(s, _schemaType, _voorVc);

        if (_validateOnSet() && newVal != null)
            validateValue(newVal, _schemaType, _voorVc);

        _value = newVal;
    }

    public static GDate lex(String v, SchemaType sType, ValidationContext context)
    {
        GDate date = null;

        try
        {
            date = new GDate(v);
        }
        catch (Exception e)
        {
            context.invalid(XmlErrorCodes.DATE, new Object[] { v });
        }

        if (date != null)
        {
            if (date.getBuiltinTypeCode() != sType.getPrimitiveType().getBuiltinTypeCode())
            {
                context.invalid(XmlErrorCodes.DATE, new Object[] { "wrong type: " + v });
                date = null;
            }
            else if (!date.isValid())
            {
                context.invalid(XmlErrorCodes.DATE, new Object[] { v });
                date = null;
            }
        }

        return date;
    }

    public static GDate validateLexical(String v, SchemaType sType, ValidationContext context)
    {

        GDate date = lex(v, sType, context);

        if (date != null && sType.hasPatternFacet())
            if (!sType.matchPatternFacet(v))
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "date", v, QNameHelper.readable(sType) });

        return date;
    }

    public static void validateValue(GDateSpecification v, SchemaType sType, ValidationContext context)
    {
        XmlObject x;
        GDate g;

        if (v.getBuiltinTypeCode() != sType.getPrimitiveType().getBuiltinTypeCode())
            context.invalid(XmlErrorCodes.DATE, new Object[] { "Date (" + v + ") does not have the set of fields required for " + QNameHelper.readable(sType) });

        if ((x = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) <= 0)
                context.invalid(XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID,
                    new Object[] { "date", v, g, QNameHelper.readable(sType) });

        if ((x = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) < 0)
                context.invalid(XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID,
                    new Object[] { "date", v, g, QNameHelper.readable(sType) });

        if ((x = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) >= 0)
                context.invalid(XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID,
                    new Object[] { "date", v, g, QNameHelper.readable(sType) });

        if ((x = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)) != null)
            if (v.compareToGDate(g = ((XmlObjectBase)x).gDateValue()) > 0)
                context.invalid(XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID,
                    new Object[] { "date", v, g, QNameHelper.readable(sType) });

        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (v.compareToGDate(((XmlObjectBase)vals[i]).gDateValue()) == 0)
                    return;
            context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "date", v, QNameHelper.readable(sType) });
        }
    }

    protected void set_nil()
    {
        _value = null;
    }

    // numerics: gYear, gMonth, gDay accept an integer
    public int getIntValue()
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        if (code != SchemaType.BTC_G_DAY &&
                code != SchemaType.BTC_G_MONTH &&
                code != SchemaType.BTC_G_YEAR)
            throw new XmlValueOutOfRangeException();

        check_dated();

        if (_value == null)
            return 0;

        switch (code)
        {
            case SchemaType.BTC_G_DAY:
                return _value.getDay();
            case SchemaType.BTC_G_MONTH:
                return _value.getMonth();
            case SchemaType.BTC_G_YEAR:
                return _value.getYear();
            default:
                assert(false);
                throw new IllegalStateException();
        }
    }

    public GDate getGDateValue()
    {
        check_dated();

        if (_value == null)
            return null;

        return _value;
    }
    
    public Calendar getCalendarValue()
    {
        check_dated();

        if (_value == null)
            return null;

        return _value.getCalendar();
    }

    public Date getDateValue()
    {
        check_dated();

        if (_value == null)
            return null;

        return _value.getDate();
    }

    // setters
    protected void set_int(int v)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        if (code != SchemaType.BTC_G_DAY &&
                code != SchemaType.BTC_G_MONTH &&
                code != SchemaType.BTC_G_YEAR)
            throw new XmlValueOutOfRangeException();

        GDateBuilder value = new GDateBuilder();

        switch (code)
        {
            case SchemaType.BTC_G_DAY:
                value.setDay(v); break;
            case SchemaType.BTC_G_MONTH:
                value.setMonth(v); break;
            case SchemaType.BTC_G_YEAR:
                value.setYear(v); break;
        }

        if (_validateOnSet())
            validateValue(value, _schemaType, _voorVc);

        _value = value.toGDate();
    }

    protected void set_GDate(GDateSpecification v)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        GDate candidate;

        if (v.isImmutable() && (v instanceof GDate) && v.getBuiltinTypeCode() == code)
            candidate = (GDate)v;
        else
        {
            // truncate extra fields from the date if necessary.
            if (v.getBuiltinTypeCode() != code)
            {
                GDateBuilder gDateBuilder = new GDateBuilder(v);
                gDateBuilder.setBuiltinTypeCode(code);
                v = gDateBuilder;
            }
            candidate = new GDate(v);
        }

        if (_validateOnSet())
            validateValue(candidate, _schemaType, _voorVc);

        _value = candidate;
    }
    
    protected void set_Calendar(Calendar c)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        GDateBuilder gDateBuilder = new GDateBuilder(c);
        gDateBuilder.setBuiltinTypeCode(code);
        GDate value = gDateBuilder.toGDate();
 
        if (_validateOnSet())
            validateValue(value, _schemaType, _voorVc);

        _value = value;
    }

    protected void set_Date(Date v)
    {
        int code = schemaType().getPrimitiveType().getBuiltinTypeCode();

        if (code != SchemaType.BTC_DATE && code != SchemaType.BTC_DATE_TIME ||
            v == null)
            throw new XmlValueOutOfRangeException();

        GDateBuilder gDateBuilder = new GDateBuilder(v);
        gDateBuilder.setBuiltinTypeCode(code);
        GDate value = gDateBuilder.toGDate();
 
        if (_validateOnSet())
            validateValue(value, _schemaType, _voorVc);

        _value = value;
    }


    // comparators
    protected int compare_to(XmlObject obj)
    {
        return _value.compareToGDate(((XmlObjectBase)obj).gDateValue());
    }

    protected boolean equal_to(XmlObject obj)
    {
        return _value.equals(((XmlObjectBase)obj).gDateValue());
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
    
    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(gDateValue(), schemaType(), ctx);
    }
    
}
