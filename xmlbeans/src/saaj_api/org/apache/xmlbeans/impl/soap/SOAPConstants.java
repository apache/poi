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

package org.apache.xmlbeans.impl.soap;

/** The definition of constants pertaining to the SOAP 1.1 protocol. */
public interface SOAPConstants {

    /** The namespace identifier for the SOAP envelope. */
    public static final String URI_NS_SOAP_ENVELOPE =
        "http://schemas.xmlsoap.org/soap/envelope/";

    /**
     * The namespace identifier for the SOAP encoding (see section 5 of
     * the SOAP 1.1 specification).
     */
    public static final String URI_NS_SOAP_ENCODING =
        "http://schemas.xmlsoap.org/soap/encoding/";

    /**
     * The URI identifying the first application processing a SOAP request as the intended
     * actor for a SOAP header entry (see section 4.2.2 of the SOAP 1.1 specification).
     */
    public static final String URI_SOAP_ACTOR_NEXT =
        "http://schemas.xmlsoap.org/soap/actor/next";
}
