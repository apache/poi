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

/**
 * A factory for creating <code>SOAPConnection</code> objects. Implementation of
 * this class is optional. If <code>SOAPConnectionFactory.newInstance()</code>
 * throws an <code>UnsupportedOperationException</code> then the implementation
 * does not support the SAAJ communication infrastructure. Otherwise
 * <code>SOAPConnection</code> objects can be created by calling
 * <code>createConnection()</code> on the newly created
 * <code>SOAPConnectionFactory</code> object.
 */
public abstract class SOAPConnectionFactory {

    public SOAPConnectionFactory() {}

    /**
     * Creates an instance of the default <CODE>
     * SOAPConnectionFactory</CODE> object.
     * @return a new instance of a default <CODE>
     *     SOAPConnectionFactory</CODE> object
     * @throws  SOAPException  if there was an error creating
     *     the <CODE>SOAPConnectionFactory
     * @throws UnsupportedOperationException  if newInstance is not supported.
     */
    public static SOAPConnectionFactory newInstance()
            throws SOAPException, UnsupportedOperationException {

        try {
            return (SOAPConnectionFactory) FactoryFinder.find(SF_PROPERTY,
                    DEFAULT_SOAP_CONNECTION_FACTORY);
        } catch (Exception exception) {
            throw new SOAPException("Unable to create SOAP connection factory: "
                                    + exception.getMessage());
        }
    }

    /**
     * Create a new <CODE>SOAPConnection</CODE>.
     * @return the new <CODE>SOAPConnection</CODE> object.
     * @throws  SOAPException if there was an exception
     *     creating the <CODE>SOAPConnection</CODE> object.
     */
    public abstract SOAPConnection createConnection() throws SOAPException;

    private static final String DEFAULT_SOAP_CONNECTION_FACTORY =
        "org.apache.axis.soap.SOAPConnectionFactoryImpl";

    private static final String SF_PROPERTY =
        "javax.xml.soap.SOAPConnectionFactory";
}
