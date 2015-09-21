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
import java.awt.Rectangle;

import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.Background;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.FillStyle;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PaintStyle.SolidPaint;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D;
import org.openxmlformats.schemas.presentationml.x2006.main.CTBackground;

/**
 * Background shape
 *
 * @author Yegor Kozlov
 */
public class XSLFBackground extends XSLFSimpleShape
    implements Background<XSLFShape,XSLFTextParagraph> {

    /* package */XSLFBackground(CTBackground shape, XSLFSheet sheet) {
        super(shape, sheet);
    }

    @Override
    public Rectangle getAnchor(){
        Dimension pg = getSheet().getSlideShow().getPageSize();
        return new Rectangle(0, 0, (int)pg.getWidth(), (int)pg.getHeight());
    }

    @Override
    public Color getFillColor(){
        FillStyle fs = getFillStyle();
        PaintStyle ps = fs.getPaint();
        if (ps instanceof SolidPaint) {
            SolidPaint sp = (SolidPaint)ps;
            ColorStyle cs = sp.getSolidColor();
            return DrawPaint.applyColorTransform(cs);
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
    protected CTTransform2D getXfrm() {
        return CTTransform2D.Factory.newInstance();
    }
}
