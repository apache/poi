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

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackagePart;

/**
 * Base class for the Footnotes and Endnotes part implementations.
 * @since 4.0.0
 */
public abstract class AbstractXWPFFootnotesEndnotes extends POIXMLDocumentPart {

    protected XWPFDocument document;
    protected List<AbstractXWPFFootnoteEndnote> listFootnote = new ArrayList<>();
    private FootnoteEndnoteIdManager idManager;
    
    public AbstractXWPFFootnotesEndnotes(OPCPackage pkg) {
        super(pkg);
    }

    public AbstractXWPFFootnotesEndnotes(OPCPackage pkg,
            String coreDocumentRel) {
        super(pkg, coreDocumentRel);
    }

    public AbstractXWPFFootnotesEndnotes() {
        super();
    }

    public AbstractXWPFFootnotesEndnotes(PackagePart part) {
        super(part);
    }

    public AbstractXWPFFootnotesEndnotes(POIXMLDocumentPart parent, PackagePart part) {
        super(parent, part);
    }


    public AbstractXWPFFootnoteEndnote getFootnoteById(int id) {
        for (AbstractXWPFFootnoteEndnote note : listFootnote) {
            if (note.getCTFtnEdn().getId().intValue() == id)
                return note;
        }
        return null;
    }

    /**
     * @see org.apache.poi.xwpf.usermodel.IBody#getPart()
     */
    public XWPFDocument getXWPFDocument() {
        if (document != null) {
            return document;
        } else {
            return (XWPFDocument) getParent();
        }
    }

    public void setXWPFDocument(XWPFDocument doc) {
        document = doc;
    }

    public void setIdManager(FootnoteEndnoteIdManager footnoteIdManager) {
       this.idManager = footnoteIdManager;
        
    }
    
    public FootnoteEndnoteIdManager getIdManager() {
        return this.idManager;
    }

}