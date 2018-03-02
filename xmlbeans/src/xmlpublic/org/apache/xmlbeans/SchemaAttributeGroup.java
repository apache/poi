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

import javax.xml.namespace.QName;

/**
 * Represents an attribute group.
 * <p>
 * An attribute group is a syntactic construct, not a part
 * of the logical model. For example, types declared within an
 * attribute group become local to the type that uses the group -
 * they're not local to the attribute group itself. Therefore
 * in the logical model of a schema type system, an attribute
 * group doesn't represent anything. Its contents are merged
 * into the logical model at parse time.
 * 
 * @see SchemaTypeLoader#findAttributeGroup
 */
public interface SchemaAttributeGroup extends SchemaComponent, SchemaAnnotated
{
    /**
     * Returns SchemaComponent.ATTRIBUTE_GROUP.
     */
    int getComponentType();

    /**
     * The name of the model group.
     */
    QName getName();

    /**
     * Used to allow on-demand loading of attribute groups.
     * 
     * @exclude
     */
    public final static class Ref extends SchemaComponent.Ref
    {
        public Ref(SchemaAttributeGroup attributeGroup)
            { super(attributeGroup); }

        public Ref(SchemaTypeSystem system, String handle)
            { super(system, handle); }

        public final int getComponentType()
            { return SchemaComponent.ATTRIBUTE_GROUP; }

        public final SchemaAttributeGroup get()
            { return (SchemaAttributeGroup)getComponent(); }
    }

    /**
     * Returns user-specific information.
     * @see SchemaBookmark
     */
    Object getUserData();
}
