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

import javax.xml.transform.Source;
import java.util.Iterator;

/**
 * <P>The container for the SOAP-specific portion of a <CODE>
 * SOAPMessage</CODE> object. All messages are required to have a
 * SOAP part, so when a <CODE>SOAPMessage</CODE> object is
 * created, it will automatically have a <CODE>SOAPPart</CODE>
 * object.</P>
 *
 * <P>A <CODE>SOAPPart</CODE> object is a MIME part and has the
 * MIME headers Content-Id, Content-Location, and Content-Type.
 * Because the value of Content-Type must be "text/xml", a <CODE>
 * SOAPPart</CODE> object automatically has a MIME header of
 * Content-Type with its value set to "text/xml". The value must
 * be "text/xml" because content in the SOAP part of a message
 * must be in XML format. Content that is not of type "text/xml"
 * must be in an <CODE>AttachmentPart</CODE> object rather than in
 * the <CODE>SOAPPart</CODE> object.</P>
 *
 * <P>When a message is sent, its SOAP part must have the MIME
 * header Content-Type set to "text/xml". Or, from the other
 * perspective, the SOAP part of any message that is received must
 * have the MIME header Content-Type with a value of
 * "text/xml".</P>
 *
 * <P>A client can access the <CODE>SOAPPart</CODE> object of a
 * <CODE>SOAPMessage</CODE> object by calling the method <CODE>
 * SOAPMessage.getSOAPPart</CODE>. The following line of code, in
 * which <CODE>message</CODE> is a <CODE>SOAPMessage</CODE>
 * object, retrieves the SOAP part of a message.</P>
 * <PRE>
 * SOAPPart soapPart = message.getSOAPPart();
 * </PRE>
 *
 * <P>A <CODE>SOAPPart</CODE> object contains a <CODE>
 * SOAPEnvelope</CODE> object, which in turn contains a <CODE>
 * SOAPBody</CODE> object and a <CODE>SOAPHeader</CODE> object.
 * The <CODE>SOAPPart</CODE> method <CODE>getEnvelope</CODE> can
 * be used to retrieve the <CODE>SOAPEnvelope</CODE> object.</P>
 */
public abstract class SOAPPart implements org.w3c.dom.Document {

    public SOAPPart() {}

    /**
     * Gets the <CODE>SOAPEnvelope</CODE> object associated with
     * this <CODE>SOAPPart</CODE> object. Once the SOAP envelope is
     * obtained, it can be used to get its contents.
     * @return the <CODE>SOAPEnvelope</CODE> object for this <CODE>
     *     SOAPPart</CODE> object
     * @throws  SOAPException if there is a SOAP error
     */
    public abstract SOAPEnvelope getEnvelope() throws SOAPException;

    /**
     * Retrieves the value of the MIME header whose name is
     * "Content-Id".
     * @return  a <CODE>String</CODE> giving the value of the MIME
     *     header named "Content-Id"
     * @see #setContentId(java.lang.String) setContentId(java.lang.String)
     */
    public String getContentId() {

        String as[] = getMimeHeader("Content-Id");

        if (as != null && as.length > 0) {
            return as[0];
        } else {
            return null;
        }
    }

    /**
     * Retrieves the value of the MIME header whose name is
     * "Content-Location".
     * @return a <CODE>String</CODE> giving the value of the MIME
     *     header whose name is "Content-Location"
     * @see #setContentLocation(java.lang.String) setContentLocation(java.lang.String)
     */
    public String getContentLocation() {

        String as[] = getMimeHeader("Content-Location");

        if (as != null && as.length > 0) {
            return as[0];
        } else {
            return null;
        }
    }

    /**
     * Sets the value of the MIME header named "Content-Id" to
     * the given <CODE>String</CODE>.
     * @param  contentId  a <CODE>String</CODE> giving
     *     the value of the MIME header "Content-Id"
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the content id
     * @see #getContentId() getContentId()
     */
    public void setContentId(String contentId) {
        setMimeHeader("Content-Id", contentId);
    }

    /**
     * Sets the value of the MIME header "Content-Location" to
     * the given <CODE>String</CODE>.
     * @param  contentLocation a <CODE>String</CODE>
     *     giving the value of the MIME header
     *     "Content-Location"
     * @throws java.lang.IllegalArgumentException if
     *     there is a problem in setting the content location.
     * @see #getContentLocation() getContentLocation()
     */
    public void setContentLocation(String contentLocation) {
        setMimeHeader("Content-Location", contentLocation);
    }

