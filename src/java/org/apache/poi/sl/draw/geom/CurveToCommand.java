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

import org.apache.poi.sl.draw.binding.CTAdjPoint2D;

public class CurveToCommand implements PathCommand {
    private String arg1, arg2, arg3, arg4, arg5, arg6;

    CurveToCommand(CTAdjPoint2D pt1, CTAdjPoint2D pt2, CTAdjPoint2D pt3){
        arg1 = pt1.getX();
        arg2 = pt1.getY();
        arg3 = pt2.getX();
        arg4 = pt2.getY();
        arg5 = pt3.getX();
        arg6 = pt3.getY();
    }

    @Override
    public void execute(Path2D.Double path, Context ctx){
        double x1 = ctx.getValue(arg1);
        double y1 = ctx.getValue(arg2);
        double x2 = ctx.getValue(arg3);
        double y2 = ctx.getValue(arg4);
        double x3 = ctx.getValue(arg5);
        double y3 = ctx.getValue(arg6);
        path.curveTo(x1, y1, x2, y2, x3, y3);
    }
}
