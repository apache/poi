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

import java.util.Arrays;

import org.apache.poi.ooxml.util.POIXMLUnits;
import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.util.Internal;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTGradientStop;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRelativeRect;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSchemeColor;
import org.openxmlformats.schemas.drawingml.x2006.main.STPathShadeType;

@Internal
public class XSLFGradientPaint implements PaintStyle.GradientPaint {

    private final CTGradientFillProperties gradFill;
    final ColorStyle[] cs;
    final float[] fractions;

    public XSLFGradientPaint(final CTGradientFillProperties gradFill, CTSchemeColor phClr, final XSLFTheme theme, final XSLFSheet sheet) {
        this.gradFill = gradFill;

        final CTGradientStop[] gs = gradFill.getGsLst() == null ?
                new CTGradientStop[0] : gradFill.getGsLst().getGsArray();

        Arrays.sort(gs, (o1, o2) -> {
            int pos1 = POIXMLUnits.parsePercent(o1.xgetPos());
            int pos2 = POIXMLUnits.parsePercent(o2.xgetPos());
            return Integer.compare(pos1, pos2);
        });

        cs = new ColorStyle[gs.length];
        fractions = new float[gs.length];

        int i=0;
        for (CTGradientStop cgs : gs) {
            CTSchemeColor phClrCgs = phClr;
            if (phClrCgs == null && cgs.isSetSchemeClr()) {
                phClrCgs = cgs.getSchemeClr();
            }
            cs[i] = new XSLFColor(cgs, theme, phClrCgs, sheet).getColorStyle();
            fractions[i] = POIXMLUnits.parsePercent(cgs.xgetPos()) / 100000.f;
            i++;
        }

    }


    @Override
    public double getGradientAngle() {
        return (gradFill.isSetLin())
                ? gradFill.getLin().getAng() / 60000.d
                : 0;
    }

    @Override
    public ColorStyle[] getGradientColors() {
        return cs;
    }

    @Override
    public float[] getGradientFractions() {
        return fractions;
    }

    @Override
    public boolean isRotatedWithShape() {
        return gradFill.getRotWithShape();
    }

    @Override
    public PaintStyle.GradientPaint.GradientType getGradientType() {
        if (gradFill.isSetLin()) {
            return PaintStyle.GradientPaint.GradientType.linear;
        }

        if (gradFill.isSetPath()) {
            /* TODO: handle rect path */
            STPathShadeType.Enum ps = gradFill.getPath().getPath();
            if (ps == STPathShadeType.CIRCLE) {
                return PaintStyle.GradientPaint.GradientType.circular;
            } else if (ps == STPathShadeType.SHAPE) {
                return PaintStyle.GradientPaint.GradientType.shape;
            } else if (ps == STPathShadeType.RECT) {
                return PaintStyle.GradientPaint.GradientType.rectangular;
            }
        }

        return PaintStyle.GradientPaint.GradientType.linear;
    }

    @Override
    public Insets2D getFillToInsets() {
        if (gradFill.isSetPath() && gradFill.getPath().isSetFillToRect()) {
            final double base = 100_000;
            CTRelativeRect rect = gradFill.getPath().getFillToRect();
            return new Insets2D(
                POIXMLUnits.parsePercent(rect.xgetT())/base,
                POIXMLUnits.parsePercent(rect.xgetL())/base,
                POIXMLUnits.parsePercent(rect.xgetB())/base,
                POIXMLUnits.parsePercent(rect.xgetR())/base
            );
        }
        return null;
    }
}
