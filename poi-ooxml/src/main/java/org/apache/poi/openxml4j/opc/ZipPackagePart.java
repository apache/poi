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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.InvalidOperationException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.internal.ContentType;
import org.apache.poi.openxml4j.opc.internal.marshallers.ZipPartMarshaller;
import org.apache.poi.util.NotImplemented;

/**
 * Zip implementation of a PackagePart.
 *
 * @author Julien Chable
 * @version 1.0
 * @see PackagePart
 */
public class ZipPackagePart extends PackagePart {

	/**
	 * The zip entry corresponding to this part.
	 */
	private ZipArchiveEntry zipEntry;

	/**
	 * Constructor.
	 *
	 * @param container
	 *            The container package.
	 * @param zipEntry
	 *            The zip entry corresponding to this part.
	 * @param partName
	 *            The part name.
	 * @param contentType
	 *            Content type.
	 * @throws InvalidFormatException
	 *             Throws if the content of this part is invalid.
	 */
	public ZipPackagePart(OPCPackage container, ZipArchiveEntry zipEntry,
			PackagePartName partName, String contentType)
			throws InvalidFormatException {
		this(container, zipEntry, partName, contentType, true);
	}

	/**
	 * Constructor.
	 *
	 * @param container
	 *            The container package.
	 * @param zipEntry
	 *            The zip entry corresponding to this part.
	 * @param partName
	 *            The part name.
	 * @param contentType
	 *            Content type.
	 * @throws InvalidFormatException
	 *             Throws if the content of this part is invalid.
	 */
	/* package */ ZipPackagePart(OPCPackage container, ZipArchiveEntry zipEntry,
						  PackagePartName partName, String contentType, boolean loadRelationships)
			throws InvalidFormatException {
		super(container, partName, new ContentType(contentType), loadRelationships);
		this.zipEntry = zipEntry;
	}

	/**
	 * Get the zip entry of this part.
	 *
	 * @return The zip entry in the zip structure coresponding to this part.
	 */
	public ZipArchiveEntry getZipArchive() {
		return zipEntry;
	}

	/**
	 * Implementation of the getInputStream() which return the inputStream of
	 * this part zip entry.
	 *
	 * @return Input stream of this part zip entry.
	 */
	@Override
	protected InputStream getInputStreamImpl() throws IOException {
		// We use the getInputStream() method from java.util.zip.ZipFile
		// class which return an InputStream to this part zip entry.
		return ((ZipPackage) _container).getZipArchive()
				.getInputStream(zipEntry);
	}

	/**
	 * Implementation of the getOutputStream(). Return <b>null</b>. Normally
	 * will never be called since the MemoryPackage is use instead.
	 *
	 * @return <b>null</b>
	 */
	@Override
	protected OutputStream getOutputStreamImpl() {
		return null;
	}

	@Override
	public long getSize() {
		return zipEntry.getSize();
	}

	@Override
	public boolean save(OutputStream os) throws OpenXML4JException {
		return new ZipPartMarshaller().marshall(this, os);
	}

	@Override
	@NotImplemented
	public boolean load(InputStream ios) {
		throw new InvalidOperationException("Method not implemented !");
	}

	@Override
	@NotImplemented
	public void close() {
		throw new InvalidOperationException("Method not implemented !");
	}

	@Override
	@NotImplemented
	public void flush() {
		throw new InvalidOperationException("Method not implemented !");
	}
}
