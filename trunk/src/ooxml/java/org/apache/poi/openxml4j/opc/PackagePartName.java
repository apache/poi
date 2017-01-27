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

package org.apache.poi.openxml4j.opc;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JRuntimeException;

/**
 * An immutable Open Packaging Convention compliant part name.
 *
 * @author Julien Chable
 *
 * @see <a href="http://www.ietf.org/rfc/rfc3986.txt">http://www.ietf.org/rfc/rfc3986.txt</a>
 */
public final class PackagePartName implements Comparable<PackagePartName> {

	/**
	 * Part name stored as an URI.
	 */
	private URI partNameURI;

	/*
	 * URI Characters definition (RFC 3986)
	 */

	/**
	 * Reserved characters for sub delimitations.
	 */
	private static String[] RFC3986_PCHAR_SUB_DELIMS = { "!", "$", "&", "'",
			"(", ")", "*", "+", ",", ";", "=" };

	/**
	 * Unreserved character (+ ALPHA & DIGIT).
	 */
	private static String[] RFC3986_PCHAR_UNRESERVED_SUP = { "-", ".", "_", "~" };

	/**
	 * Authorized reserved characters for pChar.
	 */
	private static String[] RFC3986_PCHAR_AUTHORIZED_SUP = { ":", "@" };

	/**
	 * Flag to know if this part name is from a relationship part name.
	 */
	private boolean isRelationship;

	/**
	 * Constructor. Makes a ValidPartName object from a java.net.URI
	 *
	 * @param uri
	 *            The URI to validate and to transform into ValidPartName.
	 * @param checkConformance
	 *            Flag to specify if the contructor have to validate the OPC
	 *            conformance. Must be always <code>true</code> except for
	 *            special URI like '/' which is needed for internal use by
	 *            OpenXML4J but is not valid.
	 * @throws InvalidFormatException
	 *             Throw if the specified part name is not conform to Open
	 *             Packaging Convention specifications.
	 * @see java.net.URI
	 */
	PackagePartName(URI uri, boolean checkConformance)
			throws InvalidFormatException {
		if (checkConformance) {
			throwExceptionIfInvalidPartUri(uri);
		} else {
			if (!PackagingURIHelper.PACKAGE_ROOT_URI.equals(uri)) {
				throw new OpenXML4JRuntimeException(
						"OCP conformance must be check for ALL part name except special cases : ['/']");
			}
		}
		this.partNameURI = uri;
		this.isRelationship = isRelationshipPartURI(this.partNameURI);
	}

	/**
	 * Constructor. Makes a ValidPartName object from a String part name.
	 *
	 * @param partName
	 *            Part name to valid and to create.
	 * @param checkConformance
	 *            Flag to specify if the contructor have to validate the OPC
	 *            conformance. Must be always <code>true</code> except for
	 *            special URI like '/' which is needed for internal use by
	 *            OpenXML4J but is not valid.
	 * @throws InvalidFormatException
	 *             Throw if the specified part name is not conform to Open
	 *             Packaging Convention specifications.
	 */
	PackagePartName(String partName, boolean checkConformance)
			throws InvalidFormatException {
		URI partURI;
		try {
			partURI = new URI(partName);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(
					"partName argmument is not a valid OPC part name !");
		}

		if (checkConformance) {
			throwExceptionIfInvalidPartUri(partURI);
		} else {
			if (!PackagingURIHelper.PACKAGE_ROOT_URI.equals(partURI)) {
				throw new OpenXML4JRuntimeException(
						"OCP conformance must be check for ALL part name except special cases : ['/']");
			}
		}
		this.partNameURI = partURI;
		this.isRelationship = isRelationshipPartURI(this.partNameURI);
	}

	/**
	 * Check if the specified part name is a relationship part name.
	 *
	 * @param partUri
	 *            The URI to check.
	 * @return <code>true</code> if this part name respect the relationship
	 *         part naming convention else <code>false</code>.
	 */
	private boolean isRelationshipPartURI(URI partUri) {
		if (partUri == null)
			throw new IllegalArgumentException("partUri");

		return partUri.getPath().matches(
				"^.*/" + PackagingURIHelper.RELATIONSHIP_PART_SEGMENT_NAME + "/.*\\"
						+ PackagingURIHelper.RELATIONSHIP_PART_EXTENSION_NAME
						+ "$");
	}

	/**
	 * Know if this part name is a relationship part name.
	 *
	 * @return <code>true</code> if this part name respect the relationship
	 *         part naming convention else <code>false</code>.
	 */
	public boolean isRelationshipPartURI() {
		return this.isRelationship;
	}

