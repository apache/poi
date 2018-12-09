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
import java.util.regex.Pattern;

public class Context {
    private static final Pattern DOUBLE_PATTERN = Pattern.compile(
        "[\\x00-\\x20]*[+-]?(NaN|Infinity|((((\\p{Digit}+)(\\.)?((\\p{Digit}+)?)" +
        "([eE][+-]?(\\p{Digit}+))?)|(\\.(\\p{Digit}+)([eE][+-]?(\\p{Digit}+))?)|" +
        "(((0[xX](\\p{XDigit}+)(\\.)?)|(0[xX](\\p{XDigit}+)?(\\.)(\\p{XDigit}+)))" +
        "[pP][+-]?(\\p{Digit}+)))[fFdD]?))[\\x00-\\x20]*");

    private final Map<String, Double> _ctx = new HashMap<>();
    private final IAdjustableShape _props;
    private final Rectangle2D _anchor;
    
    public Context(CustomGeometry geom, Rectangle2D anchor, IAdjustableShape props){
        _props = props;
        _anchor = anchor;
        for(Guide gd : geom.adjusts) {
            evaluate(gd);
        }
        for(Guide gd : geom.guides) {
            evaluate(gd);
        }
    }

    Rectangle2D getShapeAnchor(){
        return _anchor;
    }

    Guide getAdjustValue(String name){
        // ignore HSLF props for now ... the results with default value are usually better - see #59004
        return (_props.getClass().getName().contains("hslf")) ? null : _props.getAdjustValue(name);
    }

    public double getValue(String key){
        if(DOUBLE_PATTERN.matcher(key).matches()){
            return Double.parseDouble(key);
        }

        // BuiltInGuide throws IllegalArgumentException if key is not defined
        return _ctx.containsKey(key) ? _ctx.get(key) : evaluate(BuiltInGuide.valueOf("_"+key));
    }

    public double evaluate(Formula fmla){
        double result = fmla.evaluate(this);
        if (fmla instanceof Guide) {
            String key = ((Guide)fmla).getName();
            if (key != null) {
                _ctx.put(key, result);
            }
        }
        return result;
    }
}
