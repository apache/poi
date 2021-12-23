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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestPresetGeometries {
    @Test
    void testRead(){
        PresetGeometries shapes = PresetGeometries.getInstance();
        assertEquals(187, shapes.size());
        assertEquals(0x4533584F, shapes.hashCode());

        for(String name : shapes.keySet()) {
            CustomGeometry geom = shapes.get(name);
            Context ctx = new Context(geom, new Rectangle2D.Double(0, 0, 100, 100), presetName -> null);
            for(PathIf p : geom){
                Path2D path = p.getPath(ctx);
                assertNotNull(path);
            }
            StreamSupport.stream(geom.spliterator(), true)
                    .map(p -> p.getPath(ctx))
                    .forEach(Assertions::assertNotNull);
        }

        // we get the same instance on further calls
        assertSame(shapes, PresetGeometries.getInstance());
    }
}
