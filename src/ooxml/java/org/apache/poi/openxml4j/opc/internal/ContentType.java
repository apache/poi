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

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

/**
 * Represents a immutable MIME ContentType value (RFC 2616 &#167;3.7)
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
 * @author Julien Chable
 * @version 0.1
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2045.txt">http://www.ietf.org/rfc/rfc2045.txt</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2616.txt">http://www.ietf.org/rfc/rfc2616.txt</a>
 */
public final class ContentType {

	/**
	 * Type in Type/Subtype.
	 */
	private String type;

	/**
	 * Subtype
	 */
	private String subType;

	/**
	 * Parameters
	 */
	private Hashtable<String, String> parameters;

	/**
	 * Media type compiled pattern for parameters.
	 */
	private final static Pattern patternMediaType;

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
		String token = "[\\x21-\\x7E&&[^\\(\\)<>@,;:\\\\/\"\\[\\]\\?={}\\x20\\x09]]";

		/*
		 * parameter = attribute "=" value
		 *
		 * attribute = token
		 *
		 * value = token | quoted-string
		 */
		// Keep for future use with parameter:
		// String parameter = "(" + token + "+)=(\"?" + token + "+\"?)";
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

		// Keep for future use with parameter:
		// patternMediaType = Pattern.compile("^(" + token + "+)/(" + token
		// + "+)(;" + parameter + ")*$");
		patternMediaType = Pattern.compile("^(" + token + "+)/(" + token
				+ "+)$");
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
		// Conversion en US-ASCII
		String contentTypeASCII = null;
		try {
			contentTypeASCII = new String(contentType.getBytes(), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			throw new InvalidFormatException(
					"The specified content type is not an ASCII value.");
		}

		Matcher mMediaType = patternMediaType.matcher(contentTypeASCII);
		if (!mMediaType.matches())
			throw new InvalidFormatException(
					"The specified content type '"
							+ contentType
							+ "' is not compliant with RFC 2616: malformed content type.");

		// Type/subtype
		if (mMediaType.groupCount() >= 2) {
			this.type = mMediaType.group(1);
			this.subType = mMediaType.group(2);
			// Parameters
			this.parameters = new Hashtable<String, String>(1);
			for (int i = 4; i <= mMediaType.groupCount()
					&& (mMediaType.group(i) != null); i += 2) {
				this.parameters.put(mMediaType.group(i), mMediaType
						.group(i + 1));
			}
		}
	}

	@Override
	public final String toString() {
		StringBuffer retVal = new StringBuffer();
		retVal.append(this.getType());
		retVal.append("/");
		retVal.append(this.getSubType());
		// Keep for future implementation if needed
		// for (String key : parameters.keySet()) {
		// retVal.append(";");
		// retVal.append(key);
		// retVal.append("=");
		// retVal.append(parameters.get(key));
		// }
		return retVal.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return (!(obj instanceof ContentType))
				|| (this.toString().equalsIgnoreCase(obj.toString()));
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
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
	 * Gets the value associated to the specified key.
	 *
	 * @param key
	 *            The key of the key/value pair.
	 * @return The value associated to the specified key.
	 */
	public String getParameters(String key) {
		return parameters.get(key);
	}
}
