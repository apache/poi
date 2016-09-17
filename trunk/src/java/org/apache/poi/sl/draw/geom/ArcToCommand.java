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

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.apache.poi.sl.draw.binding.CTPath2DArcTo;

/**
 * ArcTo command within a shape path in DrawingML:
 *
 * <arcTo wR="wr" hR="hr" stAng="stAng" swAng="swAng"/>
 *
 * Where <code>wr</code> and <code>wh</code> are the height and width radiuses
 * of the supposed circle being used to draw the arc.  This gives the circle
 * a total height of (2 * hR)  and a total width of (2 * wR)
 *
 * stAng is the <code>start</code> angle and <code></>swAng</code> is the swing angle
 *
 * @author Yegor Kozlov
 */
public class ArcToCommand implements PathCommand {
    private String hr, wr, stAng, swAng;

    ArcToCommand(CTPath2DArcTo arc){
        hr = arc.getHR().toString();
        wr = arc.getWR().toString();
        stAng = arc.getStAng().toString();
        swAng = arc.getSwAng().toString();
    }

    public void execute(Path2D.Double path, Context ctx){
        double rx = ctx.getValue(wr);
        double ry = ctx.getValue(hr);
        double start = ctx.getValue(stAng) / 60000;
        double extent = ctx.getValue(swAng) / 60000;
        Point2D pt = path.getCurrentPoint();
        double x0 = pt.getX() - rx - rx * Math.cos(Math.toRadians(start));
        double y0 = pt.getY() - ry - ry * Math.sin(Math.toRadians(start));

        Arc2D arc = new Arc2D.Double(
                         x0,
                         y0,
                         2 * rx, 2 * ry,
                         -start, -extent, 
                         Arc2D.OPEN);
		path.append(arc, true);
    }
}
