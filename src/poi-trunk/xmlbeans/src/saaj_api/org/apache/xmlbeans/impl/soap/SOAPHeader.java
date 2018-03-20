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

import java.util.Iterator;

/**
 * <P>A representation of the SOAP header element. A SOAP header
 *   element consists of XML data that affects the way the
 *   application-specific content is processed by the message
 *   provider. For example, transaction semantics, authentication
 *   information, and so on, can be specified as the content of a
 *   <CODE>SOAPHeader</CODE> object.</P>
 *
 *   <P>A <CODE>SOAPEnvelope</CODE> object contains an empty <CODE>
 *   SOAPHeader</CODE> object by default. If the <CODE>
 *   SOAPHeader</CODE> object, which is optional, is not needed, it
 *   can be retrieved and deleted with the following line of code.
 *   The variable <I>se</I> is a <CODE>SOAPEnvelope</CODE>
 *   object.</P>
 * <PRE>
 *     se.getHeader().detachNode();
 * </PRE>
 *   A <CODE>SOAPHeader</CODE> object is created with the <CODE>
 *   SOAPEnvelope</CODE> method <CODE>addHeader</CODE>. This method,
 *   which creates a new header and adds it to the envelope, may be
 *   called only after the existing header has been removed.
 * <PRE>
 *     se.getHeader().detachNode();
 *     SOAPHeader sh = se.addHeader();
 * </PRE>
 *
 *   <P>A <CODE>SOAPHeader</CODE> object can have only <CODE>
 *   SOAPHeaderElement</CODE> objects as its immediate children. The
 *   method <CODE>addHeaderElement</CODE> creates a new <CODE>
 *   HeaderElement</CODE> object and adds it to the <CODE>
 *   SOAPHeader</CODE> object. In the following line of code, the
 *   argument to the method <CODE>addHeaderElement</CODE> is a
 *   <CODE>Name</CODE> object that is the name for the new <CODE>
 *   HeaderElement</CODE> object.</P>
 * <PRE>
 *     SOAPHeaderElement shElement = sh.addHeaderElement(name);
 * </PRE>
 * @see SOAPHeaderElement SOAPHeaderElement
 */
public interface SOAPHeader extends SOAPElement {

    /**
     * Creates a new <CODE>SOAPHeaderElement</CODE> object
     * initialized with the specified name and adds it to this
     * <CODE>SOAPHeader</CODE> object.
     * @param   name a <CODE>Name</CODE> object with
     *     the name of the new <CODE>SOAPHeaderElement</CODE>
     *     object
     * @return the new <CODE>SOAPHeaderElement</CODE> object that
     *     was inserted into this <CODE>SOAPHeader</CODE>
     *     object
     * @throws  SOAPException if a SOAP error occurs
     */
    public abstract SOAPHeaderElement addHeaderElement(Name name)
        throws SOAPException;

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     * objects in this <CODE>SOAPHeader</CODE> object that have the
     * the specified actor. An actor is a global attribute that
     * indicates the intermediate parties to whom the message should
     * be sent. An actor receives the message and then sends it to
     * the next actor. The default actor is the ultimate intended
     * recipient for the message, so if no actor attribute is
     * included in a <CODE>SOAPHeader</CODE> object, the message is
     * sent to its ultimate destination.
     * @param   actor  a <CODE>String</CODE> giving the
     *     URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *     SOAPHeaderElement</CODE> objects that contain the
     *     specified actor
     * @see #extractHeaderElements(java.lang.String) extractHeaderElements(java.lang.String)
     */
    public abstract Iterator examineHeaderElements(String actor);

    /**
     * Returns a list of all the <CODE>SOAPHeaderElement</CODE>
     *   objects in this <CODE>SOAPHeader</CODE> object that have
     *   the the specified actor and detaches them from this <CODE>
     *   SOAPHeader</CODE> object.
     *
     *   <P>This method allows an actor to process only the parts of
     *   the <CODE>SOAPHeader</CODE> object that apply to it and to
     *   remove them before passing the message on to the next
     *   actor.
     * @param   actor  a <CODE>String</CODE> giving the
     *     URI of the actor for which to search
     * @return an <CODE>Iterator</CODE> object over all the <CODE>
     *     SOAPHeaderElement</CODE> objects that contain the
     *     specified actor
     * @see #examineHeaderElements(java.lang.String) examineHeaderElements(java.lang.String)
     */
    public abstract Iterator extractHeaderElements(String actor);

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object that have the specified actor and that have a MustUnderstand
     * attribute whose value is equivalent to <code>true</code>.
     *
     * @param actor a <code>String</code> giving the URI of the actor for which
     *              to search
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderElement</code> objects that contain the
     *              specified actor and are marked as MustUnderstand
     */
    public abstract Iterator examineMustUnderstandHeaderElements(String actor);

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader</code>
     * object.
     *
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderElement</code> objects contained by this
     *              <code>SOAPHeader</code>
     */
    public abstract Iterator examineAllHeaderElements();

    /**
     * Returns an <code>Iterator</code> over all the
     * <code>SOAPHeaderElement</code> objects in this <code>SOAPHeader </code>
     * object and detaches them from this <code>SOAPHeader</code> object.
     *
     * @return an <code>Iterator</code> object over all the
     *              <code>SOAPHeaderElement</code> objects contained by this
     *              <code>SOAPHeader</code>
     */
    public abstract Iterator extractAllHeaderElements();
}
