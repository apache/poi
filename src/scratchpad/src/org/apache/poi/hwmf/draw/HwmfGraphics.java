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

package org.apache.poi.hwmf.draw;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.Deque;

import org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.apache.poi.hwmf.record.HwmfPenStyle;
import org.apache.poi.hwmf.record.HwmfPenStyle.HwmfLineDash;
import org.apache.poi.util.Units;

public class HwmfGraphics {
    private final Graphics2D graphicsCtx;
    private final Deque<HwmfDrawProperties> propStack = new ArrayDeque<HwmfDrawProperties>();
    HwmfDrawProperties prop;

    public HwmfGraphics(Graphics2D graphicsCtx) {
        this.graphicsCtx = graphicsCtx;
        prop = new HwmfDrawProperties();
        propStack.push(prop);
    }

    public HwmfDrawProperties getProperties() {
        return prop;
    }

    public void draw(Shape shape) {
        HwmfLineDash lineDash = prop.getPenStyle().getLineDash();
        if (lineDash == HwmfLineDash.NULL) {
            // line is not drawn
            return;
        }

        Shape tshape = fitShapeToView(shape);
        BasicStroke stroke = getStroke();

        // first draw a solid background line (depending on bkmode)
        // only makes sense if the line is not solid
        if (prop.getBkMode() == HwmfBkMode.OPAQUE && (lineDash != HwmfLineDash.SOLID && lineDash != HwmfLineDash.INSIDEFRAME)) {
            graphicsCtx.setStroke(new BasicStroke(stroke.getLineWidth()));
            graphicsCtx.setColor(prop.getBackgroundColor().getColor());
            graphicsCtx.draw(tshape);
        }

        // then draw the (dashed) line
        graphicsCtx.setStroke(stroke);
        graphicsCtx.setColor(prop.getPenColor().getColor());
        graphicsCtx.draw(tshape);
    }

    public void fill(Shape shape) {
        if (prop.getBrushStyle() != HwmfBrushStyle.BS_NULL) {
            GeneralPath gp = new GeneralPath(shape);
            gp.setWindingRule(prop.getPolyfillMode().awtFlag);
            Shape tshape = fitShapeToView(gp);
            graphicsCtx.setPaint(getFill());
            graphicsCtx.fill(tshape);
        }

        draw(shape);
    }

    protected Shape fitShapeToView(Shape shape) {
        int scaleUnits = prop.getMapMode().scale;
        Rectangle2D view = prop.getViewport(), win = prop.getWindow();
        double scaleX, scaleY;
        switch (scaleUnits) {
        case -1:
            scaleX = view.getWidth() / win.getWidth();
            scaleY = view.getHeight() / win.getHeight();
            break;
        case 0:
            scaleX = scaleY = 1;
            break;
        default:
            scaleX = scaleY = scaleUnits / (double)Units.POINT_DPI;
        }

        AffineTransform at = new AffineTransform();
        at.translate(view.getX(), view.getY());
        at.scale(scaleX, scaleY);
        at.translate(-win.getX(), -win.getY());
        at.translate(-view.getX(), -view.getY());

        return at.createTransformedShape(shape);
    }

    protected BasicStroke getStroke() {
        Rectangle2D view = prop.getViewport(), win = prop.getWindow();
        float width = (float)(prop.getPenWidth() * view.getWidth() / win.getWidth());
        HwmfPenStyle ps = prop.getPenStyle();
        int cap = ps.getLineCap().awtFlag;
        int join = ps.getLineJoin().awtFlag;
        float miterLimit = (float)prop.getPenMiterLimit();
        float dashes[] = ps.getLineDash().dashes;
        boolean dashAlt = ps.isAlternateDash();
        // This value is not an integer index into the dash pattern array.
        // Instead, it is a floating-point value that specifies a linear distance.
        float dashStart = (dashAlt && dashes.length > 1) ? dashes[0] : 0;

        return new BasicStroke(width, cap, join, miterLimit, dashes, dashStart);
    }

    protected Paint getFill() {
        switch (prop.getBrushStyle()) {
        default:
        case BS_INDEXED:
        case BS_PATTERN8X8:
        case BS_DIBPATTERN8X8:
        case BS_MONOPATTERN:
        case BS_NULL: return null;
        case BS_PATTERN:
        case BS_DIBPATTERN:
        case BS_DIBPATTERNPT: return getPatternPaint();
        case BS_SOLID: return getSolidFill();
        case BS_HATCHED: return getHatchedFill();
        }
    }

    protected Paint getSolidFill() {
        return prop.getBrushColor().getColor();
    }

    protected Paint getHatchedFill() {
        int dim = 7, mid = 3;
        BufferedImage bi = new BufferedImage(dim, dim, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g = bi.createGraphics();
        Color c = (prop.getBkMode() == HwmfBkMode.TRANSPARENT)
            ? new Color(0, true)
            : prop.getBackgroundColor().getColor();
        g.setColor(c);
        g.fillRect(0, 0, dim, dim);
        g.setColor(prop.getBrushColor().getColor());
        HwmfHatchStyle h = prop.getBrushHatch();
        if (h == HwmfHatchStyle.HS_HORIZONTAL || h == HwmfHatchStyle.HS_CROSS) {
            g.drawLine(0, mid, dim, mid);
        }
        if (h == HwmfHatchStyle.HS_VERTICAL || h == HwmfHatchStyle.HS_CROSS) {
            g.drawLine(mid, 0, mid, dim);
        }
        if (h == HwmfHatchStyle.HS_FDIAGONAL || h == HwmfHatchStyle.HS_DIAGCROSS) {
            g.drawLine(0, 0, dim, dim);
        }
        if (h == HwmfHatchStyle.HS_BDIAGONAL || h == HwmfHatchStyle.HS_DIAGCROSS) {
            g.drawLine(0, dim, dim, 0);
        }
        g.dispose();
        return new TexturePaint(bi, new Rectangle(0,0,dim,dim));
    }

    protected Paint getPatternPaint() {
        BufferedImage bi = prop.getBrushBitmap();
        return (bi == null) ? null
            : new TexturePaint(bi, new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
    }
}
