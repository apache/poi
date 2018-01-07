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

package org.apache.poi.hemf.record;

import org.apache.poi.util.Internal;

@Internal
public enum HemfRecordType {

    header(0x00000001, UnimplementedHemfRecord.class),
    polybeizer(0x00000002, UnimplementedHemfRecord.class),
    polygon(0x00000003, UnimplementedHemfRecord.class),
    polyline(0x00000004, UnimplementedHemfRecord.class),
    polybezierto(0x00000005, UnimplementedHemfRecord.class),
    polylineto(0x00000006, UnimplementedHemfRecord.class),
    polypolyline(0x00000007, UnimplementedHemfRecord.class),
    polypolygon(0x00000008, UnimplementedHemfRecord.class),
    setwindowextex(0x00000009, UnimplementedHemfRecord.class),
    setwindoworgex(0x0000000A, UnimplementedHemfRecord.class),
    setviewportextex(0x0000000B, UnimplementedHemfRecord.class),
    setviewportorgex(0x0000000C, UnimplementedHemfRecord.class),
    setbrushorgex(0x0000000D, UnimplementedHemfRecord.class),
    eof(0x0000000E, UnimplementedHemfRecord.class),
    setpixelv(0x0000000F, UnimplementedHemfRecord.class),
    setmapperflags(0x00000010, UnimplementedHemfRecord.class),
    setmapmode(0x00000011, UnimplementedHemfRecord.class),
    setbkmode(0x00000012, UnimplementedHemfRecord.class),
    setpolyfillmode(0x00000013, UnimplementedHemfRecord.class),
    setrop2(0x00000014, UnimplementedHemfRecord.class),
    setstretchbltmode(0x00000015, UnimplementedHemfRecord.class),
    settextalign(0x00000016, HemfText.SetTextAlign.class),
    setcoloradjustment(0x00000017, UnimplementedHemfRecord.class),
    settextcolor(0x00000018, HemfText.SetTextColor.class),
    setbkcolor(0x00000019, UnimplementedHemfRecord.class),
    setoffsetcliprgn(0x0000001A, UnimplementedHemfRecord.class),
    setmovetoex(0x0000001B, UnimplementedHemfRecord.class),
    setmetargn(0x0000001C, UnimplementedHemfRecord.class),
    setexcludecliprect(0x0000001D, UnimplementedHemfRecord.class),
    setintersectcliprect(0x0000001E, UnimplementedHemfRecord.class),
    scaleviewportextex(0x0000001F, UnimplementedHemfRecord.class),
    scalewindowextex(0x00000020, UnimplementedHemfRecord.class),
    savedc(0x00000021, UnimplementedHemfRecord.class),
    restoredc(0x00000022, UnimplementedHemfRecord.class),
    setworldtransform(0x00000023, UnimplementedHemfRecord.class),
    modifyworldtransform(0x00000024, UnimplementedHemfRecord.class),
    selectobject(0x00000025, UnimplementedHemfRecord.class),
    createpen(0x00000026, UnimplementedHemfRecord.class),
    createbrushindirect(0x00000027, UnimplementedHemfRecord.class),
    deleteobject(0x00000028, UnimplementedHemfRecord.class),
    anglearc(0x00000029, UnimplementedHemfRecord.class),
    ellipse(0x0000002A, UnimplementedHemfRecord.class),
    rectangle(0x0000002B, UnimplementedHemfRecord.class),
    roundirect(0x0000002C, UnimplementedHemfRecord.class),
    arc(0x0000002D, UnimplementedHemfRecord.class),
    chord(0x0000002E, UnimplementedHemfRecord.class),
    pie(0x0000002F, UnimplementedHemfRecord.class),
    selectpalette(0x00000030, UnimplementedHemfRecord.class),
    createpalette(0x00000031, UnimplementedHemfRecord.class),
    setpaletteentries(0x00000032, UnimplementedHemfRecord.class),
    resizepalette(0x00000033, UnimplementedHemfRecord.class),
    realizepalette(0x0000034, UnimplementedHemfRecord.class),
    extfloodfill(0x00000035, UnimplementedHemfRecord.class),
    lineto(0x00000036, UnimplementedHemfRecord.class),
    arcto(0x00000037, UnimplementedHemfRecord.class),
    polydraw(0x00000038, UnimplementedHemfRecord.class),
    setarcdirection(0x00000039, UnimplementedHemfRecord.class),
    setmiterlimit(0x0000003A, UnimplementedHemfRecord.class),
    beginpath(0x0000003B, UnimplementedHemfRecord.class),
    endpath(0x0000003C, UnimplementedHemfRecord.class),
    closefigure(0x0000003D, UnimplementedHemfRecord.class),
    fillpath(0x0000003E, UnimplementedHemfRecord.class),
    strokeandfillpath(0x0000003F, UnimplementedHemfRecord.class),
    strokepath(0x00000040, UnimplementedHemfRecord.class),
    flattenpath(0x00000041, UnimplementedHemfRecord.class),
    widenpath(0x00000042, UnimplementedHemfRecord.class),
    selectclippath(0x00000043, UnimplementedHemfRecord.class),
    abortpath(0x00000044, UnimplementedHemfRecord.class), //no 45?!
    comment(0x00000046, HemfCommentRecord.class),
    fillrgn(0x00000047, UnimplementedHemfRecord.class),
    framergn(0x00000048, UnimplementedHemfRecord.class),
    invertrgn(0x00000049, UnimplementedHemfRecord.class),
    paintrgn(0x0000004A, UnimplementedHemfRecord.class),
    extselectciprrgn(0x0000004B, UnimplementedHemfRecord.class),
    bitblt(0x0000004C, UnimplementedHemfRecord.class),
    stretchblt(0x0000004D, UnimplementedHemfRecord.class),
    maskblt(0x0000004E, UnimplementedHemfRecord.class),
    plgblt(0x0000004F, UnimplementedHemfRecord.class),
    setbitstodevice(0x00000050, UnimplementedHemfRecord.class),
    stretchdibits(0x00000051, UnimplementedHemfRecord.class),
    extcreatefontindirectw(0x00000052, HemfText.ExtCreateFontIndirectW.class),
    exttextouta(0x00000053, HemfText.ExtTextOutA.class),
    exttextoutw(0x00000054, HemfText.ExtTextOutW.class),
    polybezier16(0x00000055, UnimplementedHemfRecord.class),
    polygon16(0x00000056, UnimplementedHemfRecord.class),
    polyline16(0x00000057, UnimplementedHemfRecord.class),
    polybezierto16(0x00000058, UnimplementedHemfRecord.class),
    polylineto16(0x00000059, UnimplementedHemfRecord.class),
    polypolyline16(0x0000005A, UnimplementedHemfRecord.class),
    polypolygon16(0x0000005B, UnimplementedHemfRecord.class),
    polydraw16(0x0000005C, UnimplementedHemfRecord.class),
    createmonobrush16(0x0000005D, UnimplementedHemfRecord.class),
    createdibpatternbrushpt(0x0000005E, UnimplementedHemfRecord.class),
    extcreatepen(0x0000005F, UnimplementedHemfRecord.class),
    polytextouta(0x00000060, HemfText.PolyTextOutA.class),
    polytextoutw(0x00000061, HemfText.PolyTextOutW.class),
    seticmmode(0x00000062, UnimplementedHemfRecord.class),
    createcolorspace(0x00000063, UnimplementedHemfRecord.class),
    setcolorspace(0x00000064, UnimplementedHemfRecord.class),
    deletecolorspace(0x00000065, UnimplementedHemfRecord.class),
    glsrecord(0x00000066, UnimplementedHemfRecord.class),
    glsboundedrecord(0x00000067, UnimplementedHemfRecord.class),
    pixelformat(0x00000068, UnimplementedHemfRecord.class),
    drawescape(0x00000069, UnimplementedHemfRecord.class),
    extescape(0x0000006A, UnimplementedHemfRecord.class),//no 6b?!
    smalltextout(0x0000006C, UnimplementedHemfRecord.class),
    forceufimapping(0x0000006D, UnimplementedHemfRecord.class),
    namedescape(0x0000006E, UnimplementedHemfRecord.class),
    colorcorrectpalette(0x0000006F, UnimplementedHemfRecord.class),
    seticmprofilea(0x00000070, UnimplementedHemfRecord.class),
    seticmprofilew(0x00000071, UnimplementedHemfRecord.class),
    alphablend(0x00000072, UnimplementedHemfRecord.class),
    setlayout(0x00000073, UnimplementedHemfRecord.class),
    transparentblt(0x00000074, UnimplementedHemfRecord.class),
    gradientfill(0x00000076, UnimplementedHemfRecord.class), //no 75?!
    setlinkdufis(0x00000077, UnimplementedHemfRecord.class),
    settextjustification(0x00000078, HemfText.SetTextJustification.class),
    colormatchtargetw(0x00000079, UnimplementedHemfRecord.class),
    createcolorspacew(0x0000007A, UnimplementedHemfRecord.class);

    public final long id;
    public final Class<? extends HemfRecord> clazz;

    HemfRecordType(long id, Class<? extends HemfRecord> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public static HemfRecordType getById(long id) {
        for (HemfRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
