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

import org.apache.xmlbeans.impl.schema.BuiltinSchemaTypeSystem;

public abstract class JavaNotationHolder extends XmlQNameImpl
{
    public SchemaType schemaType()
        { return BuiltinSchemaTypeSystem.ST_NOTATION; }

//    protected int get_wscanon_rule()
//        { return SchemaType.WS_PRESERVE; }

}
