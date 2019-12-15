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
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHdrFtr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTNumbering;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTP;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTbl;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.FtrDocument;

/**
 * Sketch of XWPF footer class
 */
public class XWPFFooter extends XWPFHeaderFooter {
    public XWPFFooter() {
        super();
    }

    public XWPFFooter(XWPFDocument doc, CTHdrFtr hdrFtr) throws IOException {
        super(doc, hdrFtr);
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

        }
        cursor.dispose();
    }

    /**
     * @since POI 3.14-Beta1
     */
    public XWPFFooter(POIXMLDocumentPart parent, PackagePart part) throws IOException {
        super(parent, part);
    }

    /**
     * save and commit footer
     */
    @Override
    protected void commit() throws IOException {
        XmlOptions xmlOptions = new XmlOptions(DEFAULT_XML_OPTIONS);
        xmlOptions.setSaveSyntheticDocumentElement(new QName(CTNumbering.type.getName().getNamespaceURI(), "ftr"));
        PackagePart part = getPackagePart();
        OutputStream out = part.getOutputStream();
        super._getHdrFtr().save(out, xmlOptions);
        out.close();
    }

    @Override
    protected void onDocumentRead() throws IOException {
        super.onDocumentRead();
        FtrDocument ftrDocument = null;
        try (InputStream is = getPackagePart().getInputStream()) {
            ftrDocument = FtrDocument.Factory.parse(is, DEFAULT_XML_OPTIONS);
            headerFooter = ftrDocument.getFtr();
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
        } catch (Exception e) {
            throw new POIXMLException(e);
        }
    }

    /**
     * get the PartType of the body
     *
     * @see org.apache.poi.xwpf.usermodel.IBody#getPartType()
     */
    public BodyType getPartType() {
        return BodyType.FOOTER;
    }
}
