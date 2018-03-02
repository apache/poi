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

public interface InterfaceExtension
{
    /**
     * Get the fully qualified interface name.
     */
    String getInterface();

    /**
     * Get the fully qualified static handler class name.
     */
    String getStaticHandler();

    /**
     * Returns an array of MethodSignature declared in the interface class.
     * Possibly null if there is an error in the configuration.
     */
    MethodSignature[] getMethods();

    public interface MethodSignature
    {
        /** Returns the name of the method. */
        String getName();

        /** Returns the fully qualified type name of the return value or 'void' for no return value. */
        String getReturnType();

        /** Returns the fully qualified type name of the parameter types in order. */
        String[] getParameterTypes();

        /** Returns the fully qualified type name of the exception types. */
        String[] getExceptionTypes();
    }
}
