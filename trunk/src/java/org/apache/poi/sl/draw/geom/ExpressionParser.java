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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple regexp-based parser of shape guide formulas in DrawingML
 */
public class ExpressionParser {
    private static final Map<String, ExpressionEntry> impls =
        new HashMap<String, ExpressionEntry>();
    
    private static class ExpressionEntry {
        final Pattern regex;
        final Constructor<? extends Expression> con;
        ExpressionEntry(String regex, Class<? extends Expression> cls)
        throws SecurityException, NoSuchMethodException {
            this.regex = Pattern.compile(regex);
            this.con = cls.getDeclaredConstructor(Matcher.class);
            impls.put(op(regex), this);
        }
    }
    
    static {
        try {
            new ExpressionEntry("\\*/ +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", MultiplyDivideExpression.class);
            new ExpressionEntry("\\+- +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)( 0)?", AddSubtractExpression.class);
            new ExpressionEntry("\\+/ +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", AddDivideExpression.class);
            new ExpressionEntry("\\?: +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", IfElseExpression.class);
            new ExpressionEntry("val +([\\-\\w]+)", LiteralValueExpression.class);
            new ExpressionEntry("abs +([\\-\\w]+)", AbsExpression.class);
            new ExpressionEntry("sqrt +([\\-\\w]+)", SqrtExpression.class);
            new ExpressionEntry("max +([\\-\\w]+) +([\\-\\w]+)", MaxExpression.class);
            new ExpressionEntry("min +([\\-\\w]+) +([\\-\\w]+)", MinExpression.class);
            new ExpressionEntry("at2 +([\\-\\w]+) +([\\-\\w]+)", ArcTanExpression.class);
            new ExpressionEntry("sin +([\\-\\w]+) +([\\-\\w]+)", SinExpression.class);
            new ExpressionEntry("cos +([\\-\\w]+) +([\\-\\w]+)", CosExpression.class);
            new ExpressionEntry("tan +([\\-\\w]+) +([\\-\\w]+)", TanExpression.class);
            new ExpressionEntry("cat2 +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", CosineArcTanExpression.class);
            new ExpressionEntry("sat2 +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", SinArcTanExpression.class);
            new ExpressionEntry("pin +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", PinExpression.class);
            new ExpressionEntry("mod +([\\-\\w]+) +([\\-\\w]+) +([\\-\\w]+)", ModExpression.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String op(String str) {
        return (str == null || !str.contains(" "))
            ? "" : str.substring(0, str.indexOf(" ")).replace("\\", "");
    }
    
    public static Expression parse(String str) {
        ExpressionEntry ee = impls.get(op(str));
        Matcher m = (ee == null) ? null : ee.regex.matcher(str);
        if (m == null || !m.matches()) {
            throw new RuntimeException("Unsupported formula: " + str);
        }
        
        try {
            return ee.con.newInstance(m);
        } catch (Exception e) {
            throw new RuntimeException("Unsupported formula: " + str, e);
        }
    }
}
