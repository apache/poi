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

import org.openxmlformats.schemas.drawingml.x2006.main.CTGeomGuide;

/**
 * Date: 10/24/11
 *
 * @author Yegor Kozlov
 */
public class Guide extends Formula {
    private String name, fmla;
    private Expression expr;

    public Guide(CTGeomGuide gd) {
        this(gd.getName(), gd.getFmla());
    }

    public Guide(String nm, String fm){
        name = nm;
        fmla = fm;
        expr = ExpressionParser.parse(fm);
    }


    String getName(){
        return name;
    }

    String getFormula(){
        return fmla;
    }

    @Override
    public double evaluate(Context ctx){
        return expr.evaluate(ctx);
    }


}
