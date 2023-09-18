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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;

/**
 * Helper for part and pack URI.
 *
 * @version 0.1
 */
public final class PackagingURIHelper {
    // FIXME: this class implements a lot of path joining and splitting logic that
    // is already implemented in java.nio.file.Path.
    // This class should heavily leverage Java library code to reduce the number of lines of code that POI has to maintain and test
    private static final Logger LOG = LogManager.getLogger(PackagingURIHelper.class);

    /**
     * Package root URI.
     */
    private static URI packageRootUri;

    /**
     * Extension name of a relationship part.
     */
    public static final String RELATIONSHIP_PART_EXTENSION_NAME = ".rels";

    /**
     * Segment name of a relationship part.
     */
    public static final String RELATIONSHIP_PART_SEGMENT_NAME = "_rels";

    /**
     * Segment name of the package properties folder.
     */
    public static final String PACKAGE_PROPERTIES_SEGMENT_NAME = "docProps";

    /**
     * Core package properties art name.
     */
    public static final String PACKAGE_CORE_PROPERTIES_NAME = "core.xml";

    /**
     * Forward slash URI separator.
     */
    public static final char FORWARD_SLASH_CHAR = '/';

    /**
     * Forward slash URI separator.
     */
    public static final String FORWARD_SLASH_STRING = "/";

    /**
     * Package relationships part URI
     */
    public static final URI PACKAGE_RELATIONSHIPS_ROOT_URI;

    /**
     * Package relationships part name.
     */
    public static final PackagePartName PACKAGE_RELATIONSHIPS_ROOT_PART_NAME;

    /**
     * Core properties part URI.
     */
    public static final URI CORE_PROPERTIES_URI;

    /**
     * Core properties partname.
     */
    public static final PackagePartName CORE_PROPERTIES_PART_NAME;

    /**
     * Root package URI.
     */
    public static final URI PACKAGE_ROOT_URI;

    /**
     * Root package part name.
     */
    public static final PackagePartName PACKAGE_ROOT_PART_NAME;

    /* Static initialization */
    static {
        // Make URI
        URI uriPACKAGE_ROOT_URI = null;
        URI uriPACKAGE_RELATIONSHIPS_ROOT_URI = null;
        URI uriPACKAGE_PROPERTIES_URI = null;
        try {
            uriPACKAGE_ROOT_URI = new URI("/");
            uriPACKAGE_RELATIONSHIPS_ROOT_URI = new URI(FORWARD_SLASH_CHAR
                    + RELATIONSHIP_PART_SEGMENT_NAME + FORWARD_SLASH_CHAR
                    + RELATIONSHIP_PART_EXTENSION_NAME);
            packageRootUri = new URI("/");
            uriPACKAGE_PROPERTIES_URI = new URI(FORWARD_SLASH_CHAR
                    + PACKAGE_PROPERTIES_SEGMENT_NAME + FORWARD_SLASH_CHAR
                    + PACKAGE_CORE_PROPERTIES_NAME);
        } catch (URISyntaxException e) {
            // Should never happen in production as all data are fixed
        }
        PACKAGE_ROOT_URI = uriPACKAGE_ROOT_URI;
        PACKAGE_RELATIONSHIPS_ROOT_URI = uriPACKAGE_RELATIONSHIPS_ROOT_URI;
        CORE_PROPERTIES_URI = uriPACKAGE_PROPERTIES_URI;

        // Make part name from previous URI
        PackagePartName tmpPACKAGE_ROOT_PART_NAME = null;
        PackagePartName tmpPACKAGE_RELATIONSHIPS_ROOT_PART_NAME = null;
        PackagePartName tmpCORE_PROPERTIES_URI = null;
        try {
            tmpPACKAGE_RELATIONSHIPS_ROOT_PART_NAME = createPartName(PACKAGE_RELATIONSHIPS_ROOT_URI);
            tmpCORE_PROPERTIES_URI = createPartName(CORE_PROPERTIES_URI);
            tmpPACKAGE_ROOT_PART_NAME = new PackagePartName(PACKAGE_ROOT_URI,
                    false);
        } catch (InvalidFormatException e) {
            // Should never happen in production as all data are fixed
        }
        PACKAGE_RELATIONSHIPS_ROOT_PART_NAME = tmpPACKAGE_RELATIONSHIPS_ROOT_PART_NAME;
        CORE_PROPERTIES_PART_NAME = tmpCORE_PROPERTIES_URI;
        PACKAGE_ROOT_PART_NAME = tmpPACKAGE_ROOT_PART_NAME;
    }

