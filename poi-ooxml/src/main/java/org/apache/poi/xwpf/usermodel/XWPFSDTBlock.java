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

import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtBlock;

/**
 * Experimental class to offer rudimentary read-only processing of
 * StructuredDocumentTags/ContentControl
 * <p>
 * WARNING - APIs expected to change rapidly
 */
public class XWPFSDTBlock extends XWPFAbstractSDT implements IBodyElement {
    private final XWPFSDTContentBlock xwpfsdtContentBlock;
    private final CTSdtBlock sdtBlock;
    private final IBody part;

    public XWPFSDTBlock(CTSdtBlock sdtBlock, IBody part) {
        super(sdtBlock.getSdtPr());
        this.sdtBlock = sdtBlock;
        this.part = part;
        xwpfsdtContentBlock = new XWPFSDTContentBlock(sdtBlock.getSdtContent(), this);
    }

    public CTSdtBlock getCTSdt() {
        return sdtBlock;
    }

    @Override
    public XWPFSDTContentBlock getContent() {
        return xwpfsdtContentBlock;
    }


    /**
     * @return iBody
     */
    @Override
    public IBody getBody() {
        return part;
    }

    /**
     * @return document part
     */
    @Override
    public POIXMLDocumentPart getPart() {
        return part.getPart();
    }

    /**
     * @return partType
     */
    @Override
    public BodyType getPartType() {
        return BodyType.CONTENTCONTROL;
    }

    /**
     * @return element type
     */
    @Override
    public BodyElementType getElementType() {
        return BodyElementType.CONTENTCONTROL;
    }

    @Override
    public XWPFDocument getDocument() {
        return part.getXWPFDocument();
    }
}
