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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.ZipPackage;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.openxml4j.util.ZipSecureFile.ThresholdInputStream;
import org.apache.poi.poifs.common.POIFSConstants;
import org.apache.poi.poifs.storage.HeaderBlockConstants;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.Removal;

public final class ZipHelper {
    /**
     * Forward slash use to convert part name between OPC and zip item naming
     * conventions.
     */
    private final static String FORWARD_SLASH = "/";

    /**
     * Buffer to read data from file. Use big buffer to improve performaces. the
     * InputStream class is reading only 8192 bytes per read call (default value
     * set by sun)
     * 
     * @deprecated in POI 3.16-beta3, not used anymore
     */
    @Deprecated
    @Removal(version="3.18")
    public static final int READ_WRITE_FILE_BUFFER_SIZE = 8192;

    /**
     * Prevent this class to be instancied.
     */
    private ZipHelper() {
        // Do nothing
    }

    /**
     * Retrieve the zip entry of the core properties part.
     *
     * @throws OpenXML4JException
     *             Throws if internal error occurs.
     */
    public static ZipEntry getCorePropertiesZipEntry(ZipPackage pkg) {
        PackageRelationship corePropsRel = pkg.getRelationshipsByType(
                PackageRelationshipTypes.CORE_PROPERTIES).getRelationship(0);

        if (corePropsRel == null) {
            return null;
        }

        return new ZipEntry(corePropsRel.getTargetURI().getPath());
    }

    /**
     * Retrieve the Zip entry of the content types part.
     */
    public static ZipEntry getContentTypeZipEntry(ZipPackage pkg) {
        Enumeration<? extends ZipEntry> entries = pkg.getZipArchive().getEntries();

        // Enumerate through the Zip entries until we find the one named
        // '[Content_Types].xml'.
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (entry.getName().equals(
                    ContentTypeManager.CONTENT_TYPES_PART_NAME)) {
                return entry;
            }
        }
        return null;
    }

    /**
     * Convert a zip name into an OPC name by adding a leading forward slash to
     * the specified item name.
     *
     * @param zipItemName
     *            Zip item name to convert.
     * @return An OPC compliant name.
     */
    public static String getOPCNameFromZipItemName(String zipItemName) {
        if (zipItemName == null) {
            throw new IllegalArgumentException("zipItemName cannot be null");
        }
        if (zipItemName.startsWith(FORWARD_SLASH)) {
            return zipItemName;
        }
        return FORWARD_SLASH + zipItemName;
    }

    /**
     * Convert an OPC item name into a zip item name by removing any leading
     * forward slash if it exist.
     *
     * @param opcItemName
     *            The OPC item name to convert.
     * @return A zip item name without any leading slashes.
     */
    public static String getZipItemNameFromOPCName(String opcItemName) {
        if (opcItemName == null) {
            throw new IllegalArgumentException("opcItemName cannot be null");
        }

        String retVal = opcItemName;
        while (retVal.startsWith(FORWARD_SLASH)) {
            retVal = retVal.substring(1);
        }
        return retVal;
    }

    /**
     * Convert an OPC item name into a zip URI by removing any leading forward
     * slash if it exist.
     *
     * @param opcItemName
     *            The OPC item name to convert.
     * @return A zip URI without any leading slashes.
     */
    public static URI getZipURIFromOPCName(String opcItemName) {
        if (opcItemName == null) {
            throw new IllegalArgumentException("opcItemName");
        }

        String retVal = opcItemName;
        while (retVal.startsWith(FORWARD_SLASH)) {
            retVal = retVal.substring(1);
        }
        try {
            return new URI(retVal);
        } catch (URISyntaxException e) {
            return null;
        }
    }
    
    /**
     * Verifies that the given stream starts with a Zip structure.
     * 
     * Warning - this will consume the first few bytes of the stream,
     *  you should push-back or reset the stream after use!
     */
    public static void verifyZipHeader(InputStream stream) 
            throws NotOfficeXmlFileException, IOException {
        // Grab the first 8 bytes
        byte[] data = new byte[8];
        IOUtils.readFully(stream, data);
        
        // OLE2?
        long signature = LittleEndian.getLong(data);
        if (signature == HeaderBlockConstants._signature) {
            throw new OLE2NotOfficeXmlFileException(
                "The supplied data appears to be in the OLE2 Format. " +
                "You are calling the part of POI that deals with OOXML "+
                "(Office Open XML) Documents. You need to call a different " +
                "part of POI to process this data (eg HSSF instead of XSSF)");
        }
        
        // Raw XML?
        byte[] RAW_XML_FILE_HEADER = POIFSConstants.RAW_XML_FILE_HEADER;
        if (data[0] == RAW_XML_FILE_HEADER[0] &&
            data[1] == RAW_XML_FILE_HEADER[1] &&
            data[2] == RAW_XML_FILE_HEADER[2] &&
            data[3] == RAW_XML_FILE_HEADER[3] &&
            data[4] == RAW_XML_FILE_HEADER[4]) {
            throw new NotOfficeXmlFileException(
                "The supplied data appears to be a raw XML file. " +
                "Formats such as Office 2003 XML are not supported");
        }

        // Don't check for a Zip header, as to maintain backwards
        //  compatibility we need to let them seek over junk at the
        //  start before beginning processing.
        
        // Put things back
        if (stream instanceof PushbackInputStream) {
            ((PushbackInputStream)stream).unread(data);
        } else if (stream.markSupported()) {
            stream.reset();
        } else if (stream instanceof FileInputStream) {
            // File open check, about to be closed, nothing to do
        } else {
            // Oh dear... I hope you know what you're doing!
        }
    }
    
    private static InputStream prepareToCheckHeader(InputStream stream) {
        if (stream instanceof PushbackInputStream) {
            return stream;
        }
        if (stream.markSupported()) {
            stream.mark(8);
            return stream;
        }
        return new PushbackInputStream(stream, 8);
    }

    /**
     * Opens the specified stream as a secure zip
     *
     * @param stream
     *            The stream to open.
     * @return The zip stream freshly open.
     */
    @SuppressWarnings("resource")
    public static ThresholdInputStream openZipStream(InputStream stream) throws IOException {
        // Peek at the first few bytes to sanity check
        InputStream checkedStream = prepareToCheckHeader(stream);
        verifyZipHeader(checkedStream);
        
        // Open as a proper zip stream
        InputStream zis = new ZipInputStream(checkedStream);
        return ZipSecureFile.addThreshold(zis);
    }

    /**
     * Opens the specified file as a secure zip, or returns null if no 
     *  such file exists
     *
     * @param file
     *            The file to open.
     * @return The zip archive freshly open.
     * @throws IOException if the zip file cannot be opened or closed to read the header signature
     * @throws NotOfficeXmlFileException if stream does not start with zip header signature
     */
    public static ZipFile openZipFile(File file) throws IOException, NotOfficeXmlFileException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist");
        }
        if (file.isDirectory()) {
            throw new IOException("File is a directory");
        }
        
        // Peek at the first few bytes to sanity check
        FileInputStream input = new FileInputStream(file);
        try {
            verifyZipHeader(input);
        } finally {
            input.close();
        }

        // Open as a proper zip file
        return new ZipSecureFile(file);
    }

    /**
     * Retrieve and open as a secure zip file with the specified path.
     *
     * @param path
     *            The file path.
     * @return The zip archive freshly open.
     */
    public static ZipFile openZipFile(String path) throws IOException {
        return openZipFile(new File(path));
    }
}
