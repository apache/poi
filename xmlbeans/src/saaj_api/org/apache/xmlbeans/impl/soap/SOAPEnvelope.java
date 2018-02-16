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
 * The container for the SOAPHeader and SOAPBody portions of a
 *   <CODE>SOAPPart</CODE> object. By default, a <CODE>
 *   SOAPMessage</CODE> object is created with a <CODE>
 *   SOAPPart</CODE> object that has a <CODE>SOAPEnvelope</CODE>
 *   object. The <CODE>SOAPEnvelope</CODE> object by default has an
 *   empty <CODE>SOAPBody</CODE> object and an empty <CODE>
 *   SOAPHeader</CODE> object. The <CODE>SOAPBody</CODE> object is
 *   required, and the <CODE>SOAPHeader</CODE> object, though
 *   optional, is used in the majority of cases. If the <CODE>
 *   SOAPHeader</CODE> object is not needed, it can be deleted,
 *   which is shown later.</P>
 *
 *   <P>A client can access the <CODE>SOAPHeader</CODE> and <CODE>
 *   SOAPBody</CODE> objects by calling the methods <CODE>
 *   SOAPEnvelope.getHeader</CODE> and <CODE>
 *   SOAPEnvelope.getBody</CODE>. The following lines of code use
 *   these two methods after starting with the <CODE>
 *   SOAPMessage</CODE> object <I>message</I> to get the <CODE>
 *   SOAPPart</CODE> object <I>sp</I>, which is then used to get the
 *   <CODE>SOAPEnvelope</CODE> object <I>se</I>.</P>
 * <PRE>
 *    SOAPPart sp = message.getSOAPPart();
 *    SOAPEnvelope se = sp.getEnvelope();
 *    SOAPHeader sh = se.getHeader();
 *    SOAPBody sb = se.getBody();
 * </PRE>
 *
 *   <P>It is possible to change the body or header of a <CODE>
 *   SOAPEnvelope</CODE> object by retrieving the current one,
 *   deleting it, and then adding a new body or header. The <CODE>
 *   javax.xml.soap.Node</CODE> method <CODE>detachNode</CODE>
 *   detaches the XML element (node) on which it is called. For
 *   example, the following line of code deletes the <CODE>
 *   SOAPBody</CODE> object that is retrieved by the method <CODE>
 *   getBody</CODE>.</P>
 * <PRE>
 *     se.getBody().detachNode();
 * </PRE>
 *   To create a <CODE>SOAPHeader</CODE> object to replace the one
 *   that was removed, a client uses the method <CODE>
 *   SOAPEnvelope.addHeader</CODE>, which creates a new header and
 *   adds it to the <CODE>SOAPEnvelope</CODE> object. Similarly, the
 *   method <CODE>addBody</CODE> creates a new <CODE>SOAPBody</CODE>
 *   object and adds it to the <CODE>SOAPEnvelope</CODE> object. The
 *   following code fragment retrieves the current header, removes
 *   it, and adds a new one. Then it retrieves the current body,
 *   removes it, and adds a new one.
 * <PRE>
 *    SOAPPart sp = message.getSOAPPart();
 *    SOAPEnvelope se = sp.getEnvelope();
 *    se.getHeader().detachNode();
 *    SOAPHeader sh = se.addHeader();
 *    se.getBody().detachNode();
 *    SOAPBody sb = se.addBody();
 * </PRE>
 *   It is an error to add a <CODE>SOAPBody</CODE> or <CODE>
 *   SOAPHeader</CODE> object if one already exists.
 *
 *   <P>The <CODE>SOAPEnvelope</CODE> interface provides three
 *   methods for creating <CODE>Name</CODE> objects. One method
 *   creates <CODE>Name</CODE> objects with a local name, a
 *   namespace prefix, and a namesapce URI. The second method
 *   creates <CODE>Name</CODE> objects with a local name and a
 *   namespace prefix, and the third creates <CODE>Name</CODE>
 *   objects with just a local name. The following line of code, in
 *   which <I>se</I> is a <CODE>SOAPEnvelope</CODE> object, creates
 *   a new <CODE>Name</CODE> object with all three.</P>
 * <PRE>
 *    Name name = se.createName("GetLastTradePrice", "WOMBAT",
 *                               "http://www.wombat.org/trader");
 * </PRE>
 */
