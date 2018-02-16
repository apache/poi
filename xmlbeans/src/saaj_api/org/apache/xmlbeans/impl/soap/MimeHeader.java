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
 * An object that stores a MIME header name and its value. One
 *   or more <CODE>MimeHeader</CODE> objects may be contained in a
 *   <CODE>MimeHeaders</CODE> object.
 * @see MimeHeaders MimeHeaders
 */
public class MimeHeader {

    /**
     * Constructs a <CODE>MimeHeader</CODE> object initialized
     * with the given name and value.
     * @param  name a <CODE>String</CODE> giving the
     *     name of the header
     * @param  value a <CODE>String</CODE> giving the
     *     value of the header
     */
    public MimeHeader(String name, String value) {
        this.name  = name;
        this.value = value;
    }

    /**
     * Returns the name of this <CODE>MimeHeader</CODE>
     * object.
     * @return  the name of the header as a <CODE>String</CODE>
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value of this <CODE>MimeHeader</CODE>
     * object.
     * @return the value of the header as a <CODE>String</CODE>
     */
    public String getValue() {
        return value;
    }

    private String name;

    private String value;
}
