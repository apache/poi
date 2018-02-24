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

import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaField;

/**
 * This interface is used by the TypeStore to visit every element in order
 * to compute nillable flags and default values.
 */
public interface TypeStoreVisitor
{
    /**
     * When using a visitor, you must call "visit" on every qname of
     * every element in order up to the one you're interested in.
     *
     * If you're using it for validation, call visit(null) at the end
     * of the sequence of children. If you're not validating, you can
     * just walk away once you get the info you need.
     */
    boolean visit(QName eltName);

    /**
     * Returns the elementflags for this element.
     */
    int get_elementflags();

    /**
     * Returns the default text for this element.
     */
    String get_default_text();

    /**
     * Returns the schema field for this field.
     */
    SchemaField get_schema_field();
}
