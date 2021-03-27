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

import static org.apache.poi.ooxml.POIXMLTypeLoader.DEFAULT_XML_OPTIONS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.namespace.QName;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.HdrDocument;

/**
 * Sketch of XWPF header class
 */
public class XWPFHeader extends XWPFHeaderFooter {
    public XWPFHeader() {
        super();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public XWPFHeader(POIXMLDocumentPart parent, PackagePart part) throws IOException {
        super(parent, part);
    }

    public XWPFHeader(XWPFDocument doc, CTHdrFtr hdrFtr) {
        super(doc, hdrFtr);
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
        cursor.dispose();
    }

    /**
     * save and commit footer
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTNumbering.type.getName().getNamespaceURI(), "hdr"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        super._getHdrFtr().save(out, xmlOptions);
        out.close();
    }

    /**
     * reads the document
     *
     * @throws IOException
     */
    @Override
    protected void onDocumentRead() throws IOException {
        super.onDocumentRead();
        HdrDocument hdrDocument = null;
        try (InputStream is = getPackagePart().getInputStream()) {
            hdrDocument = HdrDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            headerFooter = hdrDocument.getHdr();
            // parse the document with cursor and add
            // the XmlObject to its lists
            XmlCursor cursor = headerFooter.newCursor();
            cursor.selectPath("./*");
            while (cursor.toNextSelection()) {
                XmlObject o = cursor.getObject();
                if (o instanceof CTP) {
                    XWPFParagraph p = new XWPFParagraph((CTP) o, this);
                    paragraphs.add(p);
                    bodyElements.add(p);
                }
                if (o instanceof CTTbl) {
                    XWPFTable t = new XWPFTable((CTTbl) o, this);
                    tables.add(t);
                    bodyElements.add(t);
                }
                if (o instanceof CTSdtBlock) {
                    XWPFSDT c = new XWPFSDT((CTSdtBlock) o, this);
                    bodyElements.add(c);
                }
            }
            cursor.dispose();
        } catch (XmlException e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * get the PartType of the body
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    public BodyType getPartType() {
        return BodyType.HEADER;
    }
}//end class
