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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.StreamHelper;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;
import org.dom4j.Document;

/**
 * Zip implementation of the ContentTypeManager.
 *
 * @author Julien Chable
 * @version 1.0
 * @see ContentTypeManager
 */
public class ZipContentTypeManager extends ContentTypeManager {
    private static POILogger logger = POILogFactory.getLogger(ZipContentTypeManager.class);

	/**
	 * Delegate constructor to the super constructor.
	 *
	 * @param in
	 *            The input stream to parse to fill internal content type
	 *            collections.
	 * @throws InvalidFormatException
	 *             If the content types part content is not valid.
	 */
	public ZipContentTypeManager(InputStream in, OPCPackage pkg)
			throws InvalidFormatException {
		super(in, pkg);
	}

	@Override
	public boolean saveImpl(Document content, OutputStream out) {
		ZipOutputStream zos = null;
		if (out instanceof ZipOutputStream)
			zos = (ZipOutputStream) out;
		else
			zos = new ZipOutputStream(out);

		ZipEntry partEntry = new ZipEntry(CONTENT_TYPES_PART_NAME);
		try {
			// Referenced in ZIP
			zos.putNextEntry(partEntry);
			// Saving data in the ZIP file
			ByteArrayOutputStream outTemp = new ByteArrayOutputStream();
			StreamHelper.saveXmlInStream(content, out);
			InputStream ins = new ByteArrayInputStream(outTemp.toByteArray());
			byte[] buff = new byte[ZipHelper.READ_WRITE_FILE_BUFFER_SIZE];
			while (ins.available() > 0) {
				int resultRead = ins.read(buff);
				if (resultRead == -1) {
					// end of file reached
					break;
				}
				zos.write(buff, 0, resultRead);
			}
			zos.closeEntry();
		} catch (IOException ioe) {
			logger.log(POILogger.ERROR, "Cannot write: " + CONTENT_TYPES_PART_NAME
					+ " in Zip !", ioe);
			return false;
		}
		return true;
	}
}