	/**
	 * Throws an exception (of any kind) if the specified part name does not
	 * follow the Open Packaging Convention specifications naming rules.
	 *
	 * @param partUri
	 *            The part name to check.
	 * @throws Exception
	 *             Throws if the part name is invalid.
	 */
	private static void throwExceptionIfInvalidPartUri(URI partUri)
			throws InvalidFormatException {
		if (partUri == null)
			throw new IllegalArgumentException("partUri");
		// Check if the part name URI is empty [M1.1]
		throwExceptionIfEmptyURI(partUri);

		// Check if the part name URI is absolute
		throwExceptionIfAbsoluteUri(partUri);

		// Check if the part name URI starts with a forward slash [M1.4]
		throwExceptionIfPartNameNotStartsWithForwardSlashChar(partUri);

		// Check if the part name URI ends with a forward slash [M1.5]
		throwExceptionIfPartNameEndsWithForwardSlashChar(partUri);

		// Check if the part name does not have empty segments. [M1.3]
		// Check if a segment ends with a dot ('.') character. [M1.9]
		throwExceptionIfPartNameHaveInvalidSegments(partUri);
	}

	/**
	 * Throws an exception if the specified URI is empty. [M1.1]
	 *
	 * @param partURI
	 *            Part URI to check.
	 * @throws InvalidFormatException
	 *             If the specified URI is empty.
	 */
	private static void throwExceptionIfEmptyURI(URI partURI)
			throws InvalidFormatException {
		if (partURI == null)
			throw new IllegalArgumentException("partURI");

		String uriPath = partURI.getPath();
		if (uriPath.length() == 0
				|| ((uriPath.length() == 1) && (uriPath.charAt(0) == PackagingURIHelper.FORWARD_SLASH_CHAR)))
			throw new InvalidFormatException(
					"A part name shall not be empty [M1.1]: "
							+ partURI.getPath());
	}

	/**
	 * Throws an exception if the part name has empty segments. [M1.3]
	 *
	 * Throws an exception if a segment any characters other than pchar
	 * characters. [M1.6]
	 *
	 * Throws an exception if a segment contain percent-encoded forward slash
	 * ('/'), or backward slash ('\') characters. [M1.7]
	 *
	 * Throws an exception if a segment contain percent-encoded unreserved
	 * characters. [M1.8]
	 *
	 * Throws an exception if the specified part name's segments end with a dot
	 * ('.') character. [M1.9]
	 *
	 * Throws an exception if a segment doesn't include at least one non-dot
	 * character. [M1.10]
	 *
	 * @param partUri
	 *            The part name to check.
	 * @throws InvalidFormatException
	 *             if the specified URI contain an empty segments or if one the
	 *             segments contained in the part name, ends with a dot ('.')
	 *             character.
	 */
	private static void throwExceptionIfPartNameHaveInvalidSegments(URI partUri)
			throws InvalidFormatException {
		if (partUri == null) {
			throw new IllegalArgumentException("partUri");
		}

		// Split the URI into several part and analyze each
		String[] segments = partUri.toASCIIString().split("/");
		if (segments.length <= 1 || !segments[0].equals(""))
			throw new InvalidFormatException(
					"A part name shall not have empty segments [M1.3]: "
							+ partUri.getPath());

		for (int i = 1; i < segments.length; ++i) {
			String seg = segments[i];
			if (seg == null || "".equals(seg)) {
				throw new InvalidFormatException(
						"A part name shall not have empty segments [M1.3]: "
								+ partUri.getPath());
			}

			if (seg.endsWith(".")) {
				throw new InvalidFormatException(
						"A segment shall not end with a dot ('.') character [M1.9]: "
								+ partUri.getPath());
			}

			if ("".equals(seg.replaceAll("\\\\.", ""))) {
				// Normally will never been invoked with the previous
				// implementation rule [M1.9]
				throw new InvalidFormatException(
						"A segment shall include at least one non-dot character. [M1.10]: "
								+ partUri.getPath());
			}

			// Check for rule M1.6, M1.7, M1.8
			checkPCharCompliance(seg);
		}
	}

