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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
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
import org.apache.poi.util.POILogger;
import org.apache.poi.util.POILogFactory;

/**
 * Zip part marshaller. This marshaller is use to save any part in a zip stream.
 *
 * @author Julien Chable
 */
public final class ZipPartMarshaller implements PartMarshaller {
	private static POILogger logger = POILogFactory.getLogger(ZipPartMarshaller.class);

	/**
	 * Save the specified part.
	 *
	 * @throws OpenXML4JException
	 *             Throws if an internal exception is thrown.
	 */
	public boolean marshall(PackagePart part, OutputStream os)
			throws OpenXML4JException {
		if (!(os instanceof ZipOutputStream)) {
			logger.log(POILogger.ERROR,"Unexpected class " + os.getClass().getName());
			throw new OpenXML4JException("ZipOutputStream expected !");
			// Normally should happen only in developement phase, so just throw
			// exception
		}

		ZipOutputStream zos = (ZipOutputStream) os;
		ZipEntry partEntry = new ZipEntry(ZipHelper
				.getZipItemNameFromOPCName(part.getPartName().getURI()
						.getPath()));
		try {
			// Create next zip entry
			zos.putNextEntry(partEntry);

			// Saving data in the ZIP file
			InputStream ins = part.getInputStream();
			byte[] buff = new byte[ZipHelper.READ_WRITE_FILE_BUFFER_SIZE];
			while (ins.available() > 0) {
				int resultRead = ins.read(buff);
				if (resultRead == -1) {
					// End of file reached
					break;
				}
				zos.write(buff, 0, resultRead);
			}
			zos.closeEntry();
		} catch (IOException ioe) {
			logger.log(POILogger.ERROR,"Cannot write: " + part.getPartName() + ": in ZIP",
					ioe);
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
	 */
	public static boolean marshallRelationshipPart(
			PackageRelationshipCollection rels, PackagePartName relPartName,
			ZipOutputStream zos) {
		// Building xml
		Document xmlOutDoc = DocumentHelper.createDocument();
		// make something like <Relationships
		// xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
		Namespace dfNs = Namespace.get("", PackageNamespaces.RELATIONSHIPS);
		Element root = xmlOutDoc.addElement(new QName(
				PackageRelationship.RELATIONSHIPS_TAG_NAME, dfNs));

		// <Relationship
		// TargetMode="External"
		// Id="rIdx"
		// Target="http://www.custom.com/images/pic1.jpg"
		// Type="http://www.custom.com/external-resource"/>

		URI sourcePartURI = PackagingURIHelper
				.getSourcePartUriFromRelationshipPartUri(relPartName.getURI());

		for (PackageRelationship rel : rels) {
			// the relationship element
			Element relElem = root
					.addElement(PackageRelationship.RELATIONSHIP_TAG_NAME);

			// the relationship ID
			relElem.addAttribute(PackageRelationship.ID_ATTRIBUTE_NAME, rel
					.getId());

			// the relationship Type
			relElem.addAttribute(PackageRelationship.TYPE_ATTRIBUTE_NAME, rel
					.getRelationshipType());

			// the relationship Target
			String targetValue;
			URI uri = rel.getTargetURI();
			if (rel.getTargetMode() == TargetMode.EXTERNAL) {
				// Save the target as-is - we don't need to validate it,
				//  alter it etc
				targetValue = uri.toString();

				// add TargetMode attribute (as it is external link external)
				relElem.addAttribute(
						PackageRelationship.TARGET_MODE_ATTRIBUTE_NAME,
						"External");
			} else {
                URI targetURI = rel.getTargetURI();
                targetValue = PackagingURIHelper.relativizeURI(
						sourcePartURI, targetURI, true).getPath();
                if (targetURI.getRawFragment() != null) {
                    targetValue += "#" + targetURI.getRawFragment();
                }
			}
			relElem.addAttribute(PackageRelationship.TARGET_ATTRIBUTE_NAME,
					targetValue);
		}

		xmlOutDoc.normalize();

		// String schemaFilename = Configuration.getPathForXmlSchema()+
		// File.separator + "opc-relationships.xsd";

		// Save part in zip
		ZipEntry ctEntry = new ZipEntry(ZipHelper.getZipURIFromOPCName(
				relPartName.getURI().toASCIIString()).getPath());
		try {
			zos.putNextEntry(ctEntry);
			if (!StreamHelper.saveXmlInStream(xmlOutDoc, zos)) {
				return false;
			}
			zos.closeEntry();
		} catch (IOException e) {
			logger.log(POILogger.ERROR,"Cannot create zip entry " + relPartName, e);
			return false;
		}
		return true; // success
	}
}
