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
package org.apache.poi.xslf.geom;

import static org.junit.Assert.assertEquals;

import org.apache.poi.sl.draw.binding.CTCustomGeometry2D;
import org.apache.poi.sl.draw.geom.*;
import org.junit.Test;

/**
 * Date: 10/24/11
 *
 * @author Yegor Kozlov
 */
public class TestFormulaParser {
    @Test
    public void testParse(){

        Formula[] ops = {
            new Guide("adj1", "val 100"),
            new Guide("adj2", "val 200"),
            new Guide("adj3", "val -1"),
            new Guide("a1", "*/ adj1 2 adj2"), // a1 = 100*2 / 200
            new Guide("a2", "+- adj2 a1 adj1"), // a2 = 200 + a1 - 100
            new Guide("a3", "+/ adj1 adj2 adj2"), // a3 = (100 + 200) / 200
            new Guide("a4", "?: adj3 adj1 adj2"), // a4 = adj3 > 0 ? adj1 : adj2
            new Guide("a5", "abs -2"),
        };

        CustomGeometry geom = new CustomGeometry(new CTCustomGeometry2D());
        Context ctx = new Context(geom, null, null);
        for(Formula fmla : ops) {
            ctx.evaluate(fmla);
        }

        assertEquals(100.0, ctx.getValue("adj1"), 0.0);
        assertEquals(200.0, ctx.getValue("adj2"), 0.0);
        assertEquals(1.0, ctx.getValue("a1"), 0.0);
        assertEquals(101.0, ctx.getValue("a2"), 0.0);
        assertEquals(1.5, ctx.getValue("a3"), 0.0);
        assertEquals(200.0, ctx.getValue("a4"), 0.0);
        assertEquals(2.0, ctx.getValue("a5"), 0.0);
    }
}
