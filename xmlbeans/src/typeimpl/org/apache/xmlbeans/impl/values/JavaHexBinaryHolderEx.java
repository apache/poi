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
import org.apache.xmlbeans.impl.common.QNameHelper;
import org.apache.xmlbeans.impl.common.ValidationContext;


public abstract class JavaHexBinaryHolderEx extends JavaHexBinaryHolder
{
    private SchemaType _schemaType;


    public SchemaType schemaType()
        { return _schemaType; }

    public JavaHexBinaryHolderEx(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    protected int get_wscanon_rule()
    {
        return schemaType().getWhiteSpaceRule();
    }

    protected void set_text(String s)
    {
        byte[] v;

        if (_validateOnSet())
            v = validateLexical(s, schemaType(), _voorVc);
        else
            v = lex(s, _voorVc);

        if (_validateOnSet() && v != null)
            validateValue(v, schemaType(), XmlObjectBase._voorVc);
        
        super.set_ByteArray(v);

        _value = v;
    }

    // setters
    protected void set_ByteArray(byte[] v)
    {
        if (_validateOnSet())
            validateValue(v, schemaType(), _voorVc);
        
        super.set_ByteArray(v);
    }

    public static void validateValue(byte[] v, SchemaType sType, ValidationContext context)
    {
        int i;
        XmlObject o;

        if ((o = sType.getFacet(SchemaType.FACET_LENGTH)) != null)
        {
            if ((i = ((XmlObjectBase)o).bigIntegerValue().intValue()) != v.length)
            {
                context.invalid(XmlErrorCodes.DATATYPE_LENGTH_VALID$BINARY,
                    new Object[] { "hexBinary", new Integer(v.length), new Integer(i), QNameHelper.readable(sType) } );
            }
        }

        if ((o = sType.getFacet( SchemaType.FACET_MIN_LENGTH )) != null)
        {
            if ((i = ((XmlObjectBase)o).bigIntegerValue().intValue()) > v.length)
            {
                context.invalid(XmlErrorCodes.DATATYPE_MIN_LENGTH_VALID$BINARY,
                    new Object[] { "hexBinary", new Integer(v.length), new Integer(i), QNameHelper.readable(sType) } );
            }
        }

        if ((o = sType.getFacet( SchemaType.FACET_MAX_LENGTH )) != null)
        {
            if ((i = ((XmlObjectBase)o).bigIntegerValue().intValue()) < v.length)
            {
                context.invalid(XmlErrorCodes.DATATYPE_MAX_LENGTH_VALID$BINARY,
                    new Object[] { "hexBinary", new Integer(v.length), new Integer(i), QNameHelper.readable(sType) } );
            }
        }
        
        XmlObject[] vals = sType.getEnumerationValues();

        if (vals != null)
        {
            enumLoop: for ( i = 0 ; i < vals.length ; i++ )
            {
                byte[] enumBytes = ((XmlObjectBase)vals[i]).byteArrayValue();

                if (enumBytes.length != v.length)
                    continue;

                for ( int j = 0 ; j < enumBytes.length ; j++ )
                    if (enumBytes[j] != v[j])
                        continue enumLoop;
                
                break;
            }
            
            if (i >= vals.length)
                context.invalid(XmlErrorCodes.DATATYPE_ENUM_VALID$NO_VALUE,
                    new Object[] { "hexBinary", QNameHelper.readable(sType) });
        }
    }

    protected void validate_simpleval(String lexical, ValidationContext ctx)
    {
        validateLexical(lexical, schemaType(), ctx);
        validateValue(byteArrayValue(), schemaType(), ctx);
    }
}
