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

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple regexp-based parser of shape guide formulas in DrawingML
 *
 * @author Yegor Kozlov
 */
public class ExpressionParser {
    static final HashMap<String, Class> impls = new HashMap<String, Class>();
    static {
        impls.put("\\*/ +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", MultiplyDivideExpression.class);
        impls.put("\\+- +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)( 0)?", AddSubtractExpression.class);
        impls.put("\\+/ +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", AddDivideExpression.class);
        impls.put("\\?: +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", IfElseExpression.class);
        impls.put("val +([\\-\\w]+)", LiteralValueExpression.class);
        impls.put("abs +([\\-\\w]+)", AbsExpression.class);
        impls.put("sqrt +([\\-\\w]+)", SqrtExpression.class);
        impls.put("max +([\\-\\w]+) +([\\-\\w]+)", MaxExpression.class);
        impls.put("min +([\\-\\w]+) +([\\-\\w]+)", MinExpression.class);
        impls.put("at2 +([\\-\\w]+) +([\\-\\w]+)", ArcTanExpression.class);
        impls.put("sin +([\\-\\w]+) +([\\-\\w]+)", SinExpression.class);
        impls.put("cos +([\\-\\w]+) +([\\-\\w]+)", CosExpression.class);
        impls.put("tan +([\\-\\w]+) +([\\-\\w]+)", TanExpression.class);
        impls.put("cat2 +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", CosineArcTanExpression.class);
        impls.put("sat2 +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", SinArcTanExpression.class);
        impls.put("pin +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", PinExpression.class);
        impls.put("mod +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", ModExpression.class);

    }

    public static Expression parse(String str){
        for(String regexp : impls.keySet()) {
            Pattern ptrn = Pattern.compile(regexp);
            Matcher m = ptrn.matcher(str);
            if(m.matches()) {
                Class c = impls.get(regexp);
                try {
                    return (Expression)c.getDeclaredConstructor(Matcher.class).newInstance(m);
                } catch (Exception e){
                    throw new RuntimeException(e);
                }
            }
        }
        throw new RuntimeException("Unsupported formula: " + str);
    }
}
