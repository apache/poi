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
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.internal.marshallers.ZipPartMarshaller;

/**
 * Memory version of a package part. Use to
 * 
 * @author Julien Chable
 * @version 1.0
 */
public final class MemoryPackagePart extends PackagePart {

	/**
	 * Storage for the part data.
	 */
	protected byte[] data;

	/**
	 * Size of data.
	 */
	protected int length;

	/**
	 * Constructor.
	 * 
	 * @param pack
	 *            The owner package.
	 * @param partName
	 *            The part name.
	 * @param contentType
	 *            The content type.
	 * @throws InvalidFormatException
	 *             If the specified URI is not OPC compliant.
	 */
	public MemoryPackagePart(OPCPackage pack, PackagePartName partName,
			String contentType) throws InvalidFormatException {
		super(pack, partName, contentType);
	}

	/**
	 * Constructor.
	 * 
	 * @param pack
	 *            The owner package.
	 * @param partName
	 *            The part name.
	 * @param contentType
	 *            The content type.
	 * @param loadRelationships
	 *            Specify if the relationships will be loaded.
	 * @throws InvalidFormatException
	 *             If the specified URI is not OPC compliant.
	 */
	public MemoryPackagePart(OPCPackage pack, PackagePartName partName,
			String contentType, boolean loadRelationships)
			throws InvalidFormatException {
		super(pack, partName, new ContentType(contentType), loadRelationships);
	}

	@Override
	protected InputStream getInputStreamImpl() {
		// If this part has been created from scratch and/or the data buffer is
		// not
		// initialize, so we do it now.
		if (data == null) {
			data = new byte[0];
		}
		return new ByteArrayInputStream(data);
	}

	@Override
	protected OutputStream getOutputStreamImpl() {
		return new MemoryPackagePartOutputStream(this);
	}

	public void clear() {
		data = null;
		length = 0;
	}

	@Override
	public boolean save(OutputStream os) throws OpenXML4JException {
		return new ZipPartMarshaller().marshall(this, os);
	}

	@Override
	public boolean load(InputStream ios) throws InvalidFormatException {
		throw new InvalidFormatException("Method not implemented");
	}

	@Override
	public void close() {
		// Do nothing
	}

	@Override
	public void flush() {
		// Do nothing
	}
}
