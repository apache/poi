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
package org.apache.poi.xslf.usermodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Dimension;
import java.awt.geom.Rectangle2D;

import org.junit.Test;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFGroupShape {

    @Test
    public void testCreateShapes() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        ppt.setPageSize(new Dimension(792, 612));
        
        XSLFGroupShape group = slide.createGroup();
        assertEquals(1, slide.getShapes().size());

        Rectangle2D interior = new Rectangle2D.Double(-10, -10, 20, 20);
        group.setInteriorAnchor(interior);
        assertEquals(interior, group.getInteriorAnchor());

        Rectangle2D anchor = new Rectangle2D.Double(0, 0, 792, 612);
        group.setAnchor(anchor);
        assertEquals(anchor, group.getAnchor());

        assertTrue(group.getShapes().isEmpty());

        XSLFTextBox shape1 = group.createTextBox();
        assertEquals(1, group.getShapes().size());
        assertSame(shape1, group.getShapes().get(0));
        assertEquals(3, shape1.getShapeId());

        XSLFAutoShape shape2 = group.createAutoShape();
        assertEquals(2, group.getShapes().size());
        assertSame(shape1, group.getShapes().get(0));
        assertSame(shape2, group.getShapes().get(1));
        assertEquals(4, shape2.getShapeId());

        XSLFConnectorShape shape3 = group.createConnector();
        assertEquals(3, group.getShapes().size());
        assertSame(shape3, group.getShapes().get(2));
        assertEquals(5, shape3.getShapeId());

        XSLFGroupShape shape4 = group.createGroup();
        assertEquals(4, group.getShapes().size());
        assertSame(shape4, group.getShapes().get(3));
        assertEquals(6, shape4.getShapeId());

        group.removeShape(shape2);
        assertEquals(3, group.getShapes().size());
        assertSame(shape1, group.getShapes().get(0));
        assertSame(shape3, group.getShapes().get(1));
        assertSame(shape4, group.getShapes().get(2));

        group.removeShape(shape3);
        assertEquals(2, group.getShapes().size());
        assertSame(shape1, group.getShapes().get(0));
        assertSame(shape4, group.getShapes().get(1));

        group.removeShape(shape1);
        group.removeShape(shape4);
        assertTrue(group.getShapes().isEmpty());
        
        ppt.close();
    }

    @Test
    public void testRemoveShapes() throws Exception {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();

        XSLFGroupShape group1 = slide.createGroup();
        group1.createTextBox();
        XSLFGroupShape group2 = slide.createGroup();
        group2.createTextBox();
        XSLFGroupShape group3 = slide.createGroup();
        slide.removeShape(group1);
        slide.removeShape(group2);
        slide.removeShape(group3);

        ppt.close();
    }
}