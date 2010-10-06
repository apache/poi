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
package org.apache.poi.xwpf.usermodel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.HdrDocument;

/**
 * Sketch of XWPF header class
 */
public class XWPFHeader extends XWPFHeaderFooter {
	
	public XWPFHeader() {
		super();
	}
	
	public XWPFHeader(PackagePart part, PackageRelationship rel) throws IOException {
		super(part, rel);
	}
	
	public XWPFHeader(CTHdrFtr hdrFtr) throws IOException {
		super(hdrFtr);
		paragraphs = new ArrayList<XWPFParagraph>();
		tables = new ArrayList<XWPFTable>();
		XmlCursor cursor = headerFooter.newCursor();
		cursor.selectPath("./*");
		while (cursor.toNextSelection()) {
			XmlObject o = cursor.getObject();
			if (o instanceof CTP) {
				XWPFParagraph p = new XWPFParagraph((CTP) o, this);
				paragraphs.add(p);
			}
			if (o instanceof CTTbl) {
				XWPFTable t = new XWPFTable((CTTbl) o, this);
				tables.add(t);
			}
		}
		getAllPictures();
	}

	/**
	public XWPFHeader(PackagePart part, PackageRelationship rel)
			throws IOException {
		super(part, rel);
	}
	
	/**
	 * save and commit footer
	 */
	@Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTNumbering.type.getName().getNamespaceURI(), "hdr"));
        Map<String,String> map = new HashMap<String, String>();
        map.put("http://schemas.openxmlformats.org/markup-compatibility/2006", "ve");
        map.put("urn:schemas-microsoft-com:office:office", "o");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "r");
        map.put("http://schemas.openxmlformats.org/officeDocument/2006/math", "m");
        map.put("urn:schemas-microsoft-com:vml", "v");
        map.put("http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing", "wp");
        map.put("urn:schemas-microsoft-com:office:word", "w10");
        map.put("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "w");
        map.put("http://schemas.microsoft.com/office/word/2006/wordml", "wne");
        xmlOptions.setSaveSuggestedPrefixes(map);
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        super._getHdrFtr().save(out, xmlOptions);
        out.close();
    }

	/**
	 * reads the document
	 */
	  @Override  
	    protected void onDocumentRead(){
		  	bodyElements = new ArrayList<IBodyElement>();
	        paragraphs = new ArrayList<XWPFParagraph>();
	        tables= new ArrayList<XWPFTable>();
	        HdrDocument hdrDocument = null;
			InputStream is;
			try {
				is = getPackagePart().getInputStream();
				hdrDocument = HdrDocument.Factory.parse(is);
				headerFooter = hdrDocument.getHdr();
		        // parse the document with cursor and add
		        // the XmlObject to its lists
				XmlCursor cursor = headerFooter.newCursor();
		        cursor.selectPath("./*");
		        while (cursor.toNextSelection()) {
		            XmlObject o = cursor.getObject();
		            if (o instanceof CTP) {
		            	XWPFParagraph p = new XWPFParagraph((CTP)o, this);
		            	paragraphs.add(p);
		            	bodyElements.add(p);
		            }
		            if (o instanceof CTTbl) {
		            	XWPFTable t = new XWPFTable((CTTbl)o, this);
		            	tables.add(t);
		            	bodyElements.add(t);
		            }
		        }
		        getAllPictures();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XmlException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	  }

	/**
	 * returns the Part, to which the body belongs, which you need for adding relationship to other parts
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
	 */
	public IBody getPart() {
		return this;
	}

	/**
	 * get the PartType of the body
	 * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
	 */
	public BodyType getPartType() {
		return BodyType.HEADER;
	}


}//end class
