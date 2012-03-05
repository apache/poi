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

import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTBackgroundFillStyleList;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTStyleMatrixReference;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;

/**
 * Background shape
 *
 * @author Yegor Kozlov
 */
public class XSLFBackground extends XSLFSimpleShape {

    /* package */XSLFBackground(CTBackground shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    @Override
    public Rectangle2D getAnchor(){
        Dimension pg = getSheet().getSlideShow().getPageSize();
        return new Rectangle2D.Double(0, 0, pg.getWidth(), pg.getHeight());
    }

    public void draw(Graphics2D graphics) {
        Rectangle2D anchor = getAnchor();

        Paint fill = getPaint(graphics);
        if(fill != null) {
            graphics.setPaint(fill);
            graphics.fill(anchor);
        }
    }

    /**
     * @return the Paint object to fill
     */
    Paint getPaint(Graphics2D graphics){
        RenderableShape rShape = new RenderableShape(this);

        Paint fill = null;
        CTBackground bg = (CTBackground)getXmlObject();
        if(bg.isSetBgPr()){
            XmlObject spPr = bg.getBgPr();
            fill = rShape.getPaint(graphics, spPr, null);
        } else if (bg.isSetBgRef()){
            CTStyleMatrixReference bgRef= bg.getBgRef();
            CTSchemeColor phClr = bgRef.getSchemeClr();

            int idx = (int)bgRef.getIdx() - 1001;
            XSLFTheme theme = getSheet().getTheme();
            CTBackgroundFillStyleList bgStyles =
                    theme.getXmlObject().getThemeElements().getFmtScheme().getBgFillStyleLst();

            XmlObject bgStyle = bgStyles.selectPath("*")[idx];
            fill = rShape.selectPaint(graphics, bgStyle, phClr, theme.getPackagePart());
        }

        return fill;
    }

    @Override
    public Color getFillColor(){
        Paint p = getPaint(null);
        if(p instanceof Color){
            return (Color)p;
        }
        return null;
    }

    /**
     * background does not have a associated transform.
     * we return a dummy transform object to prevent exceptions in inherited methods.
     *
     * @return  dummy  CTTransform2D bean
     */
    @Override
    CTTransform2D getXfrm() {
        return CTTransform2D.Factory.newInstance();
    }
}
