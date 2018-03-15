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

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.sl.draw.binding.CTAdjPoint2D;
import org.apache.poi.sl.draw.binding.CTPath2D;
import org.apache.poi.sl.draw.binding.CTPath2DArcTo;
import org.apache.poi.sl.draw.binding.CTPath2DClose;
import org.apache.poi.sl.draw.binding.CTPath2DCubicBezierTo;
import org.apache.poi.sl.draw.binding.CTPath2DLineTo;
import org.apache.poi.sl.draw.binding.CTPath2DMoveTo;
import org.apache.poi.sl.draw.binding.CTPath2DQuadBezierTo;
import org.apache.poi.sl.usermodel.PaintStyle.PaintModifier;

/**
 * Specifies a creation path consisting of a series of moves, lines and curves
 * that when combined forms a geometric shape
 */
public class Path {

    private final List<PathCommand> commands;
    PaintModifier _fill;
    boolean _stroke;
    long _w, _h;

    public Path(){
        this(true, true);
    }

    public Path(boolean fill, boolean stroke){
        commands = new ArrayList<>();
        _w = -1;
        _h = -1;
        _fill = (fill) ? PaintModifier.NORM : PaintModifier.NONE;
        _stroke = stroke;
    }

    public Path(CTPath2D spPath){
        switch (spPath.getFill()) {
            case NONE: _fill = PaintModifier.NONE; break;
            case DARKEN: _fill = PaintModifier.DARKEN; break;
            case DARKEN_LESS: _fill = PaintModifier.DARKEN_LESS; break;
            case LIGHTEN: _fill = PaintModifier.LIGHTEN; break;
            case LIGHTEN_LESS: _fill = PaintModifier.LIGHTEN_LESS; break;
            default:
            case NORM: _fill = PaintModifier.NORM; break;
        }
        _stroke = spPath.isStroke();
        _w = spPath.isSetW() ? spPath.getW() : -1;
        _h = spPath.isSetH() ? spPath.getH() : -1;

        commands = new ArrayList<>();

        for(Object ch : spPath.getCloseOrMoveToOrLnTo()){
            if(ch instanceof CTPath2DMoveTo){
                CTAdjPoint2D pt = ((CTPath2DMoveTo)ch).getPt();
                commands.add(new MoveToCommand(pt));
            } else if (ch instanceof CTPath2DLineTo){
                CTAdjPoint2D pt = ((CTPath2DLineTo)ch).getPt();
                commands.add(new LineToCommand(pt));
            } else if (ch instanceof CTPath2DArcTo){
                CTPath2DArcTo arc = (CTPath2DArcTo)ch;
                commands.add(new ArcToCommand(arc));
            } else if (ch instanceof CTPath2DQuadBezierTo){
                CTPath2DQuadBezierTo bez = ((CTPath2DQuadBezierTo)ch);
                CTAdjPoint2D pt1 = bez.getPt().get(0);
                CTAdjPoint2D pt2 = bez.getPt().get(1);
                commands.add(new QuadToCommand(pt1, pt2));
            } else if (ch instanceof CTPath2DCubicBezierTo){
                CTPath2DCubicBezierTo bez = ((CTPath2DCubicBezierTo)ch);
                CTAdjPoint2D pt1 = bez.getPt().get(0);
                CTAdjPoint2D pt2 = bez.getPt().get(1);
                CTAdjPoint2D pt3 = bez.getPt().get(2);
                commands.add(new CurveToCommand(pt1, pt2, pt3));
            } else if (ch instanceof CTPath2DClose){
                commands.add(new ClosePathCommand());
            }  else {
                throw new IllegalStateException("Unsupported path segment: " + ch);
            }
        }
    }

    public void addCommand(PathCommand cmd){
        commands.add(cmd);
    }

    /**
     * Convert the internal represenation to java.awt.geom.Path2D
     */
    public Path2D.Double getPath(Context ctx) {
        Path2D.Double path = new Path2D.Double();
        for(PathCommand cmd : commands) {
            cmd.execute(path, ctx);
        }
        return path;
    }

    public boolean isStroked(){
        return _stroke;
    }

    public boolean isFilled(){
        return _fill != PaintModifier.NONE;
    }

    public PaintModifier getFill() {
        return _fill;
    }

    public long getW(){
    	return _w;
    }

    public long getH(){
    	return _h;
    }
}
