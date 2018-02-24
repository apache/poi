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

import org.apache.xmlbeans.GDuration;
import org.apache.xmlbeans.GDurationSpecification;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlErrorCodes;

public abstract class JavaGDurationHolderEx extends XmlObjectBase
{
    public JavaGDurationHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    GDuration _value;
    private SchemaType _schemaType;

    public SchemaType schemaType() { return _schemaType; }

    protected void set_text(String s)
    {
        GDuration newVal;
        if (_validateOnSet())
            newVal = validateLexical(s, _schemaType, _voorVc);
        else
            newVal = lex(s, _voorVc);

        if (_validateOnSet() && newVal != null)
            validateValue(newVal, _schemaType, _voorVc);

        _value = newVal;
    }

    protected void set_GDuration(GDurationSpecification v)
    {
        if (_validateOnSet())
            validateValue(v, _schemaType, _voorVc);
        
        if (v.isImmutable() && (v instanceof GDuration))
            _value = (GDuration)v;
        else
            _value = new GDuration(v);
    }

    protected String compute_text(NamespaceManager nsm)
        { return _value == null ? "" : _value.toString(); }

    protected void set_nil()
    {
        _value = null;
    }

    public GDuration getGDurationValue()
    {
        check_dated();

        return _value == null ? null : _value;
    }

    public static GDuration lex(String v, ValidationContext context)
    {
        GDuration duration = null;
        
        try
        {
            duration = new GDuration(v);
        }
        catch (Exception e)
        {
            context.invalid(XmlErrorCodes.DURATION, new Object[] { v });
        }

        return duration;
    }

    public static GDuration validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        GDuration duration = lex(v, context);

        if (duration != null && sType.hasPatternFacet())
            if (!sType.matchPatternFacet(v))
                context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                    new Object[] { "duration", v, QNameHelper.readable(sType) });
        
        return duration;
    }

    public static void validateValue(GDurationSpecification v, SchemaType sType, ValidationContext context)
    {
        XmlObject x;
        GDuration g;
        
        if ((x = sType.getFacet(SchemaType.FACET_MIN_EXCLUSIVE)) != null)
            if (v.compareToGDuration(g = ((XmlObjectBase)x).gDurationValue()) <= 0)
                context.invalid(XmlErrorCodes.DATATYPE_MIN_EXCLUSIVE_VALID,
                    new Object[] { "duration", v, g, QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MIN_INCLUSIVE)) != null)
            if (v.compareToGDuration(g = ((XmlObjectBase)x).gDurationValue()) < 0)
                context.invalid(XmlErrorCodes.DATATYPE_MIN_INCLUSIVE_VALID,
                    new Object[] { "duration", v, g, QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_EXCLUSIVE)) != null)
            if (v.compareToGDuration(g = ((XmlObjectBase)x).gDurationValue()) >= 0)
                context.invalid(XmlErrorCodes.DATATYPE_MAX_EXCLUSIVE_VALID,
                    new Object[] { "duration", v, g, QNameHelper.readable(sType) });
        
        if ((x = sType.getFacet(SchemaType.FACET_MAX_INCLUSIVE)) != null)
            if (v.compareToGDuration(g = ((XmlObjectBase)x).gDurationValue()) > 0)
                context.invalid(XmlErrorCodes.DATATYPE_MAX_INCLUSIVE_VALID,
                    new Object[] { "duration", v, g, QNameHelper.readable(sType) });
        
        XmlObject[] vals = sType.getEnumerationValues();
        if (vals != null)
        {
            for (int i = 0; i < vals.length; i++)
                if (v.compareToGDuration(((XmlObjectBase)vals[i]).gDurationValue()) == 0)
                    return;
            context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "duration", v, QNameHelper.readable(sType) });
        }
    }
    
    protected int compare_to(XmlObject d)
    {
        return _value.compareToGDuration(((XmlObjectBase) d).gDurationValue());
    }

    protected boolean equal_to(XmlObject d)
    {
        return _value.equals(((XmlObjectBase) d).gDurationValue());
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
    
    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(gDurationValue(), schemaType(), ctx);
    }
}
