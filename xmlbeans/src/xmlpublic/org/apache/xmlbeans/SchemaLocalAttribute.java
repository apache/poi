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
 * Represents a local or global attribute definition.
 */ 
public interface SchemaLocalAttribute extends SchemaField, SchemaAnnotated
{
    /**
     * Returns {@link #PROHIBITED}, {@link #OPTIONAL}, or {@link #REQUIRED}.
     * (Actually, never returns PROHIBITED because the schema specificaion
     * states that a prohibited attribute is equivalent to no attribute
     * at all, so a prohibited attribute will never be present in the compiled
     * model.)
     */
    int getUse();

    /** A prohibited attribute.  See {@link #getUse}. */
    static final int PROHIBITED = 1;
    /** An optional attribute.  See {@link #getUse}. */
    static final int OPTIONAL = 2;
    /** A required attribute.  See {@link #getUse}. */
    static final int REQUIRED = 3;
}
