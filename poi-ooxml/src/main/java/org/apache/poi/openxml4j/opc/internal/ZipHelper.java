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
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.poi.openxml4j.exceptions.NotOfficeXmlFileException;
import org.apache.poi.openxml4j.exceptions.OLE2NotOfficeXmlFileException;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.ZipPackage;
import org.apache.poi.openxml4j.util.ZipArchiveThresholdInputStream;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.util.Internal;

@Internal
public final class ZipHelper {
    /**
     * Forward slash use to convert part name between OPC and zip item naming
     * conventions.
     */
    private static final String FORWARD_SLASH = "/";

    /**
     * Prevent this class to be instancied.
     */
    private ZipHelper() {
        // Do nothing
    }

    /**
     * Retrieve the zip entry of the core properties part.
     *
     * @throws IllegalArgumentException If the relationship for
     *      core properties cannot be read or an invalid name is
     *      specified in the properties.
     */
    public static ZipArchiveEntry getCorePropertiesZipEntry(ZipPackage pkg) {
        PackageRelationship corePropsRel = pkg.getRelationshipsByType(
                PackageRelationshipTypes.CORE_PROPERTIES).getRelationship(0);

        if (corePropsRel == null) {
            return null;
        }

        return new ZipArchiveEntry(corePropsRel.getTargetURI().getPath());
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
    private static void verifyZipHeader(InputStream stream) throws NotOfficeXmlFileException, IOException {
        InputStream is = FileMagic.prepareToCheckMagic(stream);
        FileMagic fm = FileMagic.valueOf(is);

        switch (fm) {
        case OLE2:
            throw new OLE2NotOfficeXmlFileException(
                "The supplied data appears to be in the OLE2 Format. " +
                "You are calling the part of POI that deals with OOXML "+
                "(Office Open XML) Documents. You need to call a different " +
                "part of POI to process this data (eg HSSF instead of XSSF)");
        case XML:
            throw new NotOfficeXmlFileException(
                "The supplied data appears to be a raw XML file. " +
                "Formats such as Office 2003 XML are not supported");
        default:
            // Don't check for a Zip header, as to maintain backwards
            //  compatibility we need to let them seek over junk at the
            //  start before beginning processing.
            break;
        }
    }

    /**
     * Opens the specified stream as a secure zip
     *
     * @param stream
     *            The stream to open.
     * @return The zip stream freshly open.
     */
    @SuppressWarnings("resource")
    public static ZipArchiveThresholdInputStream openZipStream(InputStream stream) throws IOException {
        // Peek at the first few bytes to sanity check
        InputStream checkedStream = FileMagic.prepareToCheckMagic(stream);
        verifyZipHeader(checkedStream);
        
        // Open as a proper zip stream
        return new ZipArchiveThresholdInputStream(new ZipArchiveInputStream(checkedStream));
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
    public static ZipSecureFile openZipFile(File file) throws IOException, NotOfficeXmlFileException {
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist");
        }
        if (file.isDirectory()) {
            throw new IOException("File is a directory");
        }
        
        // Peek at the first few bytes to sanity check
        try (FileInputStream input = new FileInputStream(file)) {
            verifyZipHeader(input);
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
    public static ZipSecureFile openZipFile(String path) throws IOException {
        return openZipFile(new File(path));
    }
}
