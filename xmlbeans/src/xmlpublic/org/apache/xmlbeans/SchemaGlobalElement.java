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
 * Represents a global element definition.
 * 
 * @see SchemaTypeLoader#findElement
 */
public interface SchemaGlobalElement extends SchemaLocalElement, SchemaComponent
{
    /**
     * Set of QNames for elements that are the members of the
     * substitution group for which this element is the head,
     * not including this element.
     */
    QName[] substitutionGroupMembers();

    /**
     * The element that is the head of this element's substitution
     * group, or <code>null</code> if this element is not a member
     * of a substitution group.
     */
    SchemaGlobalElement substitutionGroup();

    /**
     * True if using this element as the head of a substitution
     * group for a substitution via type extension is prohibited.
     * If both finalExtension and finalRestriction are true, this
     * element cannot be head of a substitution group.
     * Sensible only for global elements.
     */
    public boolean finalExtension();

    /**
     * True if using this element as the head of a substitution
     * group for a substitution via type restriction is prohibited.
     * If both finalExtension and finalRestriction are true, this
     * element cannot be head of a substitution group.
     * Sensible only for global elements.
     */
    public boolean finalRestriction();

    /**
     * Used to allow on-demand loading of elements.
     * 
     * @exclude
     */
    public final static class Ref extends SchemaComponent.Ref
    {
        public Ref(SchemaGlobalElement element)
            { super(element); }

        public Ref(SchemaTypeSystem system, String handle)
            { super(system, handle); }

        public final int getComponentType()
            { return SchemaComponent.ELEMENT; }

        public final SchemaGlobalElement get()
            { return (SchemaGlobalElement)getComponent(); }
    }

    /**
     * Retruns a SchemaGlobalElement.Ref pointing to this element itself.
     */
    public Ref getRef();


}
