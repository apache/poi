/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.openxml4j.opc.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.StringUtil;

/**
 * Represents an immutable MIME ContentType value (RFC 2616 &#167;3.7)
 * <p>
 * media-type = type "/" subtype *( ";" parameter ) type = token<br>
 * subtype = token<br>
 * </p><p>
 * Rule M1.13 : Package implementers shall only create and only recognize parts
 * with a content type; format designers shall specify a content type for each
 * part included in the format. Content types for package parts shall fit the
 * definition and syntax for media types as specified in RFC 2616, \&#167;3.7.
 * </p><p>
 * Rule M1.14: Content types shall not use linear white space either between the
 * type and subtype or between an attribute and its value. Content types also
 * shall not have leading or trailing white spaces. Package implementers shall
 * create only such content types and shall require such content types when
 * retrieving a part from a package; format designers shall specify only such
 * content types for inclusion in the format.
 * </p>
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">http://www.ietf.org/rfc/rfc2045.txt</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2616.txt">http://www.ietf.org/rfc/rfc2616.txt</a>
 */
public final class ContentType {

    /**
     * Type in Type/Subtype.
     */
    private final String type;

    /**
     * Subtype
     */
    private final String subType;

    /**
     * Parameters
     */
    private final Map<String, String> parameters;

    /**
     * Media type compiled pattern, without parameters
     */
    private static final Pattern patternTypeSubType;
    /**
     * Media type compiled pattern, without parameters, matched as a prefix of
     * a content type that has parameters.
     */
    private static final Pattern patternTypeSubTypePrefix;
    /**
     * Pattern to match on just the parameters part, to work
     * around the Java Regexp group capture behaviour
     */
    private static final Pattern patternParams;

    static {
        /*
         * token = 1*<any CHAR except CTLs or separators>
         *
         * separators = "(" | ")" | "<" | ">" | "@" | "," | ";" | ":" | "\" |
         * <"> | "/" | "[" | "]" | "?" | "=" | "{" | "}" | SP | HT
         *
         * CTL = <any US-ASCII control character (octets 0 - 31) and DEL (127)>
         *
         * CHAR = <any US-ASCII character (octets 0 - 127)>
         */
        String token = "[\\x21-\\x7E&&[^()<>@,;:\\\\/\"\\[\\]?={}\\x20\\x09]]";

        /*
         * parameter = attribute "=" value
         *
         * attribute = token
         *
         * value = token | quoted-string
         */
        String parameter = "(" + token + "+)=(\"?" + token + "+\"?)";
        /*
         * Pattern for media type.
         *
         * Don't allow comment, rule M1.15: The package implementer shall
         * require a content type that does not include comments and the format
         * designer shall specify such a content type.
         *
         * comment = "(" *( ctext | quoted-pair | comment ) ")"
         *
         * ctext = <any TEXT excluding "(" and ")">
         *
         * TEXT = <any OCTET except CTLs, but including LWS>
         *
         * LWS = [CRLF] 1*( SP | HT )
         *
         * CR = <US-ASCII CR, carriage return (13)>
         *
         * LF = <US-ASCII LF, linefeed (10)>
         *
         * SP = <US-ASCII SP, space (32)>
         *
         * HT = <US-ASCII HT, horizontal-tab (9)>
         *
         * quoted-pair = "\" CHAR
         */

        patternTypeSubType       = Pattern.compile("^(" + token + "+)/(" +
                                                   token + "+)$");
        patternTypeSubTypePrefix = Pattern.compile("^(" + token + "+)/(" +
                                                   token + "+)");
        patternParams            = Pattern.compile(";" + parameter);
    }

    /**
     * Constructor. Check the input with the RFC 2616 grammar.
     *
     * @param contentType
     *            The content type to store.
     * @throws InvalidFormatException
     *             If the specified content type is not valid with RFC 2616.
     */
    public ContentType(String contentType) throws InvalidFormatException {
        Matcher mMediaType = patternTypeSubType.matcher(contentType);
        this.parameters = new HashMap<>();
        if (!mMediaType.matches()) {
            // How about with parameters? Match them one at a time rather than with
            // a single (;parameter)*$ regex, which recurses per parameter and throws
            // StackOverflowError on a content type with many parameters.
            // A parameter can only be followed by ';' or the end of the string,
            // so this accepts and extracts exactly what that regex did.
            mMediaType = patternTypeSubTypePrefix.matcher(contentType);
            if (!mMediaType.lookingAt()) {
                throw malformed(contentType);
            }
            Matcher mParams = patternParams.matcher(contentType);
            int end = contentType.length();
            for (int pos = mMediaType.end(); pos < end; pos = mParams.end()) {
                mParams.region(pos, end);
                if (!mParams.lookingAt()) {
                    throw malformed(contentType);
                }
                this.parameters.put(mParams.group(1), mParams.group(2));
            }
        }

        // Type/subtype
        this.type = mMediaType.group(1);
        this.subType = mMediaType.group(2);
    }

    private static InvalidFormatException malformed(String contentType) {
        return new InvalidFormatException(
                "The specified content type '"
                + contentType
                + "' is not compliant with RFC 2616: malformed content type.");
    }

    /**
     * Returns the content type as a string, including parameters
     */
    @Override
    public final String toString() {
        return toString(true);
    }

    public final String toString(boolean withParameters) {
        StringBuilder retVal = new StringBuilder(64);
        retVal.append(this.getType());
        retVal.append('/');
        retVal.append(this.getSubType());

        if (withParameters) {
            for (Entry<String, String> me : parameters.entrySet()) {
                retVal.append(';');
                retVal.append(me.getKey());
                retVal.append('=');
                retVal.append(me.getValue());
            }
        }
        return retVal.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ContentType)
                && StringUtil.equalsIgnoreCase(this.toString(), obj.toString());
    }

    @Override
    public int hashCode() {
        // must be consistent with the case-insensitive comparison in equals()
        return this.toString().toLowerCase(Locale.ROOT).hashCode();
    }

    /* Getters */

    /**
     * Get the subtype.
     *
     * @return The subtype of this content type.
     */
    public String getSubType() {
        return this.subType;
    }

    /**
     * Get the type.
     *
     * @return The type of this content type.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Does this content type have any parameters associated with it?
     */
    public boolean hasParameters() {
        return (parameters != null) && !parameters.isEmpty();
    }

    /**
     * Return the parameter keys
     */
    public String[] getParameterKeys() {
        if (parameters == null)
            return new String[0];
        return parameters.keySet().toArray(new String[0]);
    }

    /**
     * Gets the value associated to the specified key.
     *
     * @param key
     *            The key of the key/value pair.
     * @return The value associated to the specified key.
     */
    public String getParameter(String key) {
        return parameters.get(key);
    }
}
