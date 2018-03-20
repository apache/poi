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
 * generic interface to abstract creation of an object
 */
public interface ObjectFactory
{
    /**
     * Given the type, create an object of that type.  Note that the return object
     * might be a subclass of type, but should always be an instanceof type.
     *
     * @param type   type of object to create
     * @return  instance
     */
    Object createObject(Class type);
}
