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
 * This class is used to programatically validate the contents of an
 * XML element.Call to both {@link #next} and {@link #peek}
 * will return true if the element
 * with the provided name is allowed at the current position in the element
 * content, the difference being that {@link #next} will advance
 * the current position, while {@link #peek} won't.
 *
 * @see SchemaType#getElementSequencer
 */
public interface SchemaTypeElementSequencer
{
    /**
     * Returns true if the element with the given name is valid at the
     * current position. Advances the current position.
     */
    boolean next(QName elementName);

    /**
     * Return true if the element with the given name is valid at the
     * current position. Does not advance the current position.
     */
    boolean peek(QName elementName);
}