    /**
     * Removes all MIME headers that match the given name.
     * @param  header  a <CODE>String</CODE> giving
     *     the name of the MIME header(s) to be removed
     */
    public abstract void removeMimeHeader(String header);

    /**
     * Removes all the <CODE>MimeHeader</CODE> objects for this
     * <CODE>SOAPEnvelope</CODE> object.
     */
    public abstract void removeAllMimeHeaders();

    /**
     * Gets all the values of the <CODE>MimeHeader</CODE> object
     * in this <CODE>SOAPPart</CODE> object that is identified by
     * the given <CODE>String</CODE>.
     * @param   name  the name of the header; example:
     *     "Content-Type"
     * @return a <CODE>String</CODE> array giving all the values for
     *     the specified header
     * @see #setMimeHeader(java.lang.String, java.lang.String) setMimeHeader(java.lang.String, java.lang.String)
     */
    public abstract String[] getMimeHeader(String name);

    /**
     * Changes the first header entry that matches the given
     *   header name so that its value is the given value, adding a
     *   new header with the given name and value if no existing
     *   header is a match. If there is a match, this method clears
     *   all existing values for the first header that matches and
     *   sets the given value instead. If more than one header has
     *   the given name, this method removes all of the matching
     *   headers after the first one.
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     * @param  name a <CODE>String</CODE> giving the
     *     header name for which to search
     * @param  value a <CODE>String</CODE> giving the
     *     value to be set. This value will be substituted for the
     *     current value(s) of the first header that is a match if
     *     there is one. If there is no match, this value will be
     *     the value for a new <CODE>MimeHeader</CODE> object.
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified mime header name
     *     or value
     * @throws java.lang.IllegalArgumentException if there was a problem with the specified mime header name or value
     * @see #getMimeHeader(java.lang.String) getMimeHeader(java.lang.String)
     */
    public abstract void setMimeHeader(String name, String value);

    /**
     *  Creates a <CODE>MimeHeader</CODE> object with the specified
     *   name and value and adds it to this <CODE>SOAPPart</CODE>
     *   object. If a <CODE>MimeHeader</CODE> with the specified
     *   name already exists, this method adds the specified value
     *   to the already existing value(s).
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     *
     * @param  name a <CODE>String</CODE> giving the
     *     header name
     * @param  value a <CODE>String</CODE> giving the
     *     value to be set or added
     * @throws java.lang.IllegalArgumentException if
     * there was a problem with the specified mime header name
     *     or value
     */
    public abstract void addMimeHeader(String name, String value);

    /**
     * Retrieves all the headers for this <CODE>SOAPPart</CODE>
     * object as an iterator over the <CODE>MimeHeader</CODE>
     * objects.
     * @return an <CODE>Iterator</CODE> object with all of the Mime
     *     headers for this <CODE>SOAPPart</CODE> object
     */
    public abstract Iterator getAllMimeHeaders();

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects that match
     * a name in the given array.
     * @param   names a <CODE>String</CODE> array with
     *     the name(s) of the MIME headers to be returned
     * @return all of the MIME headers that match one of the names
     *     in the given array, returned as an <CODE>Iterator</CODE>
     *     object
     */
    public abstract Iterator getMatchingMimeHeaders(String names[]);

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects whose name
     * does not match a name in the given array.
     * @param   names a <CODE>String</CODE> array with
     *     the name(s) of the MIME headers not to be returned
     * @return all of the MIME headers in this <CODE>SOAPPart</CODE>
     *     object except those that match one of the names in the
     *     given array. The nonmatching MIME headers are returned as
     *     an <CODE>Iterator</CODE> object.
     */
    public abstract Iterator getNonMatchingMimeHeaders(String names[]);

    /**
     * Sets the content of the <CODE>SOAPEnvelope</CODE> object
     * with the data from the given <CODE>Source</CODE> object.
     * @param   source javax.xml.transform.Source</CODE> object with the data to
     *     be set
     * @throws  SOAPException if there is a problem in
     *     setting the source
     * @see #getContent() getContent()
     */
    public abstract void setContent(Source source) throws SOAPException;

    /**
     * Returns the content of the SOAPEnvelope as a JAXP <CODE>
     * Source</CODE> object.
     * @return the content as a <CODE>
     *     javax.xml.transform.Source</CODE> object
     * @throws  SOAPException  if the implementation cannot
     *     convert the specified <CODE>Source</CODE> object
     * @see #setContent(javax.xml.transform.Source) setContent(javax.xml.transform.Source)
     */
    public abstract Source getContent() throws SOAPException;
}
