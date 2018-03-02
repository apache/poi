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

package org.apache.xmlbeans;

/**
 * Represents a Schema bookmark. The XMLSchema compiler will copy the value
 * returned by {@link #getValue} when this bookmark is found in the XMLStore
 * to the corresponding Schema* objects, and the value will be accessible using
 * the <code>getUserInfo</code> method on these objects.
 *
 * @see SchemaType
 * @see SchemaField
 * @see SchemaAttributeGroup
 * @see SchemaModelGroup
 * @see SchemaIdentityConstraint
 */
public class SchemaBookmark extends XmlCursor.XmlBookmark
{
    private Object _value;

    public SchemaBookmark(Object value)
    {   _value = value; }

    /**
     * Getter. Called during the Schema compilation process.
     */
    public Object getValue()
    {   return _value; }
}
