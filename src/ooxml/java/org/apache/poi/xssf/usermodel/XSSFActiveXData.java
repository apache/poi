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

package org.apache.poi.xssf.usermodel;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.PictureData;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.model.XSSFWritableModel;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * 
 * @author Nick Burch
 */
public final class XSSFActiveXData implements PictureData, XSSFWritableModel {

	private final PackagePart _packagePart;
	private final String _originalId;

	public XSSFActiveXData(PackagePart packagePart, String originalId) {
		_packagePart = packagePart;
		_originalId = originalId;
	}

	public XSSFActiveXData(PackagePart packagePart) {
		this(packagePart, null);
	}

	public String getOriginalId() {
		return _originalId;
	}

	public PackagePart getPart() {
		return _packagePart;
	}

	public void writeTo(OutputStream out) throws IOException {
		IOUtils.copy(_packagePart.getInputStream(), out);
	}

	public byte[] getData() {
		// TODO - is this right?
		// Are there headers etc?
		try {
			return IOUtils.toByteArray(_packagePart.getInputStream());
		} catch(IOException e) {
			throw new POIXMLException(e);
		}
	}

	public String suggestFileExtension() {
		return _packagePart.getPartName().getExtension();
	}
}
