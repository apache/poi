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
     eof(0x0000, null)
    ,animatePalette(0x0436, HwmfPalette.WmfAnimatePalette.class)
    ,arc(0x0817, HwmfDraw.WmfArc.class)
    ,bitBlt(0x0922, HwmfFill.WmfBitBlt.class)
    ,chord(0x0830, HwmfDraw.WmfChord.class)
    ,createBrushIndirect(0x02fc, HwmfMisc.WmfCreateBrushIndirect.class)
    ,createFontIndirect(0x02fb, HwmfText.WmfCreateFontIndirect.class)
    ,createPalette(0x00f7, HwmfPalette.WmfCreatePalette.class)
    ,createPatternBrush(0x01f9, HwmfMisc.WmfCreatePatternBrush.class)
    ,createPenIndirect(0x02fa, HwmfMisc.WmfCreatePenIndirect.class)
    ,createRegion(0x06ff, HwmfWindowing.WmfCreateRegion.class)
    ,deleteObject(0x01f0, HwmfMisc.WmfDeleteObject.class)
    ,dibBitBlt(0x0940, HwmfFill.WmfDibBitBlt.class)
    ,dibCreatePatternBrush(0x0142, HwmfMisc.WmfDibCreatePatternBrush.class)
    ,dibStretchBlt(0x0b41, HwmfFill.WmfDibStretchBlt.class)
    ,ellipse(0x0418, HwmfDraw.WmfEllipse.class)
    ,escape(0x0626, HwmfEscape.class)
    ,excludeClipRect(0x0415, HwmfWindowing.WmfExcludeClipRect.class)
    ,extFloodFill(0x0548, HwmfFill.WmfExtFloodFill.class)
    ,extTextOut(0x0a32, HwmfText.WmfExtTextOut.class)
    ,fillRegion(0x0228, HwmfFill.WmfFillRegion.class)
    ,floodFill(0x0419, HwmfFill.WmfFloodFill.class)
    ,frameRegion(0x0429, HwmfDraw.WmfFrameRegion.class)
    ,intersectClipRect(0x0416, HwmfWindowing.WmfIntersectClipRect.class)
    ,invertRegion(0x012a, HwmfFill.WmfInvertRegion.class)
    ,lineTo(0x0213, HwmfDraw.WmfLineTo.class)
    ,moveTo(0x0214, HwmfDraw.WmfMoveTo.class)
    ,offsetClipRgn(0x0220, HwmfWindowing.WmfOffsetClipRgn.class)
    ,offsetViewportOrg(0x0211, HwmfWindowing.WmfOffsetViewportOrg.class)
    ,offsetWindowOrg(0x020f, HwmfWindowing.WmfOffsetWindowOrg.class)
    ,paintRegion(0x012b, HwmfFill.WmfPaintRegion.class)
    ,patBlt(0x061d, HwmfFill.WmfPatBlt.class)
    ,pie(0x081a, HwmfDraw.WmfPie.class)
    ,polygon(0x0324, HwmfDraw.WmfPolygon.class)
    ,polyline(0x0325, HwmfDraw.WmfPolyline.class)
    ,polyPolygon(0x0538, HwmfDraw.WmfPolyPolygon.class)
    ,realizePalette(0x0035, HwmfPalette.WmfRealizePalette.class)
    ,rectangle(0x041b, HwmfDraw.WmfRectangle.class)
    ,resizePalette(0x0139, HwmfPalette.WmfResizePalette.class)
    ,restoreDc(0x0127, HwmfMisc.WmfRestoreDc.class)
    ,roundRect(0x061c, HwmfDraw.WmfRoundRect.class)
    ,saveDc(0x001e, HwmfMisc.WmfSaveDc.class)
    ,scaleViewportExt(0x0412, HwmfWindowing.WmfScaleViewportExt.class) 
    ,scaleWindowExt(0x0410, HwmfWindowing.WmfScaleWindowExt.class)
    ,selectClipRegion(0x012c, HwmfWindowing.WmfSelectClipRegion.class)
    ,selectObject(0x012d, HwmfDraw.WmfSelectObject.class)
    ,selectPalette(0x0234, HwmfPalette.WmfSelectPalette.class)
    ,setBkColor(0x0201, HwmfMisc.WmfSetBkColor.class)
    ,setBkMode(0x0102, HwmfMisc.WmfSetBkMode.class)
    ,setDibToDev(0x0d33, HwmfFill.WmfSetDibToDev.class)
    ,setLayout(0x0149, HwmfMisc.WmfSetLayout.class)
    ,setMapMode(0x0103, HwmfMisc.WmfSetMapMode.class)
    ,setMapperFlags(0x0231, HwmfMisc.WmfSetMapperFlags.class)
    ,setPalEntries(0x0037, HwmfPalette.WmfSetPaletteEntries.class)
    ,setPixel(0x041f, HwmfDraw.WmfSetPixel.class)
    ,setPolyFillMode(0x0106, HwmfFill.WmfSetPolyfillMode.class)
    ,setRelabs(0x0105, HwmfMisc.WmfSetRelabs.class)
    ,setRop2(0x0104, HwmfMisc.WmfSetRop2.class)
    ,setStretchBltMode(0x0107, HwmfMisc.WmfSetStretchBltMode.class)
    ,setTextAlign(0x012e, HwmfText.WmfSetTextAlign.class)
    ,setTextCharExtra(0x0108, HwmfText.WmfSetTextCharExtra.class)
    ,setTextColor(0x0209, HwmfText.WmfSetTextColor.class)
    ,setTextJustification(0x020a, HwmfText.WmfSetTextJustification.class)
    ,setViewportExt(0x020e, HwmfWindowing.WmfSetViewportExt.class)
    ,setViewportOrg(0x020d, HwmfWindowing.WmfSetViewportOrg.class)
    ,setWindowExt(0x020c, HwmfWindowing.WmfSetWindowExt.class)
    ,setWindowOrg(0x020b, HwmfWindowing.WmfSetWindowOrg.class)
    ,stretchBlt(0x0b23, HwmfFill.WmfStretchBlt.class)
    ,stretchDib(0x0f43, HwmfFill.WmfStretchDib.class)
    ,textOut(0x0521, HwmfText.WmfTextOut.class)
    ;
    
    public final int id;
    public final Class<? extends HwmfRecord> clazz;
    
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
