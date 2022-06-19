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

import java.awt.geom.Path2D;

import org.apache.poi.sl.draw.geom.ClosePathCommand;
import org.apache.poi.sl.draw.geom.Context;
import org.apache.poi.sl.draw.geom.PathCommand;
import org.apache.poi.sl.draw.geom.PathIf;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.util.Beta;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DArcTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DClose;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DCubicBezierTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DLineTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DMoveTo;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPath2DQuadBezierTo;
import org.openxmlformats.schemas.drawingml.x2006.main.STPathFillMode;

/**
 * Wrapper / delegate for XmlBeans custom geometry
 */
@Beta
public class XSLFPath implements PathIf {

    private final CTPath2D pathXml;

    public XSLFPath(CTPath2D pathXml) {
        this.pathXml = pathXml;
    }

    @Override
    public void addCommand(PathCommand cmd) {
        // not supported
    }

    @Override
    public Path2D.Double getPath(Context ctx) {
        Path2D.Double path2D = new Path2D.Double();
        try (XmlCursor cur = pathXml.newCursor()) {
            for (boolean hasNext = cur.toFirstChild(); hasNext; hasNext = cur.toNextSibling()) {
                XmlObject xo = cur.getObject();
                PathCommand pc;
                if (xo instanceof CTPath2DArcTo) {
                    pc = new XSLFArcTo((CTPath2DArcTo) xo);
                } else if (xo instanceof CTPath2DCubicBezierTo) {
                    pc = new XSLFCurveTo((CTPath2DCubicBezierTo) xo);
                } else if (xo instanceof CTPath2DMoveTo) {
                    pc = new XSLFMoveTo((CTPath2DMoveTo) xo);
                } else if (xo instanceof CTPath2DLineTo) {
                    pc = new XSLFLineTo((CTPath2DLineTo) xo);
                } else if (xo instanceof CTPath2DQuadBezierTo) {
                    pc = new XSLFQuadTo((CTPath2DQuadBezierTo) xo);
                } else if (xo instanceof CTPath2DClose) {
                    pc = new ClosePathCommand();
                } else {
                    continue;
                }
                pc.execute(path2D, ctx);
            }
        }
        return path2D;
    }

    @Override
    public boolean isStroked() {
        return pathXml.getStroke();
    }

    @Override
    public void setStroke(boolean stroke) {
        pathXml.setStroke(stroke);
    }

    @Override
    public boolean isFilled() {
        return pathXml.getFill() != STPathFillMode.NONE;
    }

    @Override
    public PaintStyle.PaintModifier getFill() {
        switch (pathXml.getFill().intValue()) {
            default:
            case STPathFillMode.INT_NONE:
                return PaintStyle.PaintModifier.NONE;
            case STPathFillMode.INT_NORM:
                return PaintStyle.PaintModifier.NORM;
            case STPathFillMode.INT_LIGHTEN:
                return PaintStyle.PaintModifier.LIGHTEN;
            case STPathFillMode.INT_LIGHTEN_LESS:
                return PaintStyle.PaintModifier.LIGHTEN_LESS;
            case STPathFillMode.INT_DARKEN:
                return PaintStyle.PaintModifier.DARKEN;
            case STPathFillMode.INT_DARKEN_LESS:
                return PaintStyle.PaintModifier.DARKEN_LESS;
        }
    }

    @Override
    public void setFill(PaintStyle.PaintModifier fill) {
        STPathFillMode.Enum f;
        switch (fill) {
            default:
            case NONE:
                f = STPathFillMode.NONE;
                break;
            case NORM:
                f = STPathFillMode.NORM;
                break;
            case LIGHTEN:
                f = STPathFillMode.LIGHTEN;
                break;
            case LIGHTEN_LESS:
                f = STPathFillMode.LIGHTEN_LESS;
                break;
            case DARKEN:
                f = STPathFillMode.DARKEN;
                break;
            case DARKEN_LESS:
                f = STPathFillMode.DARKEN_LESS;
                break;
        }
        pathXml.setFill(f);
    }

    @Override
    public long getW() {
        return pathXml.getW();
    }

    @Override
    public void setW(long w) {
        pathXml.setW(w);
    }

    @Override
    public long getH() {
        return pathXml.getH();
    }

    @Override
    public void setH(long h) {
        pathXml.setH(h);
    }

    @Override
    public boolean isExtrusionOk() {
        return pathXml.getExtrusionOk();
    }

    @Override
    public void setExtrusionOk(boolean extrusionOk) {
        pathXml.setExtrusionOk(extrusionOk);
    }

}
