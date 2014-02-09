package org.apache.poi.hwmf.record;

public enum WmfRecordType {
    eof(0x0000, null),
    realizePalette(0x0035, WmfPalette.WmfRealizePalette.class),
    setPalEntries(0x0037, WmfPalette.WmfSetPaletteEntries.class),
    setBkMode(0x0102, WmfSetBkMode.class),
    setMapMode(0x0103, WmfSetMapMode.class),
    setRop2(0x0104, WmfSetRop2.class),
    setRelabs(0x0105, WmfNoArg.WmfSetRelabs.class),
    setPolyFillMode(0x0106, WmfFill.WmfSetPolyfillMode.class),
    setStretchBltMode(0x0107, WmfSetStretchBltMode.class),
    setTextCharExtra(0x0108, WmfText.WmfSetTextCharExtra.class),
    restoreDc(0x0127, WmfRestoreDc.class),
    resizePalette(0x0139, WmfPalette.WmfResizePalette.class),
    dibCreatePatternBrush(0x0142, WmfDibCreatePatternBrush.class),
    setLayout(0x0149, WmfSetLayout.class),
    setBkColor(0x0201, WmfSetBkColor.class),
    setTextColor(0x0209, WmfText.WmfSetTextColor.class),
    offsetViewportOrg(0x0211, WmfWindowing.WmfOffsetViewportOrg.class),
    lineTo(0x0213, WmfDraw.WmfLineTo.class),
    moveTo(0x0214, WmfMoveTo.class),
    offsetClipRgn(0x0220, WmfWindowing.WmfOffsetClipRgn.class),
    fillRegion(0x0228, WmfFill.WmfFillRegion.class),
    setMapperFlags(0x0231, WmfSetMapperFlags.class),
    selectPalette(0x0234, WmfPalette.WmfSelectPalette.class),
    polygon(0x0324, WmfDraw.WmfPolygon.class),
    polyline(0x0325, WmfDraw.WmfPolyline.class),
    setTextJustification(0x020a, WmfText.WmfSetTextJustification.class),
    setWindowOrg(0x020b, WmfWindowing.WmfSetWindowOrg.class),
    setWindowExt(0x020c, WmfWindowing.WmfSetWindowExt.class),
    setViewportOrg(0x020d, WmfWindowing.WmfSetViewportOrg.class),
    setViewportExt(0x020e, WmfWindowing.WmfSetViewportExt.class),
    offsetWindowOrg(0x020f, WmfWindowing.WmfOffsetWindowOrg.class),
    scaleWindowExt(0x0410, WmfWindowing.WmfScaleWindowExt.class),
    scaleViewportExt(0x0412, WmfWindowing.WmfScaleViewportExt.class), 
    excludeClipRect(0x0415, WmfWindowing.WmfExcludeClipRect.class),
    intersectClipRect(0x0416, WmfWindowing.WmfIntersectClipRect.class),
    ellipse(0x0418, WmfDraw.WmfEllipse.class),
    floodFill(0x0419, WmfFill.WmfFloodFill.class),
    frameRegion(0x0429, WmfDraw.WmfFrameRegion.class),
    animatePalette(0x0436, WmfPalette.WmfAnimatePalette.class),
    textOut(0x0521, WmfText.WmfTextOut.class),
    polyPolygon(0x0538, WmfDraw.WmfPolyPolygon.class),
    extFloodFill(0x0548, WmfFill.WmfExtFloodFill.class),
    rectangle(0x041b, WmfDraw.WmfRectangle.class),
    setPixel(0x041f, WmfDraw.WmfSetPixel.class),
    roundRect(0x061c, WmfDraw.WmfRoundRect.class),
    patBlt(0x061d, WmfFill.WmfPatBlt.class),
    saveDc(0x001e, WmfNoArg.WmfSaveDc.class),
    pie(0x081a, WmfDraw.WmfPie.class),
    stretchBlt(0x0b23, WmfFill.WmfStretchBlt.class),
    escape(0x0626, WmfEscape.class),
    invertRegion(0x012a, WmfFill.WmfInvertRegion.class),
    paintRegion(0x012b, WmfFill.WmfPaintRegion.class),
    selectClipRegion(0x012c, WmfWindowing.WmfSelectClipRegion.class),
    selectObject(0x012d, WmfDraw.WmfSelectObject.class),
    setTextAlign(0x012e, WmfText.WmfSetTextAlign.class),
    arc(0x0817, WmfDraw.WmfArc.class),
    chord(0x0830, WmfDraw.WmfChord.class),
    bitBlt(0x0922, WmfFill.WmfBitBlt.class),
    extTextOut(0x0a32, WmfText.WmfExtTextOut.class),
    setDibToDev(0x0d33, WmfBitmap.WmfSetDibToDev.class),
    dibBitBlt(0x0940, null),
    dibStretchBlt(0x0b41, null),
    stretchDib(0x0f43, null),
    deleteObject(0x01f0, null),
    createPalette(0x00f7, WmfPalette.WmfCreatePalette.class),
    createPatternBrush(0x01f9, null),
    createPenIndirect(0x02fa, null),
    createFontIndirect(0x02fb, null),
    createBrushIndirect(0x02fc, null),
    createRegion(0x06ff, null);
    
    public int id;
    public Class<? extends WmfRecord> clazz;
    
    WmfRecordType(int id, Class<? extends WmfRecord> clazz) {
        this.id = id;
        this.clazz = clazz;
    }
    
    public static WmfRecordType getById(int id) {
        for (WmfRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
