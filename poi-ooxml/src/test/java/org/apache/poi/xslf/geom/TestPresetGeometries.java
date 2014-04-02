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

import junit.framework.TestCase;
import org.apache.poi.xslf.model.geom.Context;
import org.apache.poi.xslf.model.geom.CustomGeometry;
import org.apache.poi.xslf.model.geom.Guide;
import org.apache.poi.xslf.model.geom.IAdjustableShape;
import org.apache.poi.xslf.model.geom.Path;
import org.apache.poi.xslf.model.geom.PresetGeometries;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Map;

/**
 * Date: 10/24/11
 *
 * @author Yegor Kozlov
 */
public class TestPresetGeometries extends TestCase {
    public void testRead(){

        Map<String, CustomGeometry> shapes = PresetGeometries.getInstance();
        assertEquals(187, shapes.size());


        for(String name : shapes.keySet()) {
            CustomGeometry geom = shapes.get(name);
            Context ctx = new Context(geom, new Rectangle2D.Double(0, 0, 100, 100), new IAdjustableShape() {
                public Guide getAdjustValue(String name) {
                    return null;
                }
            });
            for(Path p : geom){
                GeneralPath path = p.getPath(ctx);
                assertNotNull(path);
            }
        }
    }
}
