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

import static org.apache.poi.sl.draw.geom.Formula.OOXML_DEGREE;

import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import org.apache.poi.sl.draw.binding.CTPath2DArcTo;

/**
 * ArcTo command within a shape path in DrawingML:
 * {@code &lt;arcTo wR="wr" hR="hr" stAng="stAng" swAng="swAng"/&gt;}<p>
 *
 * Where {@code wr} and {@code wh} are the height and width radiuses
 * of the supposed circle being used to draw the arc.  This gives the circle
 * a total height of (2 * hR)  and a total width of (2 * wR)
 *
 * stAng is the {@code start} angle and {@code swAng} is the swing angle
 */
public class ArcToCommand implements PathCommand {
    private String hr, wr, stAng, swAng;

    ArcToCommand(CTPath2DArcTo arc){
        hr = arc.getHR();
        wr = arc.getWR();
        stAng = arc.getStAng();
        swAng = arc.getSwAng();
    }

    @Override
    public void execute(Path2D.Double path, Context ctx){
        double rx = ctx.getValue(wr);
        double ry = ctx.getValue(hr);
        double ooStart = ctx.getValue(stAng) / OOXML_DEGREE;
        double ooExtent = ctx.getValue(swAng) / OOXML_DEGREE;

        // skew the angles for AWT output
        double awtStart = convertOoxml2AwtAngle(ooStart, rx, ry);
        double awtSweep = convertOoxml2AwtAngle(ooStart+ooExtent, rx, ry)-awtStart;

        // calculate the inverse angle - taken from the (reversed) preset definition
        double radStart = Math.toRadians(ooStart);
        double invStart = Math.atan2(rx * Math.sin(radStart), ry * Math.cos(radStart));

        Point2D pt = path.getCurrentPoint();
        // calculate top/left corner
        double x0 = pt.getX() - rx * Math.cos(invStart) - rx;
        double y0 = pt.getY() - ry * Math.sin(invStart) - ry;

        Arc2D arc = new Arc2D.Double(x0, y0, 2 * rx, 2 * ry, awtStart, awtSweep, Arc2D.OPEN);
		path.append(arc, true);
    }

    /**
     * Arc2D angles are skewed, OOXML aren't ... so we need to unskew them<p>
     *
     * Furthermore ooxml angle starts at the X-axis and increases clock-wise,
     * where as Arc2D api states
     * "45 degrees always falls on the line from the center of the ellipse to
     * the upper right corner of the framing rectangle"
     * so we need to reverse it
     *
     * <pre>
     * AWT:                      OOXML:
     *            |90/-270                     |270/-90 (16200000)
     *            |                            |
     * +/-180-----------0           +/-180-----------0
     *            |               (10800000)   |
     *            |270/-90                     |90/-270 (5400000)
     * </pre>
     *
     * @param ooAngle the angle in OOXML units
     * @param width the half width of the bounding box
     * @param height the half height of the bounding box
     *
     * @return the angle in degrees
     *
     * @see <a href="http://www.onlinemathe.de/forum/Problem-bei-Winkelberechnungen-einer-Ellipse">unskew angle</a>
     **/
    private double convertOoxml2AwtAngle(double ooAngle, double width, double height) {
        double aspect = (height / width);
        // reverse angle for awt
        double awtAngle = -ooAngle;
        // normalize angle, in case it's < -360 or > 360 degrees
        double awtAngle2 = awtAngle%360.;
        double awtAngle3 = awtAngle-awtAngle2;
        // because of tangens nature, the values left [90°-270°] and right [270°-90°] of the axis are mirrored/the same
        // and the result of atan2 need to be justified
        switch ((int)(awtAngle2 / 90)) {
            case -3:
                // -270 to -360
                awtAngle3 -= 360;
                awtAngle2 += 360;
                break;
            case -2:
            case -1:
                // -90 to -270
                awtAngle3 -= 180;
                awtAngle2 += 180;
                break;
            default:
            case 0:
                // -90 to 90
                break;
            case 2:
            case 1:
                // 90 to 270
                awtAngle3 += 180;
                awtAngle2 -= 180;
                break;
            case 3:
                // 270 to 360
                awtAngle3 += 360;
                awtAngle2 -= 360;
                break;
        }

        // skew
        awtAngle = Math.toDegrees(Math.atan2(Math.tan(Math.toRadians(awtAngle2)), aspect)) + awtAngle3;
        return awtAngle;
    }
}
