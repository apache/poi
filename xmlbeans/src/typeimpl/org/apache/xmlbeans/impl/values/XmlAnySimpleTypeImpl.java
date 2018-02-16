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
import org.apache.xmlbeans.XmlAnySimpleType;
import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

/**
 * This class implements the anySimpleType for XML.
 *
 */
public class XmlAnySimpleTypeImpl extends XmlObjectBase implements XmlAnySimpleType
{
    public XmlAnySimpleTypeImpl(SchemaType type, boolean complex)
        { _schemaType = type; initComplexType(complex, false); }

    public XmlAnySimpleTypeImpl()
        { _schemaType = BuiltinSchemaTypeSystem.ST_ANY_SIMPLE; }

    public SchemaType schemaType()
        { return _schemaType; }

    private SchemaType _schemaType;

    String _textvalue = "";

    protected int get_wscanon_rule()
    {
        return SchemaType.WS_PRESERVE;
    }

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------
    // gets raw text value
    protected String compute_text(NamespaceManager nsm) { return _textvalue; }
    protected void set_text(String s)
    {
        _textvalue = s;
    }

    protected void set_nil()
    {
        _textvalue = null;
    }

    // comparators
    protected boolean equal_to(XmlObject obj)
    {
        // compares against another anySimpleType
        // rule is: lexical values must match.
        return _textvalue.equals(((XmlAnySimpleType)obj).getStringValue());
    }

    protected int value_hash_code()
    {
        // matches JavaStringHolder's value_hash_code, so we can be hased against strings
        return (_textvalue == null ? 0 : _textvalue.hashCode());
    }
}
