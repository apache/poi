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

import org.apache.xmlbeans.XmlErrorCodes;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.impl.common.ValidationContext;
import org.apache.xmlbeans.impl.common.QNameHelper;



public abstract class JavaBooleanHolderEx extends JavaBooleanHolder
{
    private SchemaType _schemaType;

    public SchemaType schemaType()
        { return _schemaType; }

    public static boolean validateLexical(String v, SchemaType sType, ValidationContext context)
    {
        boolean b = JavaBooleanHolder.validateLexical(v, context);
        validatePattern(v, sType, context);
        return b;
    }
    
    public static void validatePattern(String v, SchemaType sType, ValidationContext context)
    {
        // the only new facet that can apply to booleans is pattern!
        if (!sType.matchPatternFacet(v))
            context.invalid(XmlErrorCodes.DATATYPE_VALID$PATTERN_VALID,
                new Object[] { "boolean", v, QNameHelper.readable(sType) });
    }
    
    public JavaBooleanHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected void set_text(String s)
    {
        if (_validateOnSet())
            validatePattern(s, _schemaType, _voorVc);
        super.set_text(s);
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
    }
}