    private static final Pattern missingAuthPattern = Pattern.compile("\\w+://");

    /**
     * Gets the URI for the package root.
     *
     * @return URI of the package root.
     */
    public static URI getPackageRootUri() {
        return packageRootUri;
    }

    /**
     * Know if the specified URI is a relationship part name.
     *
     * @param partUri
     *            URI to check.
     * @return <i>true</i> if the URI <i>false</i>.
     */
    public static boolean isRelationshipPartURI(URI partUri) {
        if (partUri == null)
            throw new IllegalArgumentException("partUri");

        return partUri.getPath().matches(
                ".*" + RELATIONSHIP_PART_SEGMENT_NAME + ".*"
                        + RELATIONSHIP_PART_EXTENSION_NAME + "$");
    }

    /**
     * Get file name from the specified URI.
     */
    public static String getFilename(URI uri) {
        if (uri != null) {
            String path = uri.getPath();
            int len = path.length();
            int num2 = len;
            while (--num2 >= 0) {
                char ch1 = path.charAt(num2);
                if (ch1 == PackagingURIHelper.FORWARD_SLASH_CHAR)
                    return path.substring(num2 + 1, len);
            }
        }
        return "";
    }

    /**
     * Get the file name without the trailing extension.
     */
    public static String getFilenameWithoutExtension(URI uri) {
        String filename = getFilename(uri);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1)
            return filename;
        return filename.substring(0, dotIndex);
    }

    /**
     * Get the directory path from the specified URI.
     */
    public static URI getPath(URI uri) {
        if (uri != null) {
            String path = uri.getPath();
            int num2 = path.length();
            while (--num2 >= 0) {
                char ch1 = path.charAt(num2);
                if (ch1 == PackagingURIHelper.FORWARD_SLASH_CHAR) {
                    try {
                        return new URI(path.substring(0, num2));
                    } catch (URISyntaxException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Combine two URIs.
     *
     * @param prefix the prefix URI
     * @param suffix the suffix URI
     *
     * @return the combined URI
     */
    public static URI combine(URI prefix, URI suffix) {
        URI retUri;
        try {
            retUri = new URI(combine(prefix.getPath(), suffix.getPath()));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(
                    "Prefix and suffix can't be combine !");
        }
        return retUri;
    }

    /**
     * Combine a string URI with a prefix and a suffix.
     */
    public static String combine(String prefix, String suffix) {
        if (!prefix.endsWith(FORWARD_SLASH_STRING) && !suffix.startsWith(FORWARD_SLASH_STRING))
            return prefix + FORWARD_SLASH_CHAR + suffix;
        else if (prefix.endsWith(FORWARD_SLASH_STRING) ^ suffix.startsWith(FORWARD_SLASH_STRING))
            return prefix + suffix;
        else
            return "";
    }

    /**
     * Fully relativize the source part URI against the target part URI.
     *
     * @param sourceURI
     *            The source part URI.
     * @param targetURI
     *            The target part URI.
     * @param  msCompatible if true then remove leading slash from the relativized URI.
     *         This flag violates [M1.4]: A part name shall start with a forward slash ('/') character, but
     *         allows generating URIs compatible with MS Office and OpenOffice.
     * @return A fully relativize part name URI ('word/media/image1.gif',
     *         '/word/document.xml' =&gt; 'media/image1.gif') else
     *         {@code null}.
     */
    public static URI relativizeURI(URI sourceURI, URI targetURI, boolean msCompatible) {
        StringBuilder retVal = new StringBuilder();
        String[] segmentsSource = sourceURI.getPath().split("/", -1);
        String[] segmentsTarget = targetURI.getPath().split("/", -1);

        // If the source URI is empty
        if (segmentsSource.length == 0) {
            throw new IllegalArgumentException(
                    "Can't relativize an empty source URI !");
        }

        // If target URI is empty
        if (segmentsTarget.length == 0) {
            throw new IllegalArgumentException(
                    "Can't relativize an empty target URI !");
        }

        // If the source is the root, then the relativized
        //  form must actually be an absolute URI
        if(sourceURI.toString().equals("/")) {
            String path = targetURI.getPath();
            if(msCompatible && path.length() > 0 && path.charAt(0) == '/') {
                try {
                    targetURI = new URI(path.substring(1));
                } catch (Exception e) {
                    LOG.atWarn().withThrowable(e).log("Failed to relativize");
                    return null;
                }
            }
            return targetURI;
        }


        // Relativize the source URI against the target URI.
        // First up, figure out how many steps along we can go
        // and still have them be the same
        int segmentsTheSame = 0;
        for (int i = 0; i < segmentsSource.length && i < segmentsTarget.length; i++) {
            if (segmentsSource[i].equals(segmentsTarget[i])) {
                // Match so far, good
                segmentsTheSame++;
            } else {
                break;
            }
        }

        // If we didn't have a good match or at least except a first empty element
        if ((segmentsTheSame == 0 || segmentsTheSame == 1) &&
                segmentsSource[0].isEmpty() && segmentsTarget[0].isEmpty()) {
            for (int i = 0; i < segmentsSource.length - 2; i++) {
                retVal.append("../");
            }
            for (int i = 0; i < segmentsTarget.length; i++) {
                if (segmentsTarget[i].isEmpty())
                    continue;
                retVal.append(segmentsTarget[i]);
                if (i != segmentsTarget.length - 1)
                    retVal.append("/");
            }

            try {
                return new URI(retVal.toString());
            } catch (Exception e) {
                LOG.atWarn().withThrowable(e).log("Failed to relativize");
                return null;
            }
        }

        // Special case for where the two are the same
        if (segmentsTheSame == segmentsSource.length
                && segmentsTheSame == segmentsTarget.length) {
            if(sourceURI.equals(targetURI)){
                // if source and target are the same they should be resolved to the last segment,
                // Example: if a slide references itself, e.g. the source URI is
                // "/ppt/slides/slide1.xml" and the targetURI is "slide1.xml" then
                // this it should be relativized as "slide1.xml", i.e. the last segment.
                retVal.append(segmentsSource[segmentsSource.length - 1]);
            }

        } else {
            // Matched for so long, but no more

            // Do we need to go up a directory or two from
            // the source to get here?
            // (If it's all the way up, then don't bother!)
            if (segmentsTheSame == 1) {
                retVal.append("/");
            } else {
                for (int j = segmentsTheSame; j < segmentsSource.length - 1; j++) {
                    retVal.append("../");
                }
            }

            // Now go from here on down
            for (int j = segmentsTheSame; j < segmentsTarget.length; j++) {
                if (retVal.length() > 0
                        && retVal.charAt(retVal.length() - 1) != '/') {
                    retVal.append("/");
                }
                retVal.append(segmentsTarget[j]);
            }
        }

        // if the target had a fragment then append it to the result
        String fragment = targetURI.getRawFragment();
        if (fragment != null) {
            retVal.append("#").append(fragment);
        }

        try {
            return new URI(retVal.toString());
        } catch (Exception e) {
            LOG.atWarn().withThrowable(e).log("Failed to relativize");
            return null;
        }
    }

    /**
     * Fully relativize the source part URI against the target part URI.
     *
     * @param sourceURI
     *            The source part URI.
     * @param targetURI
     *            The target part URI.
     * @return A fully relativize part name URI ('word/media/image1.gif',
     *         '/word/document.xml' =&gt; 'media/image1.gif') else
     *         {@code null}.
     */
    public static URI relativizeURI(URI sourceURI, URI targetURI) {
        return relativizeURI(sourceURI, targetURI, false);
    }

    /**
     * Resolve a source uri against a target.
     *
     * @param sourcePartUri
     *            The source URI.
     * @param targetUri
     *            The target URI.
     * @return The resolved URI.
     */
    public static URI resolvePartUri(URI sourcePartUri, URI targetUri) {
        if (sourcePartUri == null || sourcePartUri.isAbsolute()) {
            throw new IllegalArgumentException("sourcePartUri invalid - "
                    + sourcePartUri);
        }

        if (targetUri == null || targetUri.isAbsolute()) {
            throw new IllegalArgumentException("targetUri invalid - "
                    + targetUri);
        }

        return sourcePartUri.resolve(targetUri);
    }

    /**
     * Get URI from a string path.
     */
    public static URI getURIFromPath(String path) {
        URI retUri;
        try {
            retUri = toURI(path);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("path");
        }
        return retUri;
    }

    /**
     * Get the source part URI from a specified relationships part.
     *
     * @param relationshipPartUri
     *            The relationship part use to retrieve the source part.
     * @return The source part URI from the specified relationships part.
     */
    public static URI getSourcePartUriFromRelationshipPartUri(
            URI relationshipPartUri) {
        if (relationshipPartUri == null)
            throw new IllegalArgumentException(
                    "Must not be null");

        if (!isRelationshipPartURI(relationshipPartUri))
            throw new IllegalArgumentException(
                    "Must be a relationship part");

        if (relationshipPartUri.compareTo(PACKAGE_RELATIONSHIPS_ROOT_URI) == 0)
            return PACKAGE_ROOT_URI;

        String filename = relationshipPartUri.getPath();
        String filenameWithoutExtension = getFilenameWithoutExtension(relationshipPartUri);
        filename = filename
                .substring(0, ((filename.length() - filenameWithoutExtension
                        .length()) - RELATIONSHIP_PART_EXTENSION_NAME.length()));
        filename = filename.substring(0, filename.length()
                - RELATIONSHIP_PART_SEGMENT_NAME.length() - 1);
        filename = combine(filename, filenameWithoutExtension);
        return getURIFromPath(filename);
    }

    /**
     * Create an OPC compliant part name by throwing an exception if the URI is
     * not valid.
     *
     * @param partUri
     *            The part name URI to validate.
     * @return A valid part name object, else <code>null</code>.
     * @throws InvalidFormatException
     *             Throws if the specified URI is not OPC compliant.
     */
    public static PackagePartName createPartName(URI partUri)
            throws InvalidFormatException {
        if (partUri == null)
            throw new IllegalArgumentException("partName");

        return new PackagePartName(partUri, true);
    }

    /**
     * Create an OPC compliant part name.
     *
     * @param partName
     *            The part name to validate.
     * @return The correspondent part name if valid, else <code>null</code>.
     * @throws InvalidFormatException
     *             Throws if the specified part name is not OPC compliant.
     * @see #createPartName(URI)
     */
    public static PackagePartName createPartName(String partName)
            throws InvalidFormatException {
        URI partNameURI;
        try {
            partNameURI = toURI(partName);
        } catch (URISyntaxException e) {
            throw new InvalidFormatException(e.getMessage());
        }
        return createPartName(partNameURI);
    }

    /**
     * Create an OPC compliant part name by resolving it using a base part.
     *
     * @param partName
     *            The part name to validate.
     * @param relativePart
     *            The relative base part.
     * @return The correspondent part name if valid, else <code>null</code>.
     * @throws InvalidFormatException
     *             Throws if the specified part name is not OPC compliant.
     * @see #createPartName(URI)
     */
    public static PackagePartName createPartName(String partName,
            PackagePart relativePart) throws InvalidFormatException {
        URI newPartNameURI;
        try {
            newPartNameURI = resolvePartUri(
                    relativePart.getPartName().getURI(), new URI(partName));
        } catch (URISyntaxException e) {
            throw new InvalidFormatException(e.getMessage());
        }
        return createPartName(newPartNameURI);
    }

    /**
     * Create an OPC compliant part name by resolving it using a base part.
     *
     * @param partName
     *            The part name URI to validate.
     * @param relativePart
     *            The relative base part.
     * @return The correspondent part name if valid, else <code>null</code>.
     * @throws InvalidFormatException
     *             Throws if the specified part name is not OPC compliant.
     * @see #createPartName(URI)
     */
    public static PackagePartName createPartName(URI partName,
            PackagePart relativePart) throws InvalidFormatException {
        URI newPartNameURI = resolvePartUri(
                relativePart.getPartName().getURI(), partName);
        return createPartName(newPartNameURI);
    }

    /**
     * Validate a part URI by returning a boolean.
     * ([M1.1],[M1.3],[M1.4],[M1.5],[M1.6])
     *
     * (OPC Specifications 8.1.1 Part names) :
     *
     * Part Name Syntax
     *
     * The part name grammar is defined as follows:
     *
     * <i>part_name = 1*( "/" segment )
     *
     * segment = 1*( pchar )</i>
     *
     *
     * (pchar is defined in RFC 3986)
     *
     * @param partUri
     *            The URI to validate.
     * @return <b>true</b> if the URI is valid to the OPC Specifications, else
     *         <b>false</b>
     *
     * @see #createPartName(URI)
     */
    public static boolean isValidPartName(URI partUri) {
        if (partUri == null)
            throw new IllegalArgumentException("partUri");

        try {
            createPartName(partUri);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Decode a URI by converting all percent encoded character into a String
     * character.
     *
     * @param uri
     *            The URI to decode.
     * @return The specified URI in a String with converted percent encoded
     *         characters.
     */
    public static String decodeURI(URI uri) {
        StringBuilder retVal = new StringBuilder(64);
        String uriStr = uri.toASCIIString();
        char c;
        final int length = uriStr.length();
        for (int i = 0; i < length; ++i) {
            c = uriStr.charAt(i);
            if (c == '%') {
                // We certainly found an encoded character, check for length
                // now ( '%' HEXDIGIT HEXDIGIT)
                if (((length - i) < 2)) {
                    throw new IllegalArgumentException("The uri " + uriStr
                            + " contain invalid encoded character !");
                }

                // Decode the encoded character
                char decodedChar = (char) Integer.parseInt(uriStr.substring(
                        i + 1, i + 3), 16);
                retVal.append(decodedChar);
                i += 2;
                continue;
            }
            retVal.append(c);
        }
        return retVal.toString();
    }

    /**
     * Build a part name where the relationship should be stored ((ex
     * /word/document.xml -&gt; /word/_rels/document.xml.rels)
     *
     * @param partName
     *            Source part URI
     * @return the full path (as URI) of the relation file
     * @throws InvalidOperationException
     *             Throws if the specified URI is a relationship part.
     */
    public static PackagePartName getRelationshipPartName(
            PackagePartName partName) {
        if (partName == null)
            throw new IllegalArgumentException("partName");

        if (PackagingURIHelper.PACKAGE_ROOT_URI.getPath().equals(
                partName.getURI().getPath()) )
            return PackagingURIHelper.PACKAGE_RELATIONSHIPS_ROOT_PART_NAME;

        if (partName.isRelationshipPartURI())
            throw new InvalidOperationException("Can't be a relationship part");

        String fullPath = partName.getURI().getPath();
        String filename = getFilename(partName.getURI());
        fullPath = fullPath.substring(0, fullPath.length() - filename.length());
        fullPath = combine(fullPath,
                PackagingURIHelper.RELATIONSHIP_PART_SEGMENT_NAME);
        fullPath = combine(fullPath, filename);
        fullPath = fullPath
                + PackagingURIHelper.RELATIONSHIP_PART_EXTENSION_NAME;

        PackagePartName retPartName;
        try {
            retPartName = createPartName(fullPath);
        } catch (InvalidFormatException e) {
            // Should never happen in production as all data are fixed but in
            // case of return null:
            return null;
        }
        return retPartName;
    }

    /**
     * Convert a string to {@link URI}
     *
     * If  part name is not a valid URI, it is resolved as follows:
     * <p>
     * 1. Percent-encode each open bracket ([) and close bracket (]).
     * 2. Percent-encode each percent (%) character that is not followed by a hexadecimal notation of an octet value.
     * 3. Un-percent-encode each percent-encoded unreserved character.
     * 4. Un-percent-encode each forward slash (/) and back slash (\).
     * 5. Convert all back slashes to forward slashes.
     * 6. If present in a segment containing non-dot (?.?) characters, remove trailing dot (?.?) characters from each segment.
     * 7. Replace each occurrence of multiple consecutive forward slashes (/) with a single forward slash.
     * 8. If a single trailing forward slash (/) is present, remove that trailing forward slash.
     * 9. Remove complete segments that consist of three or more dots.
     * 10. Resolve the relative reference against the base URI of the part holding the Unicode string, as it is defined
     * in ?5.2 of RFC 3986. The path component of the resulting absolute URI is the part name.
     *</p>
     *
     * @param   value   the string to be parsed into a URI
     * @return  the resolved part name that should be OK to construct a URI
     *
     * TODO YK: for now this method does only (5). Finish the rest.
     */
    public static URI toURI(String value) throws URISyntaxException  {
        //5. Convert all back slashes to forward slashes
        if (value.contains("\\")) {
             value = value.replace('\\', '/');
        }

        // URI fragments (those starting with '#') are not encoded
        // and may contain white spaces and raw unicode characters
        int fragmentIdx = value.indexOf('#');
        if(fragmentIdx != -1){
            String path = value.substring(0, fragmentIdx);
            String fragment = value.substring(fragmentIdx + 1);

            value = path + "#" + encode(fragment);
        }

        // trailing white spaces must be url-encoded, see Bugzilla 53282
        if(value.length() > 0 ){
            StringBuilder b = new StringBuilder();
            int idx = value.length() - 1;
            for(; idx >= 0; idx--){
                char c = value.charAt(idx);
                if(Character.isWhitespace(c) || c == '\u00A0') {
                    b.append(c);
                } else {
                    break;
                }
            }
            if(b.length() > 0){
                value = value.substring(0, idx+1) + encode(b.reverse().toString());
            }
        }

        // MS Office can insert URIs with missing authority, e.g. "http://" or "javascript://"
        // append a forward slash to avoid parse exception
        if(missingAuthPattern.matcher(value).matches()){
            value += "/";
        }
        return new URI(value);
    }

    /**
     * percent-encode white spaces and characters above 0x80.
     * <p>
     *   Examples:
     *   <pre>{@code
     *   'Apache POI' --> 'Apache%20POI'
     *   'Apache\u0410POI' --> 'Apache%04%10POI'
     *   }</pre>
     * @param s the string to encode
     * @return  the encoded string
     */
    public static String encode(String s) {
        int n = s.length();
        if (n == 0) return s;

        ByteBuffer bb  = ByteBuffer.wrap(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        while (bb.hasRemaining()) {
            int b = bb.get() & 0xff;
            if (isUnsafe(b)) {
                sb.append('%');
                sb.append(hexDigits[(b >> 4) & 0x0F]);
                sb.append(hexDigits[(b >> 0) & 0x0F]);
            } else {
                sb.append((char)b);
            }
        }
        return sb.toString();
    }

    private static final char[] hexDigits = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    private static boolean isUnsafe(int ch) {
        return ch >= 0x80 || ch == 0x7C || Character.isWhitespace(ch);
    }

}