public interface SOAPEnvelope extends SOAPElement {

    /**
     * Creates a new <CODE>Name</CODE> object initialized with the
     *   given local name, namespace prefix, and namespace URI.
     *
     *   <P>This factory method creates <CODE>Name</CODE> objects
     *   for use in the SOAP/XML document.
     * @param   localName a <CODE>String</CODE> giving
     *     the local name
     * @param   prefix a <CODE>String</CODE> giving
     *     the prefix of the namespace
     * @param   uri  a <CODE>String</CODE> giving the
     *     URI of the namespace
     * @return a <CODE>Name</CODE> object initialized with the given
     *     local name, namespace prefix, and namespace URI
     * @throws  SOAPException  if there is a SOAP error
     */
    public abstract Name createName(String localName, String prefix, String uri)
        throws SOAPException;

    /**
     * Creates a new <CODE>Name</CODE> object initialized with the
     *   given local name.
     *
     *   <P>This factory method creates <CODE>Name</CODE> objects
     *   for use in the SOAP/XML document.
     *
     * @param localName a <CODE>String</CODE> giving
     * the local name
     * @return a <CODE>Name</CODE> object initialized with the given
     *     local name
     * @throws  SOAPException  if there is a SOAP error
     */
    public abstract Name createName(String localName) throws SOAPException;

    /**
     * Returns the <CODE>SOAPHeader</CODE> object for this <CODE>
     *   SOAPEnvelope</CODE> object.
     *
     *   <P>A new <CODE>SOAPMessage</CODE> object is by default
     *   created with a <CODE>SOAPEnvelope</CODE> object that
     *   contains an empty <CODE>SOAPHeader</CODE> object. As a
     *   result, the method <CODE>getHeader</CODE> will always
     *   return a <CODE>SOAPHeader</CODE> object unless the header
     *   has been removed and a new one has not been added.
     * @return the <CODE>SOAPHeader</CODE> object or <CODE>
     *     null</CODE> if there is none
     * @throws  SOAPException if there is a problem
     *     obtaining the <CODE>SOAPHeader</CODE> object
     */
    public abstract SOAPHeader getHeader() throws SOAPException;

    /**
     * Returns the <CODE>SOAPBody</CODE> object associated with
     *   this <CODE>SOAPEnvelope</CODE> object.
     *
     *   <P>A new <CODE>SOAPMessage</CODE> object is by default
     *   created with a <CODE>SOAPEnvelope</CODE> object that
     *   contains an empty <CODE>SOAPBody</CODE> object. As a
     *   result, the method <CODE>getBody</CODE> will always return
     *   a <CODE>SOAPBody</CODE> object unless the body has been
     *   removed and a new one has not been added.
     * @return the <CODE>SOAPBody</CODE> object for this <CODE>
     *     SOAPEnvelope</CODE> object or <CODE>null</CODE> if there
     *     is none
     * @throws  SOAPException  if there is a problem
     *     obtaining the <CODE>SOAPBody</CODE> object
     */
    public abstract SOAPBody getBody() throws SOAPException;

    /**
     * Creates a <CODE>SOAPHeader</CODE> object and sets it as the
     *   <CODE>SOAPHeader</CODE> object for this <CODE>
     *   SOAPEnvelope</CODE> object.
     *
     *   <P>It is illegal to add a header when the envelope already
     *   contains a header. Therefore, this method should be called
     *   only after the existing header has been removed.
     * @return the new <CODE>SOAPHeader</CODE> object
     * @throws  SOAPException  if this <CODE>
     *     SOAPEnvelope</CODE> object already contains a valid
     *     <CODE>SOAPHeader</CODE> object
     */
    public abstract SOAPHeader addHeader() throws SOAPException;

    /**
     * Creates a <CODE>SOAPBody</CODE> object and sets it as the
     *   <CODE>SOAPBody</CODE> object for this <CODE>
     *   SOAPEnvelope</CODE> object.
     *
     *   <P>It is illegal to add a body when the envelope already
     *   contains a body. Therefore, this method should be called
     *   only after the existing body has been removed.
     * @return  the new <CODE>SOAPBody</CODE> object
     * @throws  SOAPException  if this <CODE>
     *     SOAPEnvelope</CODE> object already contains a valid
     *     <CODE>SOAPBody</CODE> object
     */
    public abstract SOAPBody addBody() throws SOAPException;
}
