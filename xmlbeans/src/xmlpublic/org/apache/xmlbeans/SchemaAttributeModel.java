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
 * Represents the attribute structure allowed on a complex type.
 * 
 * @see SchemaType#getAttributeModel
 */ 
public interface SchemaAttributeModel
{
    /**
     * Returns an array containing all the attributes in the model.
     */
    SchemaLocalAttribute[] getAttributes();

    /**
     * Returns the attribute with the given name.
     */
    SchemaLocalAttribute getAttribute(QName name);

    /**
     * QNameSet representing the attribute wildcard specification.
     */
    QNameSet getWildcardSet();

    /**
     * Returns the processing code ({@link #STRICT}, {@link #LAX}, {@link #SKIP}).
     * Returns 0 ({@link #NONE}) if no wildcard specified.
     */
    int getWildcardProcess();

    /** See {@link #getWildcardProcess} */
    static final int NONE = 0;
    /** Strict wildcard processing. See {@link #getWildcardProcess} */
    static final int STRICT = 1;
    /** Lax wildcard processing. See {@link #getWildcardProcess} */
    static final int LAX = 2;
    /** Skip wildcard processing. See {@link #getWildcardProcess} */
    static final int SKIP = 3;
}
