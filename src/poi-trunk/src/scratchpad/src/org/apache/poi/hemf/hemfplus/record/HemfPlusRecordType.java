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

package org.apache.poi.hemf.hemfplus.record;

import org.apache.poi.util.Internal;

@Internal
public enum HemfPlusRecordType {
    header(0x4001, HemfPlusHeader.class),
    endOfFile(0x4002, UnimplementedHemfPlusRecord.class),
    comment(0x4003, UnimplementedHemfPlusRecord.class),
    getDC(0x4004, UnimplementedHemfPlusRecord.class),
    multiFormatStart(0x4005, UnimplementedHemfPlusRecord.class),
    multiFormatSection(0x4006, UnimplementedHemfPlusRecord.class),
    multiFormatEnd(0x4007, UnimplementedHemfPlusRecord.class),
    object(0x4008, UnimplementedHemfPlusRecord.class),
    clear(0x4009, UnimplementedHemfPlusRecord.class),
    fillRects(0x400A, UnimplementedHemfPlusRecord.class),
    drawRects(0x400B, UnimplementedHemfPlusRecord.class),
    fillPolygon(0x400C, UnimplementedHemfPlusRecord.class),
    drawLines(0x400D, UnimplementedHemfPlusRecord.class),
    fillEllipse(0x400E, UnimplementedHemfPlusRecord.class),
    drawEllipse(0x400F, UnimplementedHemfPlusRecord.class),
    fillPie(0x4010, UnimplementedHemfPlusRecord.class),
    drawPie(0x4011, UnimplementedHemfPlusRecord.class),
    drawArc(0x4012, UnimplementedHemfPlusRecord.class),
    fillRegion(0x4013, UnimplementedHemfPlusRecord.class),
    fillPath(0x4014, UnimplementedHemfPlusRecord.class),
    drawPath(0x4015, UnimplementedHemfPlusRecord.class),
    fillClosedCurve(0x4016, UnimplementedHemfPlusRecord.class),
    drawClosedCurve(0x4017, UnimplementedHemfPlusRecord.class),
    drawCurve(0x4018, UnimplementedHemfPlusRecord.class),
    drawBeziers(0x4019, UnimplementedHemfPlusRecord.class),
    drawImage(0x401A, UnimplementedHemfPlusRecord.class),
    drawImagePoints(0x401B, UnimplementedHemfPlusRecord.class),
    drawString(0x401C, UnimplementedHemfPlusRecord.class),
    setRenderingOrigin(0x401D, UnimplementedHemfPlusRecord.class),
    setAntiAliasMode(0x401E, UnimplementedHemfPlusRecord.class),
    setTextRenderingHint(0x401F, UnimplementedHemfPlusRecord.class),
    setTextContrast(0x4020, UnimplementedHemfPlusRecord.class),
    setInterpolationMode(0x4021, UnimplementedHemfPlusRecord.class),
    setPixelOffsetMode(0x4022, UnimplementedHemfPlusRecord.class),
    setComositingMode(0x4023, UnimplementedHemfPlusRecord.class),
    setCompositingQuality(0x4024, UnimplementedHemfPlusRecord.class),
    save(0x4025, UnimplementedHemfPlusRecord.class),
    restore(0x4026, UnimplementedHemfPlusRecord.class),
    beginContainer(0x4027, UnimplementedHemfPlusRecord.class),
    beginContainerNoParams(0x428, UnimplementedHemfPlusRecord.class),
    endContainer(0x4029, UnimplementedHemfPlusRecord.class),
    setWorldTransform(0x402A, UnimplementedHemfPlusRecord.class),
    resetWorldTransform(0x402B, UnimplementedHemfPlusRecord.class),
    multiplyWorldTransform(0x402C, UnimplementedHemfPlusRecord.class),
    translateWorldTransform(0x402D, UnimplementedHemfPlusRecord.class),
    scaleWorldTransform(0x402E, UnimplementedHemfPlusRecord.class),
    rotateWorldTransform(0x402F, UnimplementedHemfPlusRecord.class),
    setPageTransform(0x4030, UnimplementedHemfPlusRecord.class),
    resetClip(0x4031, UnimplementedHemfPlusRecord.class),
    setClipRect(0x4032, UnimplementedHemfPlusRecord.class),
    setClipRegion(0x4033, UnimplementedHemfPlusRecord.class),
    setClipPath(0x4034, UnimplementedHemfPlusRecord.class),
    offsetClip(0x4035, UnimplementedHemfPlusRecord.class),
    drawDriverstring(0x4036, UnimplementedHemfPlusRecord.class),
    strokeFillPath(0x4037, UnimplementedHemfPlusRecord.class),
    serializableObject(0x4038, UnimplementedHemfPlusRecord.class),
    setTSGraphics(0x4039, UnimplementedHemfPlusRecord.class),
    setTSClip(0x403A, UnimplementedHemfPlusRecord.class);

    public final long id;
    public final Class<? extends HemfPlusRecord> clazz;

    HemfPlusRecordType(long id, Class<? extends HemfPlusRecord> clazz) {
        this.id = id;
        this.clazz = clazz;
    }

    public static HemfPlusRecordType getById(long id) {
        for (HemfPlusRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
