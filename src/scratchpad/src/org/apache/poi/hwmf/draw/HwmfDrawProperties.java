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

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;

import org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.apache.poi.hwmf.record.HwmfColorRef;
import org.apache.poi.hwmf.record.HwmfFill.WmfSetPolyfillMode.HwmfPolyfillMode;
import org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.apache.poi.hwmf.record.HwmfMapMode;
import org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.apache.poi.hwmf.record.HwmfPenStyle;

public class HwmfDrawProperties {
    private final Rectangle2D window;
    private Rectangle2D viewport = null;
    private final Point2D location;
    private HwmfMapMode mapMode = HwmfMapMode.MM_ANISOTROPIC;
    private HwmfColorRef backgroundColor = new HwmfColorRef(Color.BLACK);
    private HwmfBrushStyle brushStyle = HwmfBrushStyle.BS_SOLID;
    private HwmfColorRef brushColor = new HwmfColorRef(Color.BLACK);
    private HwmfHatchStyle brushHatch = HwmfHatchStyle.HS_HORIZONTAL;
    private BufferedImage brushBitmap = null;
    private double penWidth = 1;
    private HwmfPenStyle penStyle = HwmfPenStyle.valueOf(0);
    private HwmfColorRef penColor = new HwmfColorRef(Color.BLACK);
    private double penMiterLimit = 10;
    private HwmfBkMode bkMode = HwmfBkMode.OPAQUE;
    private HwmfPolyfillMode polyfillMode = HwmfPolyfillMode.WINDING;
    private Shape region = null;

    public HwmfDrawProperties() {
        window = new Rectangle2D.Double(0, 0, 1, 1);
        viewport = null;
        location = new Point2D.Double(0,0);
    }
    
    public HwmfDrawProperties(HwmfDrawProperties other) {
        this.window = (Rectangle2D)other.window.clone();
        this.viewport = (other.viewport == null) ? null : (Rectangle2D)other.viewport.clone();
        this.location = (Point2D)other.location.clone();
        this.mapMode = other.mapMode;
        this.backgroundColor = (other.backgroundColor == null) ? null : other.backgroundColor.clone();
        this.brushStyle = other.brushStyle;
        this.brushColor = other.brushColor.clone();
        this.brushHatch = other.brushHatch;
        if (other.brushBitmap != null) {
            ColorModel cm = other.brushBitmap.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = other.brushBitmap.copyData(null);
            this.brushBitmap = new BufferedImage(cm, raster, isAlphaPremultiplied, null);            
        }
        this.penWidth = 1;
        this.penStyle = (other.penStyle == null) ? null : other.penStyle.clone();
        this.penColor = (other.penColor == null) ? null : other.penColor.clone();
        this.penMiterLimit = other.penMiterLimit;
        this.bkMode = other.bkMode;
        this.polyfillMode = other.polyfillMode;
        if (other.region instanceof Rectangle2D) {
            this.region = ((Rectangle2D)other.region).getBounds2D();
        } else if (other.region instanceof Area) {
            this.region = new Area(other.region);
        }
    }
    
    public void setViewportExt(double width, double height) {
        double x = viewport.getX();
        double y = viewport.getY();
        double w = (width != 0) ? width : viewport.getWidth();
        double h = (height != 0) ? height : viewport.getHeight();
        viewport.setRect(x, y, w, h);
    }

    public void setViewportOrg(double x, double y) {
        double w = viewport.getWidth();
        double h = viewport.getHeight();
        viewport.setRect(x, y, w, h);
    }

    public Rectangle2D getViewport() {
        return (viewport == null) ? null : (Rectangle2D)viewport.clone();
    }

    public void setWindowExt(double width, double height) {
        double x = window.getX();
        double y = window.getY();
        double w = (width != 0) ? width : window.getWidth();
        double h = (height != 0) ? height : window.getHeight();
        window.setRect(x, y, w, h);
    }

    public void setWindowOrg(double x, double y) {
        double w = window.getWidth();
        double h = window.getHeight();
        window.setRect(x, y, w, h);
    }

    public Rectangle2D getWindow() {
        return (Rectangle2D)window.clone();
    }

    public void setLocation(double x, double y) {
        location.setLocation(x, y);
    }

    public Point2D getLocation() {
        return (Point2D)location.clone();
    }

    public void setMapMode(HwmfMapMode mapMode) {
        this.mapMode = mapMode;
    }

    public HwmfMapMode getMapMode() {
        return mapMode;
    }

    public HwmfBrushStyle getBrushStyle() {
        return brushStyle;
    }

    public void setBrushStyle(HwmfBrushStyle brushStyle) {
        this.brushStyle = brushStyle;
    }

    public HwmfHatchStyle getBrushHatch() {
        return brushHatch;
    }

    public void setBrushHatch(HwmfHatchStyle brushHatch) {
        this.brushHatch = brushHatch;
    }

    public HwmfColorRef getBrushColor() {
        return brushColor;
    }

    public void setBrushColor(HwmfColorRef brushColor) {
        this.brushColor = brushColor;
    }

    public HwmfBkMode getBkMode() {
        return bkMode;
    }

    public void setBkMode(HwmfBkMode bkMode) {
        this.bkMode = bkMode;
    }

    public HwmfPenStyle getPenStyle() {
        return penStyle;
    }

    public void setPenStyle(HwmfPenStyle penStyle) {
        this.penStyle = penStyle;
    }

    public HwmfColorRef getPenColor() {
        return penColor;
    }

    public void setPenColor(HwmfColorRef penColor) {
        this.penColor = penColor;
    }

    public double getPenWidth() {
        return penWidth;
    }

    public void setPenWidth(double penWidth) {
        this.penWidth = penWidth;
    }

    public double getPenMiterLimit() {
        return penMiterLimit;
    }

    public void setPenMiterLimit(double penMiterLimit) {
        this.penMiterLimit = penMiterLimit;
    }

    public HwmfColorRef getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(HwmfColorRef backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public HwmfPolyfillMode getPolyfillMode() {
        return polyfillMode;
    }

    public void setPolyfillMode(HwmfPolyfillMode polyfillMode) {
        this.polyfillMode = polyfillMode;
    }

    public BufferedImage getBrushBitmap() {
        return brushBitmap;
    }

    public void setBrushBitmap(BufferedImage brushBitmap) {
        this.brushBitmap = brushBitmap;
    }


    /**
     * Gets the last stored region
     *
     * @return the last stored region
     */
    public Shape getRegion() {
        return region;
    }

    /**
     * Sets a region for further usage
     *
     * @param region a region object which is usually a rectangle
     */
    public void setRegion(Shape region) {
        this.region = region;
    }
}
