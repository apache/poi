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

public interface ArcToCommandIf extends PathCommand {

    void setHR(String hr);

    void setWR(String wr);

    void setStAng(String stAng);

    void setSwAng(String swAng);

    String getHR();

    String getWR();

    String getStAng();

    String getSwAng();


    @Override
    default void execute(Path2D.Double path, Context ctx){
        double rx = ctx.getValue(getWR());
        double ry = ctx.getValue(getHR());
        double ooStart = ctx.getValue(getStAng()) / OOXML_DEGREE;
        double ooExtent = ctx.getValue(getSwAng()) / OOXML_DEGREE;

        // skew the angles for AWT output
        double awtStart = ArcToCommand.convertOoxml2AwtAngle(ooStart, rx, ry);
        double awtSweep = ArcToCommand.convertOoxml2AwtAngle(ooStart+ooExtent, rx, ry)-awtStart;

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


}
