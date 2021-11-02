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

package org.apache.poi.xslf.draw.geom;

import org.apache.poi.sl.draw.geom.AdjustPointIf;
import org.apache.poi.sl.draw.geom.CurveToCommandIf;
import org.apache.poi.util.Beta;
import org.openxmlformats.schemas.drawingml.x2006.main.CTAdjPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DCubicBezierTo;

/**
 * Wrapper / delegate for XmlBeans custom geometry
 */
@Beta
public class XSLFCurveTo implements CurveToCommandIf {
    private final CTPath2DCubicBezierTo bezier;

    public XSLFCurveTo(CTPath2DCubicBezierTo bezier) {
        this.bezier = bezier;
    }

    @Override
    public XSLFAdjustPoint getPt1() {
        return new XSLFAdjustPoint(bezier.getPtArray(0));
    }

    @Override
    public void setPt1(AdjustPointIf pt1) {
        CTAdjPoint2D xpt = getOrCreate(0);
        xpt.setX(pt1.getX());
        xpt.setY(pt1.getY());
    }

    @Override
    public XSLFAdjustPoint getPt2() {
        return new XSLFAdjustPoint(bezier.getPtArray(1));
    }

    @Override
    public void setPt2(AdjustPointIf pt2) {
        CTAdjPoint2D xpt = getOrCreate(1);
        xpt.setX(pt2.getX());
        xpt.setY(pt2.getY());
    }

    @Override
    public XSLFAdjustPoint getPt3() {
        return new XSLFAdjustPoint(bezier.getPtArray(2));
    }

    @Override
    public void setPt3(AdjustPointIf pt3) {
        CTAdjPoint2D xpt = getOrCreate(2);
        xpt.setX(pt3.getX());
        xpt.setY(pt3.getY());
    }

    private CTAdjPoint2D getOrCreate(int idx) {
        for (int i=(idx+1)-bezier.sizeOfPtArray(); i > 0; i--) {
            bezier.addNewPt();
        }
        return bezier.getPtArray(idx);
    }
}
