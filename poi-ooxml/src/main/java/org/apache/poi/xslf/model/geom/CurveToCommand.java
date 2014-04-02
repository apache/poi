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

package org.apache.poi.xslf.model.geom;

import org.openxmlformats.schemas.drawingml.x2006.main.CTAdjPoint2D;

import java.awt.geom.GeneralPath;

/**
 * Date: 10/25/11
 *
 * @author Yegor Kozlov
 */
public class CurveToCommand implements PathCommand {
    private String arg1, arg2, arg3, arg4, arg5, arg6;

    CurveToCommand(CTAdjPoint2D pt1, CTAdjPoint2D pt2, CTAdjPoint2D pt3){
        arg1 = pt1.getX().toString();
        arg2 = pt1.getY().toString();
        arg3 = pt2.getX().toString();
        arg4 = pt2.getY().toString();
        arg5 = pt3.getX().toString();
        arg6 = pt3.getY().toString();
    }

    public void execute(GeneralPath path, Context ctx){
        double x1 = ctx.getValue(arg1);
        double y1 = ctx.getValue(arg2);
        double x2 = ctx.getValue(arg3);
        double y2 = ctx.getValue(arg4);
        double x3 = ctx.getValue(arg5);
        double y3 = ctx.getValue(arg6);
        path.curveTo((float)x1, (float)y1, (float)x2, (float)y2, (float)x3, (float)y3);
    }
}
