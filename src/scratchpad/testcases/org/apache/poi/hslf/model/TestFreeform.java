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
package org.apache.poi.hslf.model;

import junit.framework.TestCase;
import org.apache.poi.hslf.usermodel.SlideShow;
import org.apache.poi.hslf.usermodel.RichTextRun;
import org.apache.poi.hslf.HSLFSlideShow;

import java.awt.*;
import java.awt.Rectangle;
import java.awt.geom.*;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Test Freeform object.
 * The Freeform shape is constructed from java.awt.GeneralPath.
 * Check that the get/set path accessors are consistent.
 * (TODO: verification of Bezier curves is more difficult due to rounding error.  Figure out a test approach for that)
 *
 * @author Yegor Kozlov
 */
public class TestFreeform extends TestCase {

    public void testClosedPath() throws Exception {

        GeneralPath path1 = new GeneralPath();
        path1.moveTo(100, 100);
        path1.lineTo(200, 100);
        path1.lineTo(200, 200);
        path1.lineTo(100, 200);
        path1.closePath();

        Freeform p = new Freeform();
        p.setPath(path1);

        GeneralPath path2 = p.getPath();
        assertTrue(new Area(path1).equals(new Area(path2)));
    }

    public void testLine() throws Exception {

        GeneralPath path1 = new GeneralPath(new Line2D.Double(100, 100, 200, 100));

        Freeform p = new Freeform();
        p.setPath(path1);

        GeneralPath path2 = p.getPath();
        assertTrue(new Area(path1).equals(new Area(path2)));
    }

    public void testRectangle() throws Exception {

        GeneralPath path1 = new GeneralPath(new Rectangle2D.Double(100, 100, 200, 50));

        Freeform p = new Freeform();
        p.setPath(path1);

        GeneralPath path2 = p.getPath();
        assertTrue(new Area(path1).equals(new Area(path2)));
    }
}
