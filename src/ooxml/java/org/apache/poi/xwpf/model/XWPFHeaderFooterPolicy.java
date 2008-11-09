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
package org.apache.poi.xwpf.model;

import java.io.IOException;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.xmlbeans.XmlException;
import org.openxml4j.opc.PackagePart;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtrRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FtrDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.HdrDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;

/**
 * A .docx file can have no headers/footers, the same header/footer
 *  on each page, odd/even page footers, and optionally also 
 *  a different header/footer on the first page.
 * This class handles sorting out what there is, and giving you
 *  the right headers and footers for the document.
 */
public class XWPFHeaderFooterPolicy {
	private XWPFHeader firstPageHeader;
	private XWPFFooter firstPageFooter;
	
	private XWPFHeader evenPageHeader;
	private XWPFFooter evenPageFooter;
	
	private XWPFHeader defaultHeader;
	private XWPFFooter defaultFooter;
	
	
	/**
	 * Figures out the policy for the given document,
	 *  and creates any header and footer objects
	 *  as required.
	 */
	public XWPFHeaderFooterPolicy(XWPFDocument doc) throws IOException, XmlException {
		// Grab what headers and footers have been defined
		// For now, we don't care about different ranges, as it
		//  doesn't seem that .docx properly supports that
		//  feature of the file format yet
		CTSectPr sectPr = doc.getDocument().getBody().getSectPr();
		for(int i=0; i<sectPr.sizeOfHeaderReferenceArray(); i++) {
			// Get the header
			CTHdrFtrRef ref = sectPr.getHeaderReferenceArray(i);
			PackagePart hdrPart = doc.getPartById(ref.getId());
			XWPFHeader hdr = new XWPFHeader(
					HdrDocument.Factory.parse(hdrPart.getInputStream()).getHdr()
			);

			// Assign it
			if(ref.getType() == STHdrFtr.FIRST) {
				firstPageHeader = hdr;
			} else if(ref.getType() == STHdrFtr.EVEN) {
				evenPageHeader = hdr;
			} else {
				defaultHeader = hdr;
			}
		}
		for(int i=0; i<sectPr.sizeOfFooterReferenceArray(); i++) {
			// Get the footer
			CTHdrFtrRef ref = sectPr.getFooterReferenceArray(i);
			PackagePart ftrPart = doc.getPartById(ref.getId());
			XWPFFooter ftr = new XWPFFooter(
					FtrDocument.Factory.parse(ftrPart.getInputStream()).getFtr()
			);

			// Assign it
			if(ref.getType() == STHdrFtr.FIRST) {
				firstPageFooter = ftr;
			} else if(ref.getType() == STHdrFtr.EVEN) {
				evenPageFooter = ftr;
			} else {
				defaultFooter = ftr;
			}
		}
	}

	
	public XWPFHeader getFirstPageHeader() {
		return firstPageHeader;
	}
	public XWPFFooter getFirstPageFooter() {
		return firstPageFooter;
	}
	/**
	 * Returns the odd page header. This is
	 *  also the same as the default one...
	 */
	public XWPFHeader getOddPageHeader() {
		return defaultHeader;
	}
	/**
	 * Returns the odd page footer. This is
	 *  also the same as the default one...
	 */
	public XWPFFooter getOddPageFooter() {
		return defaultFooter;
	}
	public XWPFHeader getEvenPageHeader() {
		return evenPageHeader;
	}
	public XWPFFooter getEvenPageFooter() {
		return evenPageFooter;
	}
	public XWPFHeader getDefaultHeader() {
		return defaultHeader;
	}
	public XWPFFooter getDefaultFooter() {
		return defaultFooter;
	}

	/**
	 * Get the header that applies to the given
	 *  (1 based) page.
	 * @param pageNumber The one based page number
	 */
	public XWPFHeader getHeader(int pageNumber) {
		if(pageNumber == 1 && firstPageHeader != null) {
			return firstPageHeader;
		}
		if(pageNumber % 2 == 0 && evenPageHeader != null) {
			return evenPageHeader;
		}
		return defaultHeader;
	}
	/**
	 * Get the footer that applies to the given
	 *  (1 based) page.
	 * @param pageNumber The one based page number
	 */
	public XWPFFooter getFooter(int pageNumber) {
		if(pageNumber == 1 && firstPageFooter != null) {
			return firstPageFooter;
		}
		if(pageNumber % 2 == 0 && evenPageFooter != null) {
			return evenPageFooter;
		}
		return defaultFooter;
	}
}
