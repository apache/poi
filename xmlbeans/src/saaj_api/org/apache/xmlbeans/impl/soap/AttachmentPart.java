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

// ericvas
//import javax.activation.DataHandler;
import java.util.Iterator;

/**
 * <P>A single attachment to a <CODE>SOAPMessage</CODE> object. A
 *   <CODE>SOAPMessage</CODE> object may contain zero, one, or many
 *   <CODE>AttachmentPart</CODE> objects. Each <CODE>
 *   AttachmentPart</CODE> object consists of two parts,
 *   application-specific content and associated MIME headers. The
 *   MIME headers consists of name/value pairs that can be used to
 *   identify and describe the content.</P>
 *
 *   <P>An <CODE>AttachmentPart</CODE> object must conform to
 *   certain standards.</P>
 *
 *   <OL>
 *     <LI>It must conform to <A href=
 *     "http://www.ietf.org/rfc/rfc2045.txt">MIME [RFC2045]
 *     standards</A></LI>
 *
 *     <LI>It MUST contain content</LI>
 *
 *     <LI>
 *       The header portion MUST include the following header:
 *
 *       <UL>
 *         <LI>
 *           <CODE>Content-Type</CODE><BR>
 *            This header identifies the type of data in the content
 *           of an <CODE>AttachmentPart</CODE> object and MUST
 *           conform to [RFC2045]. The following is an example of a
 *           Content-Type header:
 * <PRE>
 *      Content-Type:  application/xml
 *
 * </PRE>
 *           The following line of code, in which <CODE>ap</CODE> is
 *           an <CODE>AttachmentPart</CODE> object, sets the header
 *           shown in the previous example.
 * <PRE>
 *      ap.setMimeHeader("Content-Type", "application/xml");
 *
 * </PRE>
 *
 *           <P></P>
 *         </LI>
 *       </UL>
 *     </LI>
 *   </OL>
 *
 *   <P>There are no restrictions on the content portion of an
 *   <CODE>AttachmentPart</CODE> object. The content may be anything
 *   from a simple plain text object to a complex XML document or
 *   image file.</P>
 *
 *   <P>An <CODE>AttachmentPart</CODE> object is created with the
 *   method <CODE>SOAPMessage.createAttachmentPart</CODE>. After
 *   setting its MIME headers, the <CODE>AttachmentPart</CODE>
 *   object is added to the message that created it with the method
 *   <CODE>SOAPMessage.addAttachmentPart</CODE>.</P>
 *
 *   <P>The following code fragment, in which <CODE>m</CODE> is a
 *   <CODE>SOAPMessage</CODE> object and <CODE>contentStringl</CODE>
 *   is a <CODE>String</CODE>, creates an instance of <CODE>
 *   AttachmentPart</CODE>, sets the <CODE>AttachmentPart</CODE>
 *   object with some content and header information, and adds the
 *   <CODE>AttachmentPart</CODE> object to the <CODE>
 *   SOAPMessage</CODE> object.</P>
 * <PRE>
 *    AttachmentPart ap1 = m.createAttachmentPart();
 *    ap1.setContent(contentString1, "text/plain");
 *    m.addAttachmentPart(ap1);
 * </PRE>
 *
 *   <P>The following code fragment creates and adds a second <CODE>
 *   AttachmentPart</CODE> instance to the same message. <CODE>
 *   jpegData</CODE> is a binary byte buffer representing the jpeg
 *   file.</P>
 * <PRE>
 *    AttachmentPart ap2 = m.createAttachmentPart();
 *    byte[] jpegData =  ...;
 *    ap2.setContent(new ByteArrayInputStream(jpegData), "image/jpeg");
 *    m.addAttachmentPart(ap2);
 * </PRE>
 *
 *   <P>The <CODE>getContent</CODE> method retrieves the contents
 *   and header from an <CODE>AttachmentPart</CODE> object.
 *   Depending on the <CODE>DataContentHandler</CODE> objects
 *   present, the returned <CODE>Object</CODE> can either be a typed
 *   Java object corresponding to the MIME type or an <CODE>
 *   InputStream</CODE> object that contains the content as
 *   bytes.</P>
 * <PRE>
 *    String content1 = ap1.getContent();
 *    java.io.InputStream content2 = ap2.getContent();
 * </PRE>
 *   The method <CODE>clearContent</CODE> removes all the content
 *   from an <CODE>AttachmentPart</CODE> object but does not affect
 *   its header information.
 * <PRE>
 *    ap1.clearContent();
 * </PRE>
 */
