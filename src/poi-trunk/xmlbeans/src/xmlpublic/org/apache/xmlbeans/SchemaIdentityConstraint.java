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
import java.util.Map;

/**
 * Represents an identity constraint definition.
 */ 
public interface SchemaIdentityConstraint extends SchemaComponent, SchemaAnnotated
{
    /**
     * Return the selector xpath as a string.
     */
    String getSelector();

    /**
     * Return a compiled xpath object for the selector.
     */
    Object getSelectorPath();

    /**
     * Return (a copy of) the xpaths for all the fields.
     */
    String[] getFields();

    /**
     * Return a compiled xpath object for the field.
     */
    Object getFieldPath(int index);

    /**
     * Return a read-only copy of the namespace map. This is the 
     * set of prefix to URI mappings that were in scope in the
     * schema at the point at which this constraint was declared
     */
    Map getNSMap();

    /** A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-key">xs:key</a> constraint.  See {@link #getConstraintCategory}. */
    public static final int CC_KEY = 1;
    /** A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-key">xs:keyRef</a> constraint.  See {@link #getConstraintCategory}. */
    public static final int CC_KEYREF = 2;
    /** A <a target="_blank" href="http://www.w3.org/TR/xmlschema-1/#declare-key">xs:unique</a> constraint.  See {@link #getConstraintCategory}. */
    public static final int CC_UNIQUE = 3;

    /**
     * Return the constraint category. Either {@link #CC_KEY}, {@link #CC_KEYREF},
     * or {@link #CC_UNIQUE}.
     */
    int getConstraintCategory();

    /**
     * Returns the key that a key ref refers to. Only valid for
     * keyrefs.
     */
    SchemaIdentityConstraint getReferencedKey();

    /**
     * Used to allow on-demand loading of identity constraints.
     * 
     * @exclude
     */
    public static final class Ref extends SchemaComponent.Ref
    {
        public Ref(SchemaIdentityConstraint idc)
            { super(idc); }

        public Ref(SchemaTypeSystem system, String handle)
            { super(system, handle); }

        public final int getComponentType()
            { return SchemaComponent.IDENTITY_CONSTRAINT; }

        public final SchemaIdentityConstraint get()
            { return (SchemaIdentityConstraint)getComponent(); }
    }

    /**
     * Returns user-specific information.
     * @see SchemaBookmark
     */
    Object getUserData();
}
