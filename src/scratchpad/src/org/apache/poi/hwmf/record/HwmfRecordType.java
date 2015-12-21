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

package org.apache.poi.hwmf.record;

/**
 * Available record types for WMF
 * 
 * @see <a href="http://www.symantec.com/avcenter/reference/inside.the.windows.meta.file.format.pdf">Inside the Windows Meta File Format</a>
 */
public enum HwmfRecordType {
    eof(0x0000, null),
    realizePalette(0x0035, HwmfPalette.WmfRealizePalette.class),
    setPalEntries(0x0037, HwmfPalette.WmfSetPaletteEntries.class),
    setBkMode(0x0102, HwmfMisc.WmfSetBkMode.class),
    setMapMode(0x0103, HwmfMisc.WmfSetMapMode.class),
    setRop2(0x0104, HwmfMisc.WmfSetRop2.class),
    setRelabs(0x0105, HwmfMisc.WmfSetRelabs.class),
    setPolyFillMode(0x0106, HwmfFill.WmfSetPolyfillMode.class),
    setStretchBltMode(0x0107, HwmfMisc.WmfSetStretchBltMode.class),
    setTextCharExtra(0x0108, HwmfText.WmfSetTextCharExtra.class),
    restoreDc(0x0127, HwmfMisc.WmfRestoreDc.class),
    resizePalette(0x0139, HwmfPalette.WmfResizePalette.class),
    dibCreatePatternBrush(0x0142, HwmfMisc.WmfDibCreatePatternBrush.class),
    setLayout(0x0149, HwmfMisc.WmfSetLayout.class),
    setBkColor(0x0201, HwmfMisc.WmfSetBkColor.class),
    setTextColor(0x0209, HwmfText.WmfSetTextColor.class),
    offsetViewportOrg(0x0211, HwmfWindowing.WmfOffsetViewportOrg.class),
    lineTo(0x0213, HwmfDraw.WmfLineTo.class),
    moveTo(0x0214, HwmfDraw.WmfMoveTo.class),
    offsetClipRgn(0x0220, HwmfWindowing.WmfOffsetClipRgn.class),
    fillRegion(0x0228, HwmfFill.WmfFillRegion.class),
    setMapperFlags(0x0231, HwmfMisc.WmfSetMapperFlags.class),
    selectPalette(0x0234, HwmfPalette.WmfSelectPalette.class),
    polygon(0x0324, HwmfDraw.WmfPolygon.class),
    polyline(0x0325, HwmfDraw.WmfPolyline.class),
    setTextJustification(0x020a, HwmfText.WmfSetTextJustification.class),
    setWindowOrg(0x020b, HwmfWindowing.WmfSetWindowOrg.class),
    setWindowExt(0x020c, HwmfWindowing.WmfSetWindowExt.class),
    setViewportOrg(0x020d, HwmfWindowing.WmfSetViewportOrg.class),
    setViewportExt(0x020e, HwmfWindowing.WmfSetViewportExt.class),
    offsetWindowOrg(0x020f, HwmfWindowing.WmfOffsetWindowOrg.class),
    scaleWindowExt(0x0410, HwmfWindowing.WmfScaleWindowExt.class),
    scaleViewportExt(0x0412, HwmfWindowing.WmfScaleViewportExt.class), 
    excludeClipRect(0x0415, HwmfWindowing.WmfExcludeClipRect.class),
    intersectClipRect(0x0416, HwmfWindowing.WmfIntersectClipRect.class),
    ellipse(0x0418, HwmfDraw.WmfEllipse.class),
    floodFill(0x0419, HwmfFill.WmfFloodFill.class),
    frameRegion(0x0429, HwmfDraw.WmfFrameRegion.class),
    animatePalette(0x0436, HwmfPalette.WmfAnimatePalette.class),
    textOut(0x0521, HwmfText.WmfTextOut.class),
    polyPolygon(0x0538, HwmfDraw.WmfPolyPolygon.class),
    extFloodFill(0x0548, HwmfFill.WmfExtFloodFill.class),
    rectangle(0x041b, HwmfDraw.WmfRectangle.class),
    setPixel(0x041f, HwmfDraw.WmfSetPixel.class),
    roundRect(0x061c, HwmfDraw.WmfRoundRect.class),
    patBlt(0x061d, HwmfFill.WmfPatBlt.class),
    saveDc(0x001e, HwmfMisc.WmfSaveDc.class),
    pie(0x081a, HwmfDraw.WmfPie.class),
    stretchBlt(0x0b23, HwmfFill.WmfStretchBlt.class),
    escape(0x0626, HwmfEscape.class),
    invertRegion(0x012a, HwmfFill.WmfInvertRegion.class),
    paintRegion(0x012b, HwmfFill.WmfPaintRegion.class),
    selectClipRegion(0x012c, HwmfWindowing.WmfSelectClipRegion.class),
    selectObject(0x012d, HwmfDraw.WmfSelectObject.class),
    setTextAlign(0x012e, HwmfText.WmfSetTextAlign.class),
    arc(0x0817, HwmfDraw.WmfArc.class),
    chord(0x0830, HwmfDraw.WmfChord.class),
    bitBlt(0x0922, HwmfFill.WmfBitBlt.class),
    extTextOut(0x0a32, HwmfText.WmfExtTextOut.class),
    setDibToDev(0x0d33, HwmfFill.WmfSetDibToDev.class),
    dibBitBlt(0x0940, HwmfFill.WmfDibBitBlt.class),
    dibStretchBlt(0x0b41, HwmfFill.WmfDibStretchBlt.class),
    stretchDib(0x0f43, HwmfFill.WmfStretchDib.class),
    deleteObject(0x01f0, HwmfMisc.WmfDeleteObject.class),
    createPalette(0x00f7, HwmfPalette.WmfCreatePalette.class),
    createPatternBrush(0x01f9, HwmfMisc.WmfCreatePatternBrush.class),
    createPenIndirect(0x02fa, HwmfMisc.WmfCreatePenIndirect.class),
    createFontIndirect(0x02fb, HwmfText.WmfCreateFontIndirect.class),
    createBrushIndirect(0x02fc, HwmfMisc.WmfCreateBrushIndirect.class),
    createRegion(0x06ff, HwmfWindowing.WmfCreateRegion.class);
    
    public int id;
    public Class<? extends HwmfRecord> clazz;
    
    HwmfRecordType(int id, Class<? extends HwmfRecord> clazz) {
        this.id = id;
        this.clazz = clazz;
    }
    
    public static HwmfRecordType getById(int id) {
        for (HwmfRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
