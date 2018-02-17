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

package org.apache.xmlbeans.impl.common;

public interface PrefixResolver
{
    /**
     * Caled when the user has a prefix and needs to look up the corresponding
     * namespace URI. If the prefix is not defined in this context, then this
     * method may return null.  The no-namespace is represented by the empty
     * string return result.
     * 
     * If the prefix is null or "", then the default namespace is being
     * requested.  To conform with the XML spec, the default namespace will
     * return the no-namespace ("") if it is not mapped.
     */
    String getNamespaceForPrefix(String prefix);
}
