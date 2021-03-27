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
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSdtPr;

/**
 * Experimental abstract class that is a base for XWPFSDT and XWPFSDTCell
 * <p>
 * WARNING - APIs expected to change rapidly.
 * <p>
 * These classes have so far been built only for read-only processing.
 */
public abstract class XWPFAbstractSDT implements ISDTContents {
    private final String title;
    private final String tag;
    private final IBody part;

    public XWPFAbstractSDT(CTSdtPr pr, IBody part) {
        title = (pr != null && pr.isSetAlias()) ? pr.getAlias().getVal() : "";
        tag = (pr != null && pr.isSetTag()) ? pr.getTag().getVal() : "";
        this.part = part;

    }

    /**
     * @return first SDT Title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return first SDT Tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @return the content object
     */
    public abstract ISDTContent getContent();

    /**
     * @return null
     */
    public IBody getBody() {
        return null;
    }

    /**
     * @return document part
     */
    public POIXMLDocumentPart getPart() {
        return part.getPart();
    }

    /**
     * @return partType
     */
    public BodyType getPartType() {
        return BodyType.CONTENTCONTROL;
    }

    /**
     * @return element type
     */
    public BodyElementType getElementType() {
        return BodyElementType.CONTENTCONTROL;
    }

    public XWPFDocument getDocument() {
        return part.getXWPFDocument();
    }
}
