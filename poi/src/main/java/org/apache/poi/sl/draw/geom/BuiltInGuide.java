/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.sl.draw.geom;

import java.awt.geom.Rectangle2D;

/* package */ enum BuiltInGuide implements Formula {
    _3cd4, _3cd8, _5cd8, _7cd8, _b, _cd2, _cd4, _cd8, _hc, _h, _hd2, _hd3, _hd4, _hd5, _hd6, _hd8,
    _l, _ls, _r, _ss, _ssd2, _ssd4, _ssd6, _ssd8, _ssd16, _ssd32, _t, _vc,
    _w, _wd2, _wd3, _wd4, _wd5, _wd6, _wd8, _wd10, _wd32;
    
    public String getName() {
        return name().substring(1);
    }
    
    @Override
    public double evaluate(Context ctx) {
        Rectangle2D anchor = ctx.getShapeAnchor();
        double height = anchor.getHeight(), width = anchor.getWidth(), ss = Math.min(width, height);
        return switch (this) {
            case _3cd4 ->
                // 3 circles div 4: 3 x 360 / 4 = 270
                    270 * OOXML_DEGREE;
            case _3cd8 ->
                // 3 circles div 8: 3 x 360 / 8 = 135
                    135 * OOXML_DEGREE;
            case _5cd8 ->
                // 5 circles div 8: 5 x 360 / 8 = 225
                    225 * OOXML_DEGREE;
            case _7cd8 ->
                // 7 circles div 8: 7 x 360 / 8 = 315
                    315 * OOXML_DEGREE;
            case _t ->
                // top
                    anchor.getY();
            case _b ->
                // bottom
                    anchor.getMaxY();
            case _l ->
                // left
                    anchor.getX();
            case _r ->
                // right
                    anchor.getMaxX();
            case _cd2 ->
                // circle div 2: 360 / 2 = 180
                    180 * OOXML_DEGREE;
            case _cd4 ->
                // circle div 4: 360 / 4 = 90
                    90 * OOXML_DEGREE;
            case _cd8 ->
                // circle div 8: 360 / 8 = 45
                    45 * OOXML_DEGREE;
            case _hc ->
                // horizontal center
                    anchor.getCenterX();
            case _h ->
                // height
                    height;
            case _hd2 ->
                // height div 2
                    height / 2.;
            case _hd3 ->
                // height div 3
                    height / 3.;
            case _hd4 ->
                // height div 4
                    height / 4.;
            case _hd5 ->
                // height div 5
                    height / 5.;
            case _hd6 ->
                // height div 6
                    height / 6.;
            case _hd8 ->
                // height div 8
                    height / 8.;
            case _ls ->
                // long side
                    Math.max(width, height);
            case _ss ->
                // short side
                    ss;
            case _ssd2 ->
                // short side div 2
                    ss / 2.;
            case _ssd4 ->
                // short side div 4
                    ss / 4.;
            case _ssd6 ->
                // short side div 6
                    ss / 6.;
            case _ssd8 ->
                // short side div 8
                    ss / 8.;
            case _ssd16 ->
                // short side div 16
                    ss / 16.;
            case _ssd32 ->
                // short side div 32
                    ss / 32.;
            case _vc ->
                // vertical center
                    anchor.getCenterY();
            case _w ->
                // width
                    width;
            case _wd2 ->
                // width div 2
                    width / 2.;
            case _wd3 ->
                // width div 3
                    width / 3.;
            case _wd4 ->
                // width div 4
                    width / 4.;
            case _wd5 ->
                // width div 5
                    width / 5.;
            case _wd6 ->
                // width div 6
                    width / 6.;
            case _wd8 ->
                // width div 8
                    width / 8.;
            case _wd10 ->
                // width div 10
                    width / 10.;
            case _wd32 ->
                // width div 32
                    width / 32.;
            default -> 0;
        };
    }
}