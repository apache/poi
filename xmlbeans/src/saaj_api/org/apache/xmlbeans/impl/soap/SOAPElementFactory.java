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
 * <P><CODE>SOAPElementFactory</CODE> is a factory for XML
 * fragments that will eventually end up in the SOAP part. These
 * fragments can be inserted as children of the <CODE>
 * SOAPHeader</CODE> or <CODE>SOAPBody</CODE> or <CODE>
 * SOAPEnvelope</CODE>.</P>
 *
 * <P>Elements created using this factory do not have the
 * properties of an element that lives inside a SOAP header
 * document. These elements are copied into the XML document tree
 * when they are inserted.</P>
 * @deprecated - Use javax.xml.soap.SOAPFactory for creating SOAPElements.
 * @see SOAPFactory SOAPFactory
 */
public class SOAPElementFactory {

    /**
     * Create a new <code>SOAPElementFactory from a <code>SOAPFactory</code>.
     *
     * @param soapfactory  the <code>SOAPFactory</code> to use
     */
    private SOAPElementFactory(SOAPFactory soapfactory) {
        sf = soapfactory;
    }

    /**
     * Create a <CODE>SOAPElement</CODE> object initialized with
     * the given <CODE>Name</CODE> object.
     * @param   name a <CODE>Name</CODE> object with
     *     the XML name for the new element
     * @return the new <CODE>SOAPElement</CODE> object that was
     *     created
     * @throws  SOAPException if there is an error in
     *     creating the <CODE>SOAPElement</CODE> object
     * @deprecated Use javax.xml.soap.SOAPFactory.createElement(javax.xml.soap.Name) instead
     * @see SOAPFactory#createElement(javax.xml.soap.Name) SOAPFactory.createElement(javax.xml.soap.Name)
     */
    public SOAPElement create(Name name) throws SOAPException {
        return sf.createElement(name);
    }

    /**
     * Create a <CODE>SOAPElement</CODE> object initialized with
     * the given local name.
     * @param   localName a <CODE>String</CODE> giving
     *     the local name for the new element
     * @return the new <CODE>SOAPElement</CODE> object that was
     *     created
     * @throws  SOAPException if there is an error in
     *     creating the <CODE>SOAPElement</CODE> object
     * @deprecated Use javax.xml.soap.SOAPFactory.createElement(String localName) instead
     * @see SOAPFactory#createElement(java.lang.String) SOAPFactory.createElement(java.lang.String)
     */
    public SOAPElement create(String localName) throws SOAPException {
        return sf.createElement(localName);
    }

    /**
     * Create a new <CODE>SOAPElement</CODE> object with the
     * given local name, prefix and uri.
     * @param   localName a <CODE>String</CODE> giving
     *     the local name for the new element
     * @param   prefix the prefix for this <CODE>
     *     SOAPElement</CODE>
     * @param   uri a <CODE>String</CODE> giving the
     *     URI of the namespace to which the new element
     *     belongs
     * @return the new <CODE>SOAPElement</CODE> object that was
     *     created
     * @throws  SOAPException if there is an error in
     *     creating the <CODE>SOAPElement</CODE> object
     * @deprecated Use javax.xml.soap.SOAPFactory.createElement(String localName, String prefix, String uri) instead
     * @see SOAPFactory#createElement(java.lang.String, java.lang.String, java.lang.String) SOAPFactory.createElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public SOAPElement create(String localName, String prefix, String uri)
            throws SOAPException {
        return sf.createElement(localName, prefix, uri);
    }

    /**
     * Creates a new instance of <CODE>SOAPElementFactory</CODE>.
     *
     * @return a new instance of a <CODE>
     *     SOAPElementFactory</CODE>
     * @throws  SOAPException if there was an error creating
     *     the default <CODE>SOAPElementFactory
     * @deprecated
     */
    public static SOAPElementFactory newInstance() throws SOAPException {

        try {
            return new SOAPElementFactory(SOAPFactory.newInstance());
        } catch (Exception exception) {
            throw new SOAPException("Unable to create SOAP Element Factory: "
                                    + exception.getMessage());
        }
    }

    private SOAPFactory sf;
}
