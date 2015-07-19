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
import java.util.HashMap;
import java.util.Map;

/**
 * Date: 10/24/11
 *
 * @author Yegor Kozlov
 */
public class Context {
    final Map<String, Double> _ctx = new HashMap<String, Double>();
    final IAdjustableShape _props;
    final Rectangle2D _anchor;
    
    public Context(CustomGeometry geom, Rectangle2D anchor, IAdjustableShape props){
        _props = props;
        _anchor = anchor;
        for(Guide gd : geom.adjusts) evaluate(gd);
        for(Guide gd : geom.guides) evaluate(gd);
    }

    public Rectangle2D getShapeAnchor(){
        return _anchor;
    }

    public Guide getAdjustValue(String name){
        return _props.getAdjustValue(name);
    }

    public double getValue(String key){
        if(key.matches("(\\+|-)?\\d+")){
            return Double.parseDouble(key);
        }

        Formula builtIn = Formula.builtInFormulas.get(key);
        if(builtIn != null){
            return builtIn.evaluate(this);
        }

        if(!_ctx.containsKey(key)) {
            throw new RuntimeException("undefined variable: " + key);
        }

        return _ctx.get(key);
    }

    public double evaluate(Formula fmla){
        double result = fmla.evaluate(this);
        String key = fmla.getName();
        if(key != null) _ctx.put(key, result);
        return result;
    }
}
