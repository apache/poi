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
package org.apache.poi;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackageAccess;
import org.openxml4j.opc.PackagePart;

/**
 * Parent class of the low level interface to  
 *  all POI XML (OOXML) implementations.
 * Normal users should probably deal with things that
 *  extends {@link POIXMLDocument}, unless they really
 *  do need to get low level access to the files.
 *  
 * WARNING - APIs expected to change rapidly
 */
public abstract class HXFDocument {
	/**
	 * File package/container.
	 */
	protected Package container;
	/**
	 * The Package Part for our base document
	 */
	protected PackagePart basePart;
	/**
	 * The base document of this instance, eg Workbook for
	 *  xslsx
	 */
	protected Document baseDocument;
	
	protected HXFDocument(Package container, String baseContentType) throws OpenXML4JException {
		this.container = container;
		
		// Find the base document
		ArrayList<PackagePart> baseParts =
			container.getPartsByContentType(baseContentType);
		if(baseParts.size() != 1) {
			throw new OpenXML4JException("Expecting one entry with content type of " + baseContentType + ", but found " + baseParts.size());
		}
		basePart = baseParts.get(0);
		
		// And load it up
		try {
			SAXReader reader = new SAXReader();
			baseDocument = reader.read(basePart.getInputStream());
		} catch (DocumentException e) {
			throw new OpenXML4JException(e.getMessage());
		} catch (IOException ioe) {
			throw new OpenXML4JException(ioe.getMessage());
		}
	}
	
	public static Package openPackage(File f) throws InvalidFormatException {
		return Package.open(f.toString(), PackageAccess.READ_WRITE);
	}

	/**
	 * Get the package container.
	 * @return The package associated to this document.
	 */
	public Package getPackage() {
		return container;
	}
}