public abstract class AttachmentPart {

    // fixme: should this constructor be protected?
    /** Create a new AttachmentPart. */
    public AttachmentPart() {}

    /**
     * Returns the number of bytes in this <CODE>
     * AttachmentPart</CODE> object.
     * @return the size of this <CODE>AttachmentPart</CODE> object
     *     in bytes or -1 if the size cannot be determined
     * @throws  SOAPException  if the content of this
     *     attachment is corrupted of if there was an exception
     *     while trying to determine the size.
     */
    public abstract int getSize() throws SOAPException;

    /**
     * Clears out the content of this <CODE>
     * AttachmentPart</CODE> object. The MIME header portion is left
     * untouched.
     */
    public abstract void clearContent();

    /**
     * Gets the content of this <code>AttachmentPart</code> object as a Java
     * object. The type of the returned Java object depends on (1) the
     * <code>DataContentHandler</code> object that is used to interpret the bytes
     * and (2) the <code>Content-Type</code> given in the header.
     * <p>
     * For the MIME content types "text/plain", "text/html" and "text/xml", the
     * <code>DataContentHandler</code> object does the conversions to and
     * from the Java types corresponding to the MIME types.
     * For other MIME types,the <code>DataContentHandler</code> object
     * can return an <code>InputStream</code> object that contains the content data
     * as raw bytes.
     * <p>
     * A JAXM-compliant implementation must, as a minimum, return a
     * <code>java.lang.String</code> object corresponding to any content
     * stream with a <code>Content-Type</code> value of
     * <code>text/plain</code>, a
     * <code>javax.xml.transform.StreamSource</code> object corresponding to a
     * content stream with a <code>Content-Type</code> value of
     * <code>text/xml</code>, a <code>java.awt.Image</code> object
     * corresponding to a content stream with a
     * <code>Content-Type</code> value of <code>image/gif</code> or
     * <code>image/jpeg</code>.  For those content types that an
     * installed <code>DataContentHandler</code> object does not understand, the
     * <code>DataContentHandler</code> object is required to return a
     * <code>java.io.InputStream</code> object with the raw bytes.
     *
     * @return a Java object with the content of this <CODE>
     *     AttachmentPart</CODE> object
     * @throws  SOAPException  if there is no content set
     *     into this <CODE>AttachmentPart</CODE> object or if there
     *     was a data transformation error
     */
    public abstract Object getContent() throws SOAPException;

    /**
     * Sets the content of this attachment part to that of the
     * given <CODE>Object</CODE> and sets the value of the <CODE>
     * Content-Type</CODE> header to the given type. The type of the
     * <CODE>Object</CODE> should correspond to the value given for
     * the <CODE>Content-Type</CODE>. This depends on the particular
     * set of <CODE>DataContentHandler</CODE> objects in use.
     * @param  object  the Java object that makes up
     * the content for this attachment part
     * @param  contentType the MIME string that
     * specifies the type of the content
     * @throws java.lang.IllegalArgumentException if
     *     the contentType does not match the type of the content
     *     object, or if there was no <CODE>
     *     DataContentHandler</CODE> object for this content
     *     object
     * @see #getContent() getContent()
     */
    public abstract void setContent(Object object, String contentType);

    /**
     * Gets the <CODE>DataHandler</CODE> object for this <CODE>
     * AttachmentPart</CODE> object.
     * @return the <CODE>DataHandler</CODE> object associated with
     *     this <CODE>AttachmentPart</CODE> object
     * @throws  SOAPException  if there is
     *     no data in this <CODE>AttachmentPart</CODE> object
     */
// ericvas
//    public abstract DataHandler getDataHandler() throws SOAPException;

    /**
     * Sets the given <CODE>DataHandler</CODE> object as the
     * data handler for this <CODE>AttachmentPart</CODE> object.
     * Typically, on an incoming message, the data handler is
     * automatically set. When a message is being created and
     * populated with content, the <CODE>setDataHandler</CODE>
     * method can be used to get data from various data sources into
     * the message.
     * @param  datahandler  <CODE>DataHandler</CODE> object to
     *     be set
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified <CODE>
     *     DataHandler</CODE> object
     */
// ericvas
//    public abstract void setDataHandler(DataHandler datahandler);

