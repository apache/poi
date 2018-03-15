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

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.XmlBoolean;
import org.apache.xmlbeans.impl.common.ValidationContext;

public abstract class JavaBooleanHolder extends XmlObjectBase
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_BOOLEAN; }

    private boolean _value;

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------

    // gets raw text value
    protected String compute_text(NamespaceManager nsm) { return _value ? "true" : "false"; }
    protected void set_text(String s)
    {
        _value = validateLexical(s, _voorVc);
    }
    public static boolean validateLexical(String v, ValidationContext context)
    {
        if (v.equals("true") || v.equals("1"))
            return true;

        if (v.equals("false") || v.equals("0"))
            return false;

        context.invalid(XmlErrorCodes.BOOLEAN, new Object[]{ v });

        return false;
    }
    protected void set_nil()
    {
        _value = false;
    }
    // numerics: fractional
    public boolean getBooleanValue() { check_dated(); return _value; }

    // setters
    protected void set_boolean(boolean f)
    {
        _value = f;
    }

    // comparators
    protected int compare_to(XmlObject i)
    {
        // no ordering defined between true and false
        return _value == ((XmlBoolean)i).getBooleanValue() ? 0 : 2;
    }

    protected boolean equal_to(XmlObject i)
    {
        return _value == ((XmlBoolean)i).getBooleanValue();
    }

    protected int value_hash_code()
    {
        return _value ? 957379554 : 676335975;
    }
}
