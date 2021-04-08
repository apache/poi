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

package org.apache.poi.ss.usermodel;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a type of a conditional formatting rule
 */
public class ConditionType {
    private static Map<Integer,ConditionType> lookup = new HashMap<>();
    
    /**
     * This conditional formatting rule compares a cell value
     * to a formula calculated result, using an operator
     */
    public static final ConditionType CELL_VALUE_IS = 
            new ConditionType(1, "cellIs");

    /**
     * This conditional formatting rule contains a formula to evaluate.
     * When the formula result is true, the cell is highlighted.
     */
    public static final ConditionType FORMULA = 
            new ConditionType(2, "expression");
    
    /**
     * This conditional formatting rule contains a color scale,
     * with the cell background set according to a gradient.
     */
    public static final ConditionType COLOR_SCALE = 
            new ConditionType(3, "colorScale");
    
    /**
     * This conditional formatting rule sets a data bar, with the
     *  cell populated with bars based on their values
     */
    public static final ConditionType DATA_BAR = 
            new ConditionType(4, "dataBar");
    
    /**
     * This conditional formatting rule that files the values
     */
    public static final ConditionType FILTER = 
            new ConditionType(5, null);
    
    /**
     * This conditional formatting rule sets a data bar, with the
     *  cell populated with bars based on their values
     */
    public static final ConditionType ICON_SET = 
            new ConditionType(6, "iconSet");
    
    
    public final byte id;
    public final String type;

    public String toString() {
        return id + " - " + type;
    }
    
    
    public static ConditionType forId(byte id) {
        return forId((int)id);
    }
    public static ConditionType forId(int id) {
        return lookup.get(id);
    }
        
    private ConditionType(int id, String type) {
        this.id = (byte)id; this.type = type;
        lookup.put(id, this);
    }
}
