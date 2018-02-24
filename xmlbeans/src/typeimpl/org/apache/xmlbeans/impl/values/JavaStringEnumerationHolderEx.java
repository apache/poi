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
import org.apache.xmlbeans.StringEnumAbstractBase;
import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;


public abstract class JavaStringEnumerationHolderEx extends JavaStringHolderEx
{
    public JavaStringEnumerationHolderEx(SchemaType type, boolean complex)
    {
        super(type, complex);
    }

    private StringEnumAbstractBase _val;

    // update the intval when setting via text, nil.
    protected void set_text(String s)
    {
        StringEnumAbstractBase se = schemaType().enumForString(s);
        if (se == null)
            throw new XmlValueOutOfRangeException(XmlErrorCodes.DATATYPE_ENUM_VALID,
                new Object[] { "string", s, QNameHelper.readable(schemaType()) });

        super.set_text(s);
        _val = se;
    }

    public static void validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        JavaStringHolderEx.validateLexical(v, sType, context);
    }

    protected void set_nil()
    {
        _val = null;
        super.set_nil();
    }

    // set/get the enum
    public StringEnumAbstractBase getEnumValue()
    {
        check_dated();
        return _val;
    }

    protected void set_enum(StringEnumAbstractBase se)
    {
        Class ejc = schemaType().getEnumJavaClass();
        if (ejc != null && !se.getClass().equals(ejc))
            throw new XmlValueOutOfRangeException();

        super.set_text(se.toString());
        _val = se;
    }
}
