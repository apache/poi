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

package org.apache.poi.xslf.usermodel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.sl.usermodel.Background;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackgroundProperties;

/**
 * Background shape
 */
public class XSLFBackground extends XSLFSimpleShape
    implements Background<XSLFShape,XSLFTextParagraph> {

    /* package */XSLFBackground(CTBackground shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    @Override
    public Rectangle2D getAnchor(){
        Dimension pg = getSheet().getSlideShow().getPageSize();
        return new Rectangle2D.Double(0, 0, pg.getWidth(), pg.getHeight());
    }

    /**
     * background does not have a associated transform, therefore we return null
     *
     * @param create ignored
     *
     * @return null
     */
    @Override
    protected CTTransform2D getXfrm(boolean create) {
        return null;
    }

    @Override
    public void setPlaceholder(Placeholder placeholder) {
        // extending XSLFSimpleShape is a bit unlucky ...
        throw new POIXMLException("Can't set a placeholder for a background");
    }

    protected CTBackgroundProperties getBgPr(boolean create) {
        CTBackground bg = (CTBackground)getXmlObject();
        if (!bg.isSetBgPr() && create) {
            if (bg.isSetBgRef()) {
                bg.unsetBgRef();
            }
            return bg.addNewBgPr();
        }
        return bg.getBgPr();
    }

    @Override
    public void setFillColor(Color color) {
        CTBackgroundProperties bgPr = getBgPr(true);

        if (bgPr.isSetBlipFill()) {
            bgPr.unsetBlipFill();
        }
        if (bgPr.isSetGradFill()) {
            bgPr.unsetGradFill();
        }
        if (bgPr.isSetGrpFill()) {
            bgPr.unsetGrpFill();
        }
        if (bgPr.isSetPattFill()) {
            bgPr.unsetPattFill();
        }

        if (color == null) {
            if (bgPr.isSetSolidFill()) {
                bgPr.unsetSolidFill();
            }

            if (!bgPr.isSetNoFill()) {
                bgPr.addNewNoFill();
            }
        } else {
            if (bgPr.isSetNoFill()) {
                bgPr.unsetNoFill();
            }

            CTSolidColorFillProperties fill = bgPr.isSetSolidFill() ? bgPr.getSolidFill() : bgPr.addNewSolidFill();

            XSLFColor col = new XSLFColor(fill, getSheet().getTheme(), fill.getSchemeClr(), getSheet());
            col.setColor(color);
        }
    }

    @Override
    protected XmlObject getShapeProperties() {
        CTBackground bg = (CTBackground)getXmlObject();
        if (bg.isSetBgPr()) {
            return bg.getBgPr();
        } else if (bg.isSetBgRef()) {
            return bg.getBgRef();
        } else {
            return null;
        }
    }
}
