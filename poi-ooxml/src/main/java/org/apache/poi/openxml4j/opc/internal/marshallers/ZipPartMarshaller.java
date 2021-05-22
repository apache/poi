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

package org.apache.poi.openxml4j.opc.internal.marshallers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackageNamespaces;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.StreamHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.openxml4j.opc.internal.PartMarshaller;
import org.apache.poi.openxml4j.opc.internal.ZipHelper;
import org.apache.poi.ooxml.util.DocumentHelper;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Zip part marshaller. This marshaller is use to save any part in a zip stream.
 */
public final class ZipPartMarshaller implements PartMarshaller {
    private static final Logger LOG = LogManager.getLogger(ZipPartMarshaller.class);

    /**
     * Save the specified part to the given stream.
     *
     * @param part The {@link PackagePart} to save
     * @param os The stream to write the data to
     * @return true if saving was successful or there was nothing to save,
     *      false if an error occurred.
     *      In case of errors, logging via Log4j 2 is used to provide more information.
     * @throws OpenXML4JException
     *      Throws if the stream cannot be written to or an internal exception is thrown.
     */
    @Override
    public boolean marshall(PackagePart part, OutputStream os)
            throws OpenXML4JException {
        if (!(os instanceof ZipArchiveOutputStream)) {
            LOG.atError().log("Unexpected class {}", os.getClass().getName());
            throw new OpenXML4JException("ZipOutputStream expected !");
            // Normally should happen only in development phase, so just throw
            // exception
        }

        // check if there is anything to save for some parts. We don't do this for all parts as some code
        // might depend on empty parts being saved, e.g. some unit tests verify this currently.
        if(part.getSize() == 0 && part.getPartName().getName().equals(XSSFRelation.SHARED_STRINGS.getDefaultFileName())) {
            return true;
        }

        ZipArchiveOutputStream zos = (ZipArchiveOutputStream) os;
        ZipArchiveEntry partEntry = new ZipArchiveEntry(ZipHelper
                .getZipItemNameFromOPCName(part.getPartName().getURI()
                        .getPath()));
        try {
            // Create next zip entry
            zos.putArchiveEntry(partEntry);

            // Saving data in the ZIP file
            try (final InputStream ins = part.getInputStream()) {
                IOUtils.copy(ins, zos);
            } finally {
                zos.closeArchiveEntry();
            }
        } catch (IOException ioe) {
            LOG.atError().withThrowable(ioe).log("Cannot write: {}: in ZIP", part.getPartName());
            return false;
        }

        // Saving relationship part
        if (part.hasRelationships()) {
            PackagePartName relationshipPartName = PackagingURIHelper
                    .getRelationshipPartName(part.getPartName());

            marshallRelationshipPart(part.getRelationships(),
                    relationshipPartName, zos);
        }

        return true;
    }

    /**
     * Save relationships into the part.
     *
     * @param rels
     *            The relationships collection to marshall.
     * @param relPartName
     *            Part name of the relationship part to marshall.
     * @param zos
     *            Zip output stream in which to save the XML content of the
     *            relationships serialization.
     * @return true if saving was successful,
     *      false if an error occurred.
     *      In case of errors, logging via Log4j 2 is used to provide more information.
     */
    public static boolean marshallRelationshipPart(
            PackageRelationshipCollection rels, PackagePartName relPartName,
            ZipArchiveOutputStream zos) {
        // Building xml
        Document xmlOutDoc = DocumentHelper.createDocument();
        // make something like <Relationships
        // xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
        Element root = xmlOutDoc.createElementNS(PackageNamespaces.RELATIONSHIPS, PackageRelationship.RELATIONSHIPS_TAG_NAME);
        xmlOutDoc.appendChild(root);

        // <Relationship
        // TargetMode="External"
        // Id="rIdx"
        // Target="http://www.custom.com/images/pic1.jpg"
        // Type="http://www.custom.com/external-resource"/>

        URI sourcePartURI = PackagingURIHelper
                .getSourcePartUriFromRelationshipPartUri(relPartName.getURI());

        for (PackageRelationship rel : rels) {
            // the relationship element
            Element relElem = xmlOutDoc.createElementNS(PackageNamespaces.RELATIONSHIPS, PackageRelationship.RELATIONSHIP_TAG_NAME);
            root.appendChild(relElem);

            // the relationship ID
            relElem.setAttribute(PackageRelationship.ID_ATTRIBUTE_NAME, rel.getId());

            // the relationship Type
            relElem.setAttribute(PackageRelationship.TYPE_ATTRIBUTE_NAME, rel.getRelationshipType());

            // the relationship Target
            String targetValue;
            URI uri = rel.getTargetURI();
            if (rel.getTargetMode() == TargetMode.EXTERNAL) {
                // Save the target as-is - we don't need to validate it,
                //  alter it etc
                targetValue = uri.toString();

                // add TargetMode attribute (as it is external link external)
                relElem.setAttribute(PackageRelationship.TARGET_MODE_ATTRIBUTE_NAME, "External");
            } else {
                URI targetURI = rel.getTargetURI();
                targetValue = PackagingURIHelper.relativizeURI(
                        sourcePartURI, targetURI, true).toString();
            }
            relElem.setAttribute(PackageRelationship.TARGET_ATTRIBUTE_NAME, targetValue);
        }

        xmlOutDoc.normalize();

        // String schemaFilename = Configuration.getPathForXmlSchema()+
        // File.separator + "opc-relationships.xsd";

        // Save part in zip
        ZipArchiveEntry ctEntry = new ZipArchiveEntry(ZipHelper.getZipURIFromOPCName(
                relPartName.getURI().toASCIIString()).getPath());
        try {
            zos.putArchiveEntry(ctEntry);
            try {
                return StreamHelper.saveXmlInStream(xmlOutDoc, zos);
            } finally {
                zos.closeArchiveEntry();
            }
        } catch (IOException e) {
            LOG.atError().withThrowable(e).log("Cannot create zip entry {}", relPartName);
            return false;
        }
    }
}
