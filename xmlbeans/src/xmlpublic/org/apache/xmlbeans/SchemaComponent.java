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
 * Represents a global Schema Component.  That is, a type, element, attribute,
 * model group, attribute group, or identity constraint.
 * <p>
 * Note that not all types, elements, and attributes are global; local
 * types, element, and attributes do not appear in the global lookup table.
 * Also note that other information items such as particles, facets, and
 * so on are not globally indexed, so are not SchemaComponents.
 * 
 * @see SchemaType
 * @see SchemaGlobalElement
 * @see SchemaGlobalAttribute
 * @see SchemaAttributeGroup
 * @see SchemaModelGroup
 * @see SchemaIdentityConstraint
 */
public interface SchemaComponent
{
    /** A type definition.  See {@link #getComponentType} */
    static final int TYPE = 0;
    /** An element definition.  See {@link #getComponentType} */
    static final int ELEMENT = 1;
    /** An attribute definition.  See {@link #getComponentType} */
    static final int ATTRIBUTE = 3;
    /** An attribute group definition.  See {@link #getComponentType} */
    static final int ATTRIBUTE_GROUP = 4;
    /** An identity constraint definition.  See {@link #getComponentType} */
    static final int IDENTITY_CONSTRAINT = 5;
    /** A model group definition.  See {@link #getComponentType} */
    static final int MODEL_GROUP = 6;
    /** A notation definition.  See {@link #getComponentType} */
    static final int NOTATION = 7;
    /** An annotation. See {@link #getComponentType} */
    static final int ANNOTATION = 8;

    /**
     * Returns the type code for the schema object, either {@link #TYPE},
     * {@link #ELEMENT}, {@link #ATTRIBUTE}, {@link #ATTRIBUTE_GROUP},
     * {@link #MODEL_GROUP}, {@link #IDENTITY_CONSTRAINT}, or {@link #NOTATION}.
     */
    int getComponentType();

    /**
     * Returns the typesystem within which this component definition resides
     */
    SchemaTypeSystem getTypeSystem();

    /**
     * The name of the schema component
     */
    QName getName();

    /**
     * The name of resource that represends the source .xsd in which this component was defined (if known)
     * <br/>See: {@link org.apache.xmlbeans.SchemaTypeLoader#getSourceAsStream(String)}
     * <br/><br/>Example:<pre>
     *   SchemaType schemaType = ..;
     *   InputStream is = schemaType.getTypeSystem().getSourceAsStream(schemaType.getSourceName());
     * </pre>
     */
    String getSourceName();

    /**
     * A lazy reference to a component. Used by SchemaTypeLoaders to
     * avoid loading components until they are actually needed.
     * 
     * @exclude
     */
    public static abstract class Ref
    {
        protected Ref(SchemaComponent schemaComponent)
            { _schemaComponent = schemaComponent; }

        protected Ref(SchemaTypeSystem schemaTypeSystem, String handle)
            { assert(handle != null); _schemaTypeSystem = schemaTypeSystem; _handle = handle; }

        private volatile SchemaComponent _schemaComponent;
        private SchemaTypeSystem _schemaTypeSystem;
        public String _handle;

        public abstract int getComponentType();

        public final SchemaTypeSystem getTypeSystem()
            { return _schemaTypeSystem; }

        public final SchemaComponent getComponent()
        {
            if (_schemaComponent == null && _handle != null)
            {
                synchronized (this)
                {
                    if (_schemaComponent == null && _handle != null)
                    {
                        _schemaComponent = _schemaTypeSystem.resolveHandle(_handle);
                        _schemaTypeSystem = null;
                    }
                }
            }

            return _schemaComponent;
        }
    }
    
    /**
     * Used for on-demand loading of schema components.
     */ 
    public Ref getComponentRef();
}