	/**
	 * Throws an exception if a segment any characters other than pchar
	 * characters. [M1.6]
	 *
	 * Throws an exception if a segment contain percent-encoded forward slash
	 * ('/'), or backward slash ('\') characters. [M1.7]
	 *
	 * Throws an exception if a segment contain percent-encoded unreserved
	 * characters. [M1.8]
	 *
	 * @param segment
	 *            The segment to check
	 */
	private static void checkPCharCompliance(String segment)
			throws InvalidFormatException {
		boolean errorFlag;
		final int length = segment.length();
		for (int i = 0; i < length; ++i) {
			char c = segment.charAt(i);
			errorFlag = true;

			/* Check rule M1.6 */

			// Check for digit or letter
			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')
					|| (c >= '0' && c <= '9')) {
				errorFlag = false;
			} else {
				// Check "-", ".", "_", "~"
				for (int j = 0; j < RFC3986_PCHAR_UNRESERVED_SUP.length; ++j) {
					if (c == RFC3986_PCHAR_UNRESERVED_SUP[j].charAt(0)) {
						errorFlag = false;
						break;
					}
				}

				// Check ":", "@"
				for (int j = 0; errorFlag
						&& j < RFC3986_PCHAR_AUTHORIZED_SUP.length; ++j) {
					if (c == RFC3986_PCHAR_AUTHORIZED_SUP[j].charAt(0)) {
						errorFlag = false;
					}
				}

				// Check "!", "$", "&", "'", "(", ")", "*", "+", ",", ";", "="
				for (int j = 0; errorFlag
						&& j < RFC3986_PCHAR_SUB_DELIMS.length; ++j) {
					if (c == RFC3986_PCHAR_SUB_DELIMS[j].charAt(0)) {
						errorFlag = false;
					}
				}
			}

			if (errorFlag && c == '%') {
				// We certainly found an encoded character, check for length
				// now ( '%' HEXDIGIT HEXDIGIT)
				if (((length - i) < 2)) {
					throw new InvalidFormatException("The segment " + segment
							+ " contain invalid encoded character !");
				}

				// If not percent encoded character error occur then reset the
				// flag -> the character is valid
				errorFlag = false;

				// Decode the encoded character
				char decodedChar = (char) Integer.parseInt(segment.substring(
						i + 1, i + 3), 16);
				i += 2;

				/* Check rule M1.7 */
				if (decodedChar == '/' || decodedChar == '\\')
					throw new InvalidFormatException(
							"A segment shall not contain percent-encoded forward slash ('/'), or backward slash ('\') characters. [M1.7]");

				/* Check rule M1.8 */

				// Check for unreserved character like define in RFC3986
				if ((decodedChar >= 'A' && decodedChar <= 'Z')
						|| (decodedChar >= 'a' && decodedChar <= 'z')
						|| (decodedChar >= '0' && decodedChar <= '9'))
					errorFlag = true;

				// Check for unreserved character "-", ".", "_", "~"
				for (int j = 0; !errorFlag
						&& j < RFC3986_PCHAR_UNRESERVED_SUP.length; ++j) {
					if (c == RFC3986_PCHAR_UNRESERVED_SUP[j].charAt(0)) {
						errorFlag = true;
						break;
					}
				}
				if (errorFlag)
					throw new InvalidFormatException(
							"A segment shall not contain percent-encoded unreserved characters. [M1.8]");
			}

			if (errorFlag)
				throw new InvalidFormatException(
						"A segment shall not hold any characters other than pchar characters. [M1.6]");
		}
	}

	/**
	 * Throws an exception if the specified part name doesn't start with a
	 * forward slash character '/'. [M1.4]
	 *
	 * @param partUri
	 *            The part name to check.
	 * @throws InvalidFormatException
	 *             If the specified part name doesn't start with a forward slash
	 *             character '/'.
	 */
	private static void throwExceptionIfPartNameNotStartsWithForwardSlashChar(
			URI partUri) throws InvalidFormatException {
		String uriPath = partUri.getPath();
		if (uriPath.length() > 0
				&& uriPath.charAt(0) != PackagingURIHelper.FORWARD_SLASH_CHAR)
			throw new InvalidFormatException(
					"A part name shall start with a forward slash ('/') character [M1.4]: "
							+ partUri.getPath());
	}

	/**
	 * Throws an exception if the specified part name ends with a forwar slash
	 * character '/'. [M1.5]
	 *
	 * @param partUri
	 *            The part name to check.
	 * @throws InvalidFormatException
	 *             If the specified part name ends with a forwar slash character
	 *             '/'.
	 */
	private static void throwExceptionIfPartNameEndsWithForwardSlashChar(
			URI partUri) throws InvalidFormatException {
		String uriPath = partUri.getPath();
		if (uriPath.length() > 0
				&& uriPath.charAt(uriPath.length() - 1) == PackagingURIHelper.FORWARD_SLASH_CHAR)
			throw new InvalidFormatException(
					"A part name shall not have a forward slash as the last character [M1.5]: "
							+ partUri.getPath());
	}

	/**
	 * Throws an exception if the specified URI is absolute.
	 *
	 * @param partUri
	 *            The URI to check.
	 * @throws InvalidFormatException
	 *             Throws if the specified URI is absolute.
	 */
	private static void throwExceptionIfAbsoluteUri(URI partUri)
			throws InvalidFormatException {
		if (partUri.isAbsolute())
			throw new InvalidFormatException("Absolute URI forbidden: "
					+ partUri);
	}

	/**
	 * Compare two part names following the rule M1.12 :
	 *
	 * Part name equivalence is determined by comparing part names as
	 * case-insensitive ASCII strings. Packages shall not contain equivalent
	 * part names and package implementers shall neither create nor recognize
	 * packages with equivalent part names. [M1.12]
	 */
        @Override
        public int compareTo(PackagePartName other)
        {
            // compare with natural sort order
            return compare(this, other);
        }


	/**
	 * Retrieves the extension of the part name if any. If there is no extension
	 * returns an empty String. Example : '/document/content.xml' => 'xml'
	 *
	 * @return The extension of the part name.
	 */
	public String getExtension() {
		String fragment = this.partNameURI.getPath();
		if (fragment.length() > 0) {
			int i = fragment.lastIndexOf(".");
			if (i > -1)
				return fragment.substring(i + 1);
		}
		return "";
	}

	/**
	 * Get this part name.
	 *
	 * @return The name of this part name.
	 */
	public String getName() {
		return this.partNameURI.toASCIIString();
	}

	/**
	 * Part name equivalence is determined by comparing part names as
	 * case-insensitive ASCII strings. Packages shall not contain equivalent
	 * part names and package implementers shall neither create nor recognize
	 * packages with equivalent part names. [M1.12]
	 */
	@Override
	public boolean equals(Object other) {
            if (other instanceof PackagePartName) {
                // String.equals() is compatible with our compareTo(), but cheaper
                return this.partNameURI.toASCIIString().toLowerCase(Locale.ROOT).equals
                (
                    ((PackagePartName) other).partNameURI.toASCIIString().toLowerCase(Locale.ROOT)
                );
            } else {
                return false;
            }
        }

	@Override
	public int hashCode() {
		return this.partNameURI.toASCIIString().toLowerCase(Locale.ROOT).hashCode();
	}

	@Override
	public String toString() {
		return getName();
	}

	/* Getters and setters */

	/**
	 * Part name property getter.
	 *
	 * @return This part name URI.
	 */
	public URI getURI() {
		return this.partNameURI;
	}


    /**
     * A natural sort order for package part names, consistent with the
     * requirements of {@code java.util.Comparator}, but simply implemented
     * as a static method.
     * <p>
     * For example, this sorts "file10.png" after "file2.png" (comparing the
     * numerical portion), but sorts "File10.png" before "file2.png"
     * (lexigraphical sort)
     *
     * <p>
     * When comparing part names, the rule M1.12 is followed:
     *
     * Part name equivalence is determined by comparing part names as
     * case-insensitive ASCII strings. Packages shall not contain equivalent
     * part names and package implementers shall neither create nor recognize
     * packages with equivalent part names. [M1.12]
     */
    public static int compare(PackagePartName obj1, PackagePartName obj2)
    {
        // NOTE could also throw a NullPointerException() if desired
        if (obj1 == null)
        {
            // (null) == (null), (null) < (non-null)
            return (obj2 == null ? 0 : -1);
        }
        else if (obj2 == null)
        {
            // (non-null) > (null)
            return 1;
        }

        return compare
        (
            obj1.getURI().toASCIIString().toLowerCase(Locale.ROOT),
            obj2.getURI().toASCIIString().toLowerCase(Locale.ROOT)
        );
    }


    /**
     * A natural sort order for strings, consistent with the
     * requirements of {@code java.util.Comparator}, but simply implemented
     * as a static method.
     * <p>
     * For example, this sorts "file10.png" after "file2.png" (comparing the
     * numerical portion), but sorts "File10.png" before "file2.png"
     * (lexigraphical sort)
     */
    public static int compare(String str1, String str2)
    {
        if (str1 == null)
        {
            // (null) == (null), (null) < (non-null)
            return (str2 == null ? 0 : -1);
        }
        else if (str2 == null)
        {
            // (non-null) > (null)
            return 1;
        }

        int len1 = str1.length();
        int len2 = str2.length();
        for (int idx1 = 0, idx2 = 0; idx1 < len1 && idx2 < len2; /*nil*/)
        {
            char c1 = str1.charAt(idx1++);
            char c2 = str2.charAt(idx2++);

            if (Character.isDigit(c1) && Character.isDigit(c2))
            {
                int beg1 = idx1 - 1;  // undo previous increment
                while (idx1 < len1 && Character.isDigit(str1.charAt(idx1)))
                {
                    ++idx1;
                }

                int beg2 = idx2 - 1;  // undo previous increment
                while (idx2 < len2 && Character.isDigit(str2.charAt(idx2)))
                {
                    ++idx2;
                }

                // note: BigInteger for extra safety
                int cmp = new BigInteger(str1.substring(beg1, idx1)).compareTo
                (
                    new BigInteger(str2.substring(beg2, idx2))
                );
                if (cmp != 0) return cmp;
            }
            else if (c1 != c2)
            {
                return (c1 - c2);
            }
        }

        return (len1 - len2);
    }

}

/* ************************************************************************** */
