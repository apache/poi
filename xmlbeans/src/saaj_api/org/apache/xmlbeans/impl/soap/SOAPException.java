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
 * An exception that signals that a SOAP exception has
 *   occurred. A <CODE>SOAPException</CODE> object may contain a
 *   <CODE>String</CODE> that gives the reason for the exception, an
 *   embedded <CODE>Throwable</CODE> object, or both. This class
 *   provides methods for retrieving reason messages and for
 *   retrieving the embedded <CODE>Throwable</CODE> object.</P>
 *
 *   <P>Typical reasons for throwing a <CODE>SOAPException</CODE>
 *   object are problems such as difficulty setting a header, not
 *   being able to send a message, and not being able to get a
 *   connection with the provider. Reasons for embedding a <CODE>
 *   Throwable</CODE> object include problems such as input/output
 *   errors or a parsing problem, such as an error in parsing a
 *   header.
 */
public class SOAPException extends Exception {

    /**
     * Constructs a <CODE>SOAPException</CODE> object with no
     * reason or embedded <CODE>Throwable</CODE> object.
     */
    public SOAPException() {
        cause = null;
    }

    /**
     * Constructs a <CODE>SOAPException</CODE> object with the
     * given <CODE>String</CODE> as the reason for the exception
     * being thrown.
     * @param  reason  a description of what caused
     *     the exception
     */
    public SOAPException(String reason) {

        super(reason);

        cause = null;
    }

    /**
     * Constructs a <CODE>SOAPException</CODE> object with the
     * given <CODE>String</CODE> as the reason for the exception
     * being thrown and the given <CODE>Throwable</CODE> object as
     * an embedded exception.
     * @param  reason a description of what caused
     *     the exception
     * @param  cause  a <CODE>Throwable</CODE> object
     *     that is to be embedded in this <CODE>SOAPException</CODE>
     *     object
     */
    public SOAPException(String reason, Throwable cause) {

        super(reason);

        initCause(cause);
    }

    /**
     * Constructs a <CODE>SOAPException</CODE> object
     * initialized with the given <CODE>Throwable</CODE>
     * object.
     * @param  cause  a <CODE>Throwable</CODE> object
     *     that is to be embedded in this <CODE>SOAPException</CODE>
     *     object
     */
    public SOAPException(Throwable cause) {

        super(cause.toString());

        initCause(cause);
    }

    /**
     * Returns the detail message for this <CODE>
     *   SOAPException</CODE> object.
     *
     *   <P>If there is an embedded <CODE>Throwable</CODE> object,
     *   and if the <CODE>SOAPException</CODE> object has no detail
     *   message of its own, this method will return the detail
     *   message from the embedded <CODE>Throwable</CODE>
     *   object.</P>
     * @return  the error or warning message for this <CODE>
     *     SOAPException</CODE> or, if it has none, the message of
     *     the embedded <CODE>Throwable</CODE> object, if there is
     *     one
     */
    public String getMessage() {

        String s = super.getMessage();

        if ((s == null) && (cause != null)) {
            return cause.getMessage();
        } else {
            return s;
        }
    }

    /**
     * Returns the <CODE>Throwable</CODE> object embedded in
     * this <CODE>SOAPException</CODE> if there is one. Otherwise,
     * this method returns <CODE>null</CODE>.
     * @return  the embedded <CODE>Throwable</CODE> object or <CODE>
     *     null</CODE> if there is none
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Initializes the <CODE>cause</CODE> field of this <CODE>
     *   SOAPException</CODE> object with the given <CODE>
     *   Throwable</CODE> object.
     *
     *   <P>This method can be called at most once. It is generally
     *   called from within the constructor or immediately after the
     *   constructor has returned a new <CODE>SOAPException</CODE>
     *   object. If this <CODE>SOAPException</CODE> object was
     *   created with the constructor {@link #SOAPException(java.lang.Throwable) SOAPException(java.lang.Throwable)}
     *   or {@link #SOAPException(java.lang.String, java.lang.Throwable) SOAPException(java.lang.String, java.lang.Throwable)}, meaning
     *   that its <CODE>cause</CODE> field already has a value, this
     *   method cannot be called even once.
     *
     * @param cause  the <CODE>Throwable</CODE>
     *     object that caused this <CODE>SOAPException</CODE> object
     *     to be thrown. The value of this parameter is saved for
     *     later retrieval by the <A href=
     *     "../../../javax/xml/soap/SOAPException.html#getCause()">
     *     <CODE>getCause()</CODE></A> method. A <TT>null</TT> value
     *     is permitted and indicates that the cause is nonexistent
     *     or unknown.
     * @return a reference to this <CODE>SOAPException</CODE>
     *     instance
     * @throws java.lang.IllegalArgumentException if
     *     <CODE>cause</CODE> is this <CODE>Throwable</CODE> object.
     *     (A <CODE>Throwable</CODE> object cannot be its own
     *     cause.)
     * @throws java.lang.IllegalStateException if this <CODE>
     *     SOAPException</CODE> object was created with {@link #SOAPException(java.lang.Throwable) SOAPException(java.lang.Throwable)}
     *   or {@link #SOAPException(java.lang.String, java.lang.Throwable) SOAPException(java.lang.String, java.lang.Throwable)}, or this
     *     method has already been called on this <CODE>
     *     SOAPException</CODE> object
     */
    public synchronized Throwable initCause(Throwable cause) {

        if (this.cause != null) {
            throw new IllegalStateException("Can't override cause");
        }

        if (cause == this) {
            throw new IllegalArgumentException("Self-causation not permitted");
        } else {
            this.cause = cause;

            return this;
        }
    }

    private Throwable cause;
}
