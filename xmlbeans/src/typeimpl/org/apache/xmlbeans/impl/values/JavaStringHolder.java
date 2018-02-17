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

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

public class JavaStringHolder extends XmlObjectBase
{
    public JavaStringHolder() {}

    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_STRING; }

    private String _value;

    protected int get_wscanon_rule()
        { return SchemaType.WS_PRESERVE; }

    // SIMPLE VALUE ACCESSORS BELOW -------------------------------------------
    public String compute_text(NamespaceManager nsm) { return _value; }
    protected void set_text(String s) { _value = s; }
    protected void set_nil() { _value = null; }

    // string setter and getter already handled by XmlObjectBase

    // comparators
    protected boolean equal_to(XmlObject obj)
    {
        return _value.equals(((XmlObjectBase)obj).stringValue());
    }

    protected int value_hash_code()
    {
        return _value.hashCode();
    }
    protected boolean is_defaultable_ws(String v)
    {
        return false;
    }
}
