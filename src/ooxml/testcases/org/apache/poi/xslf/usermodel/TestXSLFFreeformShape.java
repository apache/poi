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

import junit.framework.TestCase;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFFreeformShape extends TestCase {

    public void testSetPath() {
        XMLSlideShow ppt = new XMLSlideShow();
        XSLFSlide slide = ppt.createSlide();
        XSLFFreeformShape shape1 = slide.createFreeform();
        // comples path consisting of a rectangle and an ellipse inside it
        GeneralPath path1 = new GeneralPath(new Rectangle(150, 150, 300, 300));
        path1.append(new Ellipse2D.Double(200, 200, 100, 50), false);
        shape1.setPath(path1);

        GeneralPath path2 = shape1.getPath();

        // YK: how to compare the original path1 and the value returned by XSLFFreeformShape.getPath() ?
        // one way is to create another XSLFFreeformShape from path2 and compare the resulting xml
        assertEquals(path1.getBounds2D(), path2.getBounds2D());

        XSLFFreeformShape shape2 = slide.createFreeform();
        shape2.setPath(path2);

        assertEquals(shape1.getSpPr().getCustGeom().toString(), shape2.getSpPr().getCustGeom().toString());
    }
}