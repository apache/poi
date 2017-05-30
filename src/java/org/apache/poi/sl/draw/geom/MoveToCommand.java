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

public class MoveToCommand implements PathCommand {
    private String arg1, arg2;

    MoveToCommand(CTAdjPoint2D pt){
        arg1 = pt.getX();
        arg2 = pt.getY();
    }

    MoveToCommand(String s1, String s2){
        arg1 = s1;
        arg2 = s2;
    }

    @Override
    public void execute(Path2D.Double path, Context ctx){
        double x = ctx.getValue(arg1);
        double y = ctx.getValue(arg2);
        path.moveTo(x, y);
    }
}
