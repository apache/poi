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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipTypes;
import org.apache.poi.openxml4j.opc.ZipPackage;

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
	 */
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

		if (corePropsRel == null)
			return null;

		return new ZipEntry(corePropsRel.getTargetURI().getPath());
	}

	/**
	 * Retrieve the Zip entry of the content types part.
	 */
	public static ZipEntry getContentTypeZipEntry(ZipPackage pkg) {
		Enumeration entries = pkg.getZipArchive().getEntries();
		// Enumerate through the Zip entries until we find the one named
		// '[Content_Types].xml'.
		while (entries.hasMoreElements()) {
			ZipEntry entry = (ZipEntry) entries.nextElement();
			if (entry.getName().equals(
					ContentTypeManager.CONTENT_TYPES_PART_NAME))
				return entry;
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
		if (zipItemName == null)
			throw new IllegalArgumentException("zipItemName");
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
		if (opcItemName == null)
			throw new IllegalArgumentException("opcItemName");

		String retVal = opcItemName;
		while (retVal.startsWith(FORWARD_SLASH))
			retVal = retVal.substring(1);
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
		if (opcItemName == null)
			throw new IllegalArgumentException("opcItemName");

		String retVal = opcItemName;
		while (retVal.startsWith(FORWARD_SLASH))
			retVal = retVal.substring(1);
		try {
			return new URI(retVal);
		} catch (URISyntaxException e) {
			return null;
		}
	}

	/**
	 * Retrieve and open a zip file with the specified path.
	 *
	 * @param path
	 *            The file path.
	 * @return The zip archive freshly open.
	 */
	public static ZipFile openZipFile(String path) {
		File f = new File(path);
		try {
			if (!f.exists()) {
				return null;
			}
			return new ZipFile(f);
		} catch (IOException ioe) {
			return null;
		}
	}
}
