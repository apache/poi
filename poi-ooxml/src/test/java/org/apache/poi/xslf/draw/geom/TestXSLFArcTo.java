/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.xslf.draw.geom;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.geom.Path2D;

import org.apache.poi.sl.draw.geom.ArcToCommand;
import org.apache.poi.sl.draw.geom.Context;
import org.apache.poi.sl.draw.geom.CustomGeometry;
import org.junit.jupiter.api.Test;

class TestXSLFArcTo {
    @Test
    void test() {
        ArcToCommand arc = new ArcToCommand();
        CustomGeometry geom = new CustomGeometry();
        Context ctx = new Context(geom, null, null) {

            @Override
            public double getValue(String key) {
                return 1.0;
            }
        };

        Path2D.Double path = new Path2D.Double();
        path.moveTo(1.0, 1.0);
        path.lineTo(2.0, 2.0);
        arc.execute(path, ctx);
    }

    @Test
    void testPointFails() {
        ArcToCommand arc = new ArcToCommand();
        CustomGeometry geom = new CustomGeometry();
        Context ctx = new Context(geom, null, null) {

            @Override
            public double getValue(String key) {
                return 1.0;
            }
        };

        assertThrows(IllegalStateException.class,
                () -> arc.execute(new Path2D.Double(), ctx));
    }
}
