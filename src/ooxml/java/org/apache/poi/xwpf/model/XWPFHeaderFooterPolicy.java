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
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFactory;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFHeaderFooter;
import org.apache.poi.xwpf.usermodel.XWPFRelation;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtrRef;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FtrDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.HdrDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHdrFtr.Enum;

/**
 * A .docx file can have no headers/footers, the same header/footer
 *  on each page, odd/even page footers, and optionally also 
 *  a different header/footer on the first page.
 * This class handles sorting out what there is, and giving you
 *  the right headers and footers for the document.
 */
public class XWPFHeaderFooterPolicy {
	public static final Enum DEFAULT = STHdrFtr.DEFAULT;
	public static final Enum EVEN = STHdrFtr.EVEN;
	public static final Enum FIRST = STHdrFtr.FIRST;

	private XWPFDocument doc;
	
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
		this.doc = doc;
		CTSectPr sectPr = doc.getDocument().getBody().getSectPr();
		for(int i=0; i<sectPr.sizeOfHeaderReferenceArray(); i++) {
			// Get the header
			CTHdrFtrRef ref = sectPr.getHeaderReferenceArray(i);
			PackagePart hdrPart = doc.getPartById(ref.getId());
			HdrDocument hdrDoc = HdrDocument.Factory.parse(hdrPart.getInputStream());
			CTHdrFtr hdrFtr = hdrDoc.getHdr();
			XWPFHeader hdr = new XWPFHeader(hdrFtr);

			// Assign it
			Enum type = ref.getType();
			assignHeader(hdr, type);
		}
		for(int i=0; i<sectPr.sizeOfFooterReferenceArray(); i++) {
			// Get the footer
			CTHdrFtrRef ref = sectPr.getFooterReferenceArray(i);
			PackagePart ftrPart = doc.getPartById(ref.getId());
			XWPFFooter ftr = new XWPFFooter(
					FtrDocument.Factory.parse(ftrPart.getInputStream()).getFtr()
			);

			// Assign it
			Enum type = ref.getType();
			assignFooter(ftr, type);
		}
	}


	private void assignFooter(XWPFFooter ftr, Enum type) {
		if(type == STHdrFtr.FIRST) {
			firstPageFooter = ftr;
		} else if(type == STHdrFtr.EVEN) {
			evenPageFooter = ftr;
		} else {
			defaultFooter = ftr;
		}
	}


	private void assignHeader(XWPFHeader hdr, Enum type) {
		if(type == STHdrFtr.FIRST) {
			firstPageHeader = hdr;
		} else if(type == STHdrFtr.EVEN) {
			evenPageHeader = hdr;
		} else {
			defaultHeader = hdr;
		}
	}

    
    public XWPFHeader createHeader(Enum type) throws IOException {
    	XWPFRelation relation = XWPFRelation.HEADER;
    	String pStyle = "Header";
    	int i = getRelationIndex(relation);
    	HdrDocument hdrDoc = HdrDocument.Factory.newInstance();
    	XWPFHeader wrapper = (XWPFHeader)doc.createRelationship(relation, XWPFFactory.getInstance(), i);

    	CTHdrFtr hdr = buildHdr(type, pStyle, wrapper);
    	
    	OutputStream outputStream = wrapper.getPackagePart().getOutputStream();
    	hdrDoc.setHdr(hdr);
    	
        XmlOptions xmlOptions = commit(wrapper);

    	assignHeader(wrapper, type);
		hdrDoc.save(outputStream, xmlOptions);
		outputStream.close();
    	return wrapper;
    }

    
    public XWPFFooter createFooter(Enum type) throws IOException {
    	XWPFRelation relation = XWPFRelation.FOOTER;
    	String pStyle = "Footer";
    	int i = getRelationIndex(relation);
    	FtrDocument ftrDoc = FtrDocument.Factory.newInstance();
    	XWPFFooter wrapper = (XWPFFooter)doc.createRelationship(relation, XWPFFactory.getInstance(), i);

    	CTHdrFtr ftr = buildFtr(type, pStyle, wrapper);
    	
    	OutputStream outputStream = wrapper.getPackagePart().getOutputStream();
    	ftrDoc.setFtr(ftr);
    	
        XmlOptions xmlOptions = commit(wrapper);

    	assignFooter(wrapper, type);
		ftrDoc.save(outputStream, xmlOptions);
		outputStream.close();
    	return wrapper;
    }


	private int getRelationIndex(XWPFRelation relation) {
		List<POIXMLDocumentPart> relations = doc.getRelations();
    	int i = 1;
		for (Iterator<POIXMLDocumentPart> it = relations.iterator(); it.hasNext() ; ) {
    		POIXMLDocumentPart item = it.next();
    		if (item.getPackageRelationship().getRelationshipType().equals(relation.getRelation())) {
    			i++;	
    		}
    	}
		return i;
	}


	private CTHdrFtr buildFtr(Enum type, String pStyle, XWPFHeaderFooter wrapper) {
		CTHdrFtr ftr = buildHdrFtr(pStyle);
    	setFooterReference(type, wrapper);
		return ftr;
	}


	private CTHdrFtr buildHdr(Enum type, String pStyle, XWPFHeaderFooter wrapper) {
		CTHdrFtr hdr = buildHdrFtr(pStyle);
    	setHeaderReference(type, wrapper);
		return hdr;
	}


	private CTHdrFtr buildHdrFtr(String pStyle) {
		CTHdrFtr ftr = CTHdrFtr.Factory.newInstance();
		CTP p = ftr.addNewP();
		byte[] rsidr = doc.getDocument().getBody().getPArray()[0].getRsidR();
		byte[] rsidrdefault = doc.getDocument().getBody().getPArray()[0].getRsidRDefault();
		p.setRsidP(rsidr);
		p.setRsidRDefault(rsidrdefault);
		CTPPr pPr = p.addNewPPr();
		pPr.addNewPStyle().setVal(pStyle);
		return ftr;
	}


	private void setFooterReference(Enum type, XWPFHeaderFooter wrapper) {
		CTHdrFtrRef ref = doc.getDocument().getBody().getSectPr().addNewFooterReference();
    	ref.setType(type);
    	ref.setId(wrapper.getPackageRelationship().getId());
	}


	private void setHeaderReference(Enum type, XWPFHeaderFooter wrapper) {
		CTHdrFtrRef ref = doc.getDocument().getBody().getSectPr().addNewHeaderReference();
    	ref.setType(type);
    	ref.setId(wrapper.getPackageRelationship().getId());
	}


	private XmlOptions commit(XWPFHeaderFooter wrapper) {
		XmlOptions xmlOptions = new XmlOptions(wrapper.DEFAULT_XML_OPTIONS);
        Map map = new HashMap();
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("urn:schemas-microsoft-com:vml", "v");
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        xmlOptions.setSaveSuggestedPrefixes(map);
		return xmlOptions;
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
