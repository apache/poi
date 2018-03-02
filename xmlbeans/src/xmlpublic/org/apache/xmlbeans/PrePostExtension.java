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

public interface PrePostExtension
{
    /** Operation code for setting a property. */
    int OPERATION_SET = 1;

    /** Operation code for inserting a property. */
    int OPERATION_INSERT = 2;

    /** Operation code for removing a property. */
    int OPERATION_REMOVE = 3;

    /**
     * Get the fully qualified static handler class name.
     */
    String getStaticHandler();

    /**
     * Returns true if the static handler class has a preSet() method
     * with the following signature:
     * <br>
     * <code>public static boolean preSet(int, org.apache.xmlbeans.XmlObject, javax.xml.namespace.QName, boolean, int};</code>
     */
    boolean hasPreCall();

    /**
     * Returns true if the static handler class has a preSet() method
     * with the following signature:
     * <br>
     * <code>public static void postSet(int, org.apache.xmlbeans.XmlObject, javax.xml.namespace.QName, boolean, int};</code>
     */
    boolean hasPostCall();

}
