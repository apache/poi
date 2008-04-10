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
package org.apache.poi.extractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.Iterator;

import org.openxml4j.exceptions.InvalidFormatException;
import org.openxml4j.exceptions.OpenXML4JException;
import org.openxml4j.opc.Package;
import org.openxml4j.opc.PackagePart;
import org.openxml4j.opc.PackageRelationshipCollection;

import org.apache.poi.POIOLE2TextExtractor;
import org.apache.poi.POITextExtractor;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.hdgf.extractor.VisioTextExtractor;
import org.apache.poi.hslf.extractor.PowerPointExtractor;
import org.apache.poi.hssf.extractor.ExcelExtractor;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.poifs.filesystem.Entry;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xslf.XSLFSlideShow;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.XWPFDocument;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.xmlbeans.XmlException;

/**
 * Figures out the correct POITextExtractor for your supplied
 *  document, and returns it.
 */
public class ExtractorFactory {
	public static final String CORE_DOCUMENT_REL =
		"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument";
	
	public static POITextExtractor createExtractor(File f) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
		InputStream inp = new PushbackInputStream( 
			new FileInputStream(f), 8);
		
		if(POIFSFileSystem.hasPOIFSHeader(inp)) {
			return createExtractor(new POIFSFileSystem(inp));
		}
		if(POIXMLDocument.hasOOXMLHeader(inp)) {
			inp.close();
			return createExtractor(Package.open(f.toString()));
		}
		throw new IllegalArgumentException("Your File was neither an OLE2 file, nor an OOXML file");
	}
	
	public static POITextExtractor createExtractor(InputStream inp) throws IOException, InvalidFormatException, OpenXML4JException, XmlException {
		// Figure out the kind of stream
		// If clearly doesn't do mark/reset, wrap up
		if(! inp.markSupported()) {
			inp = new PushbackInputStream(inp, 8);
		}
		
		if(POIFSFileSystem.hasPOIFSHeader(inp)) {
			return createExtractor(new POIFSFileSystem(inp));
		}
		if(POIXMLDocument.hasOOXMLHeader(inp)) {
			return createExtractor(Package.open(inp));
		}
		throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
	}
	
	public static POIXMLTextExtractor createExtractor(Package pkg) throws IOException, OpenXML4JException, XmlException {
		PackageRelationshipCollection core = 
			pkg.getRelationshipsByType(CORE_DOCUMENT_REL);
		if(core.size() != 1) {
			throw new IllegalArgumentException("Invalid OOXML Package received - expected 1 core document, found " + core.size());
		}
		
		PackagePart corePart = pkg.getPart(core.getRelationship(0));
		if(corePart.getContentType().equals(XSSFWorkbook.WORKBOOK.getContentType())) {
			return new XSSFExcelExtractor(pkg);
		}
		if(corePart.getContentType().equals(XWPFDocument.MAIN_CONTENT_TYPE)) {
			return new XWPFWordExtractor(pkg);
		}
		if(corePart.getContentType().equals(XSLFSlideShow.MAIN_CONTENT_TYPE)) {
			return new XSLFPowerPointExtractor(pkg);
		}
		throw new IllegalArgumentException("No supported documents found in the OOXML package");
	}
	
	public static POIOLE2TextExtractor createExtractor(POIFSFileSystem fs) throws IOException {
		// Look for certain entries in the stream, to figure it
		//  out from
		for(Iterator entries = fs.getRoot().getEntries(); entries.hasNext(); ) {
			Entry entry = (Entry)entries.next();
			
			if(entry.getName().equals("Workbook")) {
				return new ExcelExtractor(fs);
			}
			if(entry.getName().equals("WordDocument")) {
				return new WordExtractor(fs);
			}
			if(entry.getName().equals("PowerPoint Document")) {
				return new PowerPointExtractor(fs);
			}
			if(entry.getName().equals("VisioDocument")) {
				return new VisioTextExtractor(fs);
			}
		}
		throw new IllegalArgumentException("No supported documents found in the OLE2 stream");
	}
}
