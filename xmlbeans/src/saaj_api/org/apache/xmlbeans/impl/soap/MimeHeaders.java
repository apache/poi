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
import java.util.Vector;

/**
 * A container for <CODE>MimeHeader</CODE> objects, which
 *   represent the MIME headers present in a MIME part of a
 *   message.</P>
 *
 *   <P>This class is used primarily when an application wants to
 *   retrieve specific attachments based on certain MIME headers and
 *   values. This class will most likely be used by implementations
 *   of <CODE>AttachmentPart</CODE> and other MIME dependent parts
 *   of the JAXM API.
 * @see SOAPMessage#getAttachments() SOAPMessage.getAttachments()
 * @see AttachmentPart AttachmentPart
 */
public class MimeHeaders {

    class MatchingIterator implements Iterator {

        private Object nextMatch() {

            label0:
            while (iterator.hasNext()) {
                MimeHeader mimeheader = (MimeHeader) iterator.next();

                if (names == null) {
                    return match
                           ? null
                           : mimeheader;
                }

                for (int i = 0; i < names.length; i++) {
                    if (!mimeheader.getName().equalsIgnoreCase(names[i])) {
                        continue;
                    }

                    if (match) {
                        return mimeheader;
                    }

                    continue label0;
                }

                if (!match) {
                    return mimeheader;
                }
            }

            return null;
        }

        public boolean hasNext() {

            if (nextHeader == null) {
                nextHeader = nextMatch();
            }

            return nextHeader != null;
        }

        public Object next() {

            if (nextHeader != null) {
                Object obj = nextHeader;

                nextHeader = null;

                return obj;
            }

            if (hasNext()) {
                return nextHeader;
            } else {
                return null;
            }
        }

        public void remove() {
            iterator.remove();
        }

        private boolean match;

        private Iterator iterator;

        private String names[];

        private Object nextHeader;

        MatchingIterator(String as[], boolean flag) {

            match    = flag;
            names    = as;
            iterator = headers.iterator();
        }
    }

    /**
     * Constructs
     *   a default <CODE>MimeHeaders</CODE> object initialized with
     *   an empty <CODE>Vector</CODE> object.
     */
    public MimeHeaders() {
        headers = new Vector();
    }

    /**
     * Returns all of the values for the specified header as an
     * array of <CODE>String</CODE> objects.
     * @param   name  the name of the header for which
     *     values will be returned
     * @return a <CODE>String</CODE> array with all of the values
     *     for the specified header
     * @see #setHeader(java.lang.String, java.lang.String) setHeader(java.lang.String, java.lang.String)
     */
    public String[] getHeader(String name) {

        Vector vector = new Vector();

        for (int i = 0; i < headers.size(); i++) {
            MimeHeader mimeheader = (MimeHeader) headers.elementAt(i);

            if (mimeheader.getName().equalsIgnoreCase(name)
                    && (mimeheader.getValue() != null)) {
                vector.addElement(mimeheader.getValue());
            }
        }

        if (vector.size() == 0) {
            return null;
        } else {
            String as[] = new String[vector.size()];

            vector.copyInto(as);

            return as;
        }
    }

    /**
     * Replaces the current value of the first header entry whose
     *   name matches the given name with the given value, adding a
     *   new header if no existing header name matches. This method
     *   also removes all matching headers after the first one.
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     * @param  name a <CODE>String</CODE> with the
     *     name of the header for which to search
     * @param  value a <CODE>String</CODE> with the
     *     value that will replace the current value of the
     *     specified header
     * @throws java.lang.IllegalArgumentException if there was a
     * problem in the mime header name or the value being set
     * @see #getHeader(java.lang.String) getHeader(java.lang.String)
     */
    public void setHeader(String name, String value) {

        boolean flag = false;

        if ((name == null) || name.equals("")) {
            throw new IllegalArgumentException(
                "Illegal MimeHeader name");
        }

        for (int i = 0; i < headers.size(); i++) {
            MimeHeader mimeheader = (MimeHeader) headers.elementAt(i);

            if (mimeheader.getName().equalsIgnoreCase(name)) {
                if (!flag) {
                    headers.setElementAt(new MimeHeader(mimeheader
                        .getName(), value), i);

                    flag = true;
                } else {
                    headers.removeElementAt(i--);
                }
            }
        }

        if (!flag) {
            addHeader(name, value);
        }
    }

    /**
     * Adds a <CODE>MimeHeader</CODE> object with the specified
     *   name and value to this <CODE>MimeHeaders</CODE> object's
     *   list of headers.
     *
     *   <P>Note that RFC822 headers can contain only US-ASCII
     *   characters.</P>
     * @param  name   a <CODE>String</CODE> with the
     *     name of the header to be added
     * @param  value  a <CODE>String</CODE> with the
     *     value of the header to be added
     * @throws java.lang.IllegalArgumentException if
     *     there was a problem in the mime header name or value
     *     being added
     */
    public void addHeader(String name, String value) {

        if ((name == null) || name.equals("")) {
            throw new IllegalArgumentException(
                "Illegal MimeHeader name");
        }

        int i = headers.size();

        for (int j = i - 1; j >= 0; j--) {
            MimeHeader mimeheader = (MimeHeader) headers.elementAt(j);

            if (mimeheader.getName().equalsIgnoreCase(name)) {
                headers.insertElementAt(new MimeHeader(name, value), j + 1);

                return;
            }
        }

        headers.addElement(new MimeHeader(name, value));
    }

    /**
     * Remove all <CODE>MimeHeader</CODE> objects whose name
     * matches the the given name.
     * @param  name  a <CODE>String</CODE> with the
     *     name of the header for which to search
     */
    public void removeHeader(String name) {

        for (int i = 0; i < headers.size(); i++) {
            MimeHeader mimeheader = (MimeHeader) headers.elementAt(i);

            if (mimeheader.getName().equalsIgnoreCase(name)) {
                headers.removeElementAt(i--);
            }
        }
    }

    /**
     * Removes all the header entries from this <CODE>
     * MimeHeaders</CODE> object.
     */
    public void removeAllHeaders() {
        headers.removeAllElements();
    }

    /**
     * Returns all the headers in this <CODE>MimeHeaders</CODE>
     * object.
     * @return  an <CODE>Iterator</CODE> object over this <CODE>
     *     MimeHeaders</CODE> object's list of <CODE>
     *     MimeHeader</CODE> objects
     */
    public Iterator getAllHeaders() {
        return headers.iterator();
    }

    /**
     * Returns all the <CODE>MimeHeader</CODE> objects whose
     * name matches a name in the given array of names.
     * @param   names an array of <CODE>String</CODE>
     *    objects with the names for which to search
     * @return  an <CODE>Iterator</CODE> object over the <CODE>
     *     MimeHeader</CODE> objects whose name matches one of the
     *     names in the given list
     */
    public Iterator getMatchingHeaders(String names[]) {
        return new MatchingIterator(names, true);
    }

    /**
     * Returns all of the <CODE>MimeHeader</CODE> objects whose
     * name does not match a name in the given array of names.
     * @param   names  an array of <CODE>String</CODE>
     *     objects with the names for which to search
     * @return an <CODE>Iterator</CODE> object over the <CODE>
     *     MimeHeader</CODE> objects whose name does not match one
     *     of the names in the given list
     */
    public Iterator getNonMatchingHeaders(String names[]) {
        return new MatchingIterator(names, false);
    }

    // fixme: does this need to be a Vector? Will a non-synchronized impl of
    // List do?
    /**
     * A <code>Vector</code> containing the headers as <code>MimeHeader</code>
     *              instances.
     */
    protected Vector headers;
}
