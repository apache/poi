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
 * Represents a local or global element definition.
 */ 
public interface SchemaLocalElement extends SchemaField, SchemaAnnotated
{
    /**
     * True if extension is blocked.
     */
    boolean blockExtension();
    /**
     * True if restriction is blocked.
     */
    boolean blockRestriction();
    /**
     * True if element substitution is blocked.
     */
    boolean blockSubstitution();

    /**
     * True if this element is prohibited in content. Only
     * sensible if this is the head of a substitution group;
     * then only substitution group members can appear.<P>
     * 
     * Although local elements cannot be abstract, if an element 
     * use is a ref to a global element, then the flag from the 
     * global element is copied in to the local element where the 
     * use occurs.
     */
    boolean isAbstract();

    /**
     * Returns all the Key, KeyRef, and Unique constraints
     * on this element.
     */
    SchemaIdentityConstraint[] getIdentityConstraints();
}
