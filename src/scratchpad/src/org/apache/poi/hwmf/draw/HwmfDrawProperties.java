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
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.apache.poi.hwmf.record.HwmfBrushStyle;
import org.apache.poi.hwmf.record.HwmfColorRef;
import org.apache.poi.hwmf.record.HwmfFill.WmfSetPolyfillMode.HwmfPolyfillMode;
import org.apache.poi.hwmf.record.HwmfHatchStyle;
import org.apache.poi.hwmf.record.HwmfMapMode;
import org.apache.poi.hwmf.record.HwmfMisc.WmfSetBkMode.HwmfBkMode;
import org.apache.poi.hwmf.record.HwmfPenStyle;

public class HwmfDrawProperties {
    private Rectangle2D window = new Rectangle2D.Double(0, 0, 1, 1);
    private Rectangle2D viewport = new Rectangle2D.Double(0, 0, 1, 1);
    private Point2D location = new Point2D.Double(0,0);
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
        return (Rectangle2D)viewport.clone();
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
}