    /**
     * Gets the value of the MIME header whose name is
     * "Content-Id".
     * @return  a <CODE>String</CODE> giving the value of the
     *     "Content-Id" header or <CODE>null</CODE> if there is
     *     none
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
     * Gets the value of the MIME header
     * "Content-Location".
     * @return  a <CODE>String</CODE> giving the value of the
     *     "Content-Location" header or <CODE>null</CODE> if there
     *     is none
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
     * Gets the value of the MIME header "Content-Type".
     * @return  a <CODE>String</CODE> giving the value of the
     *     "Content-Type" header or <CODE>null</CODE> if there is
     *     none
     */
    public String getContentType() {

        String as[] = getMimeHeader("Content-Type");

        if (as != null && as.length > 0) {
            return as[0];
        } else {
            return null;
        }
    }

    /**
     * Sets the MIME header "Content-Id" with the given
     * value.
     * @param  contentId a <CODE>String</CODE> giving
     *     the value of the "Content-Id" header
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified <CODE>
     *     contentId</CODE> value
     * @see #getContentId() getContentId()
     */
    public void setContentId(String contentId) {
        setMimeHeader("Content-Id", contentId);
    }

    /**
     * Sets the MIME header "Content-Location" with the given
     * value.
     * @param  contentLocation a <CODE>String</CODE>
     *     giving the value of the "Content-Location" header
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified content
     *     location
     */
    public void setContentLocation(String contentLocation) {
        setMimeHeader("Content-Location", contentLocation);
    }

    /**
     * Sets the MIME header "Content-Type" with the given
     * value.
     * @param  contentType  a <CODE>String</CODE>
     *     giving the value of the "Content-Type" header
     * @throws java.lang.IllegalArgumentException if
     * there was a problem with the specified content type
     */
    public void setContentType(String contentType) {
        setMimeHeader("Content-Type", contentType);
    }

    /**
     * Removes all MIME headers that match the given name.
     * @param  header - the string name of the MIME
     *     header/s to be removed
     */
    public abstract void removeMimeHeader(String header);

    /** Removes all the MIME header entries. */
    public abstract void removeAllMimeHeaders();

    /**
     * Gets all the values of the header identified by the given
     * <CODE>String</CODE>.
     * @param   name  the name of the header; example:
     *     "Content-Type"
     * @return a <CODE>String</CODE> array giving the value for the
     *     specified header
     * @see #setMimeHeader(java.lang.String, java.lang.String) setMimeHeader(java.lang.String, java.lang.String)
     */
    public abstract String[] getMimeHeader(String name);

    /**
     * Changes the first header entry that matches the given name
     *   to the given value, adding a new header if no existing
     *   header matches. This method also removes all matching
     *   headers but the first.
     *
     *   <P>Note that RFC822 headers can only contain US-ASCII
     *   characters.</P>
     * @param  name   a <CODE>String</CODE> giving the
     *     name of the header for which to search
     * @param  value  a <CODE>String</CODE> giving the
     *     value to be set for the header whose name matches the
     *     given name
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified mime header name
     *     or value
     */
    public abstract void setMimeHeader(String name, String value);

    /**
     * Adds a MIME header with the specified name and value to
     *   this <CODE>AttachmentPart</CODE> object.
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     * @param  name   a <CODE>String</CODE> giving the
     *     name of the header to be added
     * @param  value  a <CODE>String</CODE> giving the
     *     value of the header to be added
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem with the specified mime header name
     *     or value
     */
    public abstract void addMimeHeader(String name, String value);

    /**
     * Retrieves all the headers for this <CODE>
     * AttachmentPart</CODE> object as an iterator over the <CODE>
     * MimeHeader</CODE> objects.
     * @return  an <CODE>Iterator</CODE> object with all of the Mime
     *     headers for this <CODE>AttachmentPart</CODE> object
     */
    public abstract Iterator getAllMimeHeaders();

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects that match
     * a name in the given array.
     * @param   names a <CODE>String</CODE> array with
     *     the name(s) of the MIME headers to be returned
     * @return all of the MIME headers that match one of the names
     *     in the given array as an <CODE>Iterator</CODE>
     *     object
     */
    public abstract Iterator getMatchingMimeHeaders(String names[]);

    /**
     * Retrieves all <CODE>MimeHeader</CODE> objects whose name
     * does not match a name in the given array.
     * @param   names  a <CODE>String</CODE> array with
     *     the name(s) of the MIME headers not to be returned
     * @return all of the MIME headers in this <CODE>
     *     AttachmentPart</CODE> object except those that match one
     *     of the names in the given array. The nonmatching MIME
     *     headers are returned as an <CODE>Iterator</CODE>
     *     object.
     */
    public abstract Iterator getNonMatchingMimeHeaders(String names[]);
}
