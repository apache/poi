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

import java.io.IOException;
import java.io.InputStream;

/**
 * <P>A factory for creating <CODE>SOAPMessage</CODE> objects.</P>
 *
 *   <P>A JAXM client performs the following steps to create a
 *   message.</P>
 *
 *   <UL>
 *     <LI>
 *       Creates a <CODE>MessageFactory</CODE> object from a <CODE>
 *       ProviderConnection</CODE> object (<CODE>con</CODE> in the
 *       following line of code). The <CODE>String</CODE> passed to
 *       the <CODE>createMessageFactory</CODE> method is the name of
 *       of a messaging profile, which must be the URL for the
 *       schema.
 * <PRE>
 *      MessageFactory mf = con.createMessageFactory(schemaURL);
 * </PRE>
 *     </LI>
 *
 *     <LI>
 *       Calls the method <CODE>createMessage</CODE> on the <CODE>
 *       MessageFactory</CODE> object. All messages produced by this
 *       <CODE>MessageFactory</CODE> object will have the header
 *       information appropriate for the messaging profile that was
 *       specified when the <CODE>MessageFactory</CODE> object was
 *       created.
 * <PRE>
 *      SOAPMessage m = mf.createMessage();
 * </PRE>
 *     </LI>
 *   </UL>
 *   It is also possible to create a <CODE>MessageFactory</CODE>
 *   object using the method <CODE>newInstance</CODE>, as shown in
 *   the following line of code.
 * <PRE>
 *      MessageFactory mf = MessageFactory.newInstance();
 * </PRE>
 *   A standalone client (a client that is not running in a
 *   container) can use the <CODE>newInstance</CODE> method to
 *   create a <CODE>MessageFactory</CODE> object.
 *
 *   <P>All <CODE>MessageFactory</CODE> objects, regardless of how
 *   they are created, will produce <CODE>SOAPMessage</CODE> objects
 *   that have the following elements by default:</P>
 *
 *   <UL>
 *     <LI>A <CODE>SOAPPart</CODE> object</LI>
 *
 *     <LI>A <CODE>SOAPEnvelope</CODE> object</LI>
 *
 *     <LI>A <CODE>SOAPBody</CODE> object</LI>
 *
 *     <LI>A <CODE>SOAPHeader</CODE> object</LI>
 *   </UL>
 *   If a <CODE>MessageFactory</CODE> object was created using a
 *   <CODE>ProviderConnection</CODE> object, which means that it was
 *   initialized with a specified profile, it will produce messages
 *   that also come prepopulated with additional entries in the
 *   <CODE>SOAPHeader</CODE> object and the <CODE>SOAPBody</CODE>
 *   object. The content of a new <CODE>SOAPMessage</CODE> object
 *   depends on which of the two <CODE>MessageFactory</CODE> methods
 *   is used to create it.
 *
 *   <UL>
 *     <LI><CODE>createMessage()</CODE> -- message has no
 *     content<BR>
 *      This is the method clients would normally use to create a
 *     request message.</LI>
 *
 *     <LI><CODE>createMessage(MimeHeaders,
 *     java.io.InputStream)</CODE> -- message has content from the
 *     <CODE>InputStream</CODE> object and headers from the <CODE>
 *     MimeHeaders</CODE> object<BR>
 *      This method can be used internally by a service
 *     implementation to create a message that is a response to a
 *     request.</LI>
 *   </UL>
 */
public abstract class MessageFactory {

    // fixme: this should be protected as the class is abstract.
    /** Create a new MessageFactory. */
    public MessageFactory() {}

    /**
     * Creates a new <CODE>MessageFactory</CODE> object that is
     * an instance of the default implementation.
     * @return a new <CODE>MessageFactory</CODE> object
     * @throws  SOAPException  if there was an error in
     *     creating the default implementation of the <CODE>
     *     MessageFactory</CODE>
     */
    public static MessageFactory newInstance() throws SOAPException {

        try {
            return (MessageFactory) FactoryFinder.find(MESSAGE_FACTORY_PROPERTY,
                                                       DEFAULT_MESSAGE_FACTORY);
        } catch (Exception exception) {
            throw new SOAPException(
                "Unable to create message factory for SOAP: "
                + exception.getMessage());
        }
    }

    /**
     * Creates a new <CODE>SOAPMessage</CODE> object with the
     *   default <CODE>SOAPPart</CODE>, <CODE>SOAPEnvelope</CODE>,
     *   <CODE>SOAPBody</CODE>, and <CODE>SOAPHeader</CODE> objects.
     *   Profile-specific message factories can choose to
     *   prepopulate the <CODE>SOAPMessage</CODE> object with
     *   profile-specific headers.
     *
     *   <P>Content can be added to this message's <CODE>
     *   SOAPPart</CODE> object, and the message can be sent "as is"
     *   when a message containing only a SOAP part is sufficient.
     *   Otherwise, the <CODE>SOAPMessage</CODE> object needs to
     *   create one or more <CODE>AttachmentPart</CODE> objects and
     *   add them to itself. Any content that is not in XML format
     *   must be in an <CODE>AttachmentPart</CODE> object.</P>
     * @return  a new <CODE>SOAPMessage</CODE> object
     * @throws  SOAPException if a SOAP error occurs
     */
    public abstract SOAPMessage createMessage() throws SOAPException;

    /**
     * Internalizes the contents of the given <CODE>
     * InputStream</CODE> object into a new <CODE>SOAPMessage</CODE>
     * object and returns the <CODE>SOAPMessage</CODE> object.
     * @param   mimeheaders    the transport-specific headers
     *     passed to the message in a transport-independent fashion
     *     for creation of the message
     * @param   inputstream    the <CODE>InputStream</CODE> object
     *     that contains the data for a message
     * @return a new <CODE>SOAPMessage</CODE> object containing the
     *     data from the given <CODE>InputStream</CODE> object
     * @throws  IOException    if there is a
     *     problem in reading data from the input stream
     * @throws  SOAPException  if the message is invalid
     */
    public abstract SOAPMessage createMessage(
        MimeHeaders mimeheaders, InputStream inputstream)
            throws IOException, SOAPException;

    private static final String DEFAULT_MESSAGE_FACTORY =
        "org.apache.axis.soap.MessageFactoryImpl";

    private static final String MESSAGE_FACTORY_PROPERTY =
        "javax.xml.soap.MessageFactory";
}
