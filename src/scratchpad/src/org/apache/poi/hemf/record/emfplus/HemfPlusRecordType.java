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

package org.apache.poi.hemf.record.emfplus;

import java.util.function.Supplier;

import org.apache.poi.util.Internal;

@Internal
public enum HemfPlusRecordType {
    header(0x4001, HemfPlusHeader::new),
    eof(0x4002, UnimplementedHemfPlusRecord::new),
    comment(0x4003, UnimplementedHemfPlusRecord::new),
    getDC(0x4004, UnimplementedHemfPlusRecord::new),
    multiFormatStart(0x4005, UnimplementedHemfPlusRecord::new),
    multiFormatSection(0x4006, UnimplementedHemfPlusRecord::new),
    multiFormatEnd(0x4007, UnimplementedHemfPlusRecord::new),
    object(0x4008, UnimplementedHemfPlusRecord::new),
    clear(0x4009, UnimplementedHemfPlusRecord::new),
    fillRects(0x400A, UnimplementedHemfPlusRecord::new),
    drawRects(0x400B, UnimplementedHemfPlusRecord::new),
    fillPolygon(0x400C, UnimplementedHemfPlusRecord::new),
    drawLines(0x400D, UnimplementedHemfPlusRecord::new),
    fillEllipse(0x400E, UnimplementedHemfPlusRecord::new),
    drawEllipse(0x400F, UnimplementedHemfPlusRecord::new),
    fillPie(0x4010, UnimplementedHemfPlusRecord::new),
    drawPie(0x4011, UnimplementedHemfPlusRecord::new),
    drawArc(0x4012, UnimplementedHemfPlusRecord::new),
    fillRegion(0x4013, UnimplementedHemfPlusRecord::new),
    fillPath(0x4014, UnimplementedHemfPlusRecord::new),
    drawPath(0x4015, UnimplementedHemfPlusRecord::new),
    fillClosedCurve(0x4016, UnimplementedHemfPlusRecord::new),
    drawClosedCurve(0x4017, UnimplementedHemfPlusRecord::new),
    drawCurve(0x4018, UnimplementedHemfPlusRecord::new),
    drawBeziers(0x4019, UnimplementedHemfPlusRecord::new),
    drawImage(0x401A, UnimplementedHemfPlusRecord::new),
    drawImagePoints(0x401B, UnimplementedHemfPlusRecord::new),
    drawString(0x401C, UnimplementedHemfPlusRecord::new),
    setRenderingOrigin(0x401D, UnimplementedHemfPlusRecord::new),
    setAntiAliasMode(0x401E, UnimplementedHemfPlusRecord::new),
    setTextRenderingHint(0x401F, UnimplementedHemfPlusRecord::new),
    setTextContrast(0x4020, UnimplementedHemfPlusRecord::new),
    setInterpolationMode(0x4021, UnimplementedHemfPlusRecord::new),
    setPixelOffsetMode(0x4022, UnimplementedHemfPlusRecord::new),
    setComositingMode(0x4023, UnimplementedHemfPlusRecord::new),
    setCompositingQuality(0x4024, UnimplementedHemfPlusRecord::new),
    save(0x4025, UnimplementedHemfPlusRecord::new),
    restore(0x4026, UnimplementedHemfPlusRecord::new),
    beginContainer(0x4027, UnimplementedHemfPlusRecord::new),
    beginContainerNoParams(0x428, UnimplementedHemfPlusRecord::new),
    endContainer(0x4029, UnimplementedHemfPlusRecord::new),
    setWorldTransform(0x402A, UnimplementedHemfPlusRecord::new),
    resetWorldTransform(0x402B, UnimplementedHemfPlusRecord::new),
    multiplyWorldTransform(0x402C, UnimplementedHemfPlusRecord::new),
    translateWorldTransform(0x402D, UnimplementedHemfPlusRecord::new),
    scaleWorldTransform(0x402E, UnimplementedHemfPlusRecord::new),
    rotateWorldTransform(0x402F, UnimplementedHemfPlusRecord::new),
    setPageTransform(0x4030, UnimplementedHemfPlusRecord::new),
    resetClip(0x4031, UnimplementedHemfPlusRecord::new),
    setClipRect(0x4032, UnimplementedHemfPlusRecord::new),
    setClipRegion(0x4033, UnimplementedHemfPlusRecord::new),
    setClipPath(0x4034, UnimplementedHemfPlusRecord::new),
    offsetClip(0x4035, UnimplementedHemfPlusRecord::new),
    drawDriverstring(0x4036, UnimplementedHemfPlusRecord::new),
    strokeFillPath(0x4037, UnimplementedHemfPlusRecord::new),
    serializableObject(0x4038, UnimplementedHemfPlusRecord::new),
    setTSGraphics(0x4039, UnimplementedHemfPlusRecord::new),
    setTSClip(0x403A, UnimplementedHemfPlusRecord::new);


    public final long id;
    public final Supplier<? extends HemfPlusRecord> constructor;

    HemfPlusRecordType(long id, Supplier<? extends HemfPlusRecord> constructor) {
        this.id = id;
        this.constructor = constructor;
    }

    public static HemfPlusRecordType getById(long id) {
        for (HemfPlusRecordType wrt : values()) {
            if (wrt.id == id) return wrt;
        }
        return null;
    }
}
