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
package org.apache.poi.ss.usermodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Factory for creating the appropriate kind of Workbook
 *  (be it HSSFWorkbook or XSSFWorkbook), from the given input
 */
public class WorkbookFactory {
	/**
	 * Creates an HSSFWorkbook from the given POIFSFileSystem
	 */
	public static Workbook create(POIFSFileSystem fs) throws IOException {
		return new HSSFWorkbook(fs);
	}
	/**
	 * Creates an XSSFWorkbook from the given OOXML Package
	 */
	public static Workbook create(OPCPackage pkg) throws IOException {
		return new XSSFWorkbook(pkg);
	}
	/**
	 * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
	 *  the given InputStream.
	 * Your input stream MUST either support mark/reset, or
	 *  be wrapped as a {@link PushbackInputStream}!
	 */
	public static Workbook create(InputStream inp) throws IOException, InvalidFormatException {
		// If clearly doesn't do mark/reset, wrap up
		if(! inp.markSupported()) {
			inp = new PushbackInputStream(inp, 8);
		}
		
		if(POIFSFileSystem.hasPOIFSHeader(inp)) {
			return new HSSFWorkbook(inp);
		}
		if(POIXMLDocument.hasOOXMLHeader(inp)) {
			return new XSSFWorkbook(OPCPackage.open(inp));
		}
		throw new IllegalArgumentException("Your InputStream was neither an OLE2 stream, nor an OOXML stream");
	}
   /**
    * Creates the appropriate HSSFWorkbook / XSSFWorkbook from
    *  the given File, which must exist and be readable.
    */
	public static Workbook create(File file) throws IOException, InvalidFormatException {
	   if(! file.exists()) {
	      throw new FileNotFoundException(file.toString());
	   }
	   
	   try {
	      NPOIFSFileSystem fs = new NPOIFSFileSystem(file);
	      return new HSSFWorkbook(fs.getRoot(), true);
	   } catch(OfficeXmlFileException e) {
	      OPCPackage pkg = OPCPackage.openOrCreate(file);
	      return new XSSFWorkbook(pkg);
	   }
	}
}
