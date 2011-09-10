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
import java.util.*;
import java.util.List;

import org.apache.poi.xslf.XSLFTestDataSamples;
import org.openxmlformats.schemas.presentationml.x2006.main.CTGraphicalObjectFrame;

/**
 * @author Yegor Kozlov
 */
public class TestXSLFTableStyles extends TestCase {

    public void testRead(){
        XMLSlideShow  ppt = new XMLSlideShow();
        XSLFTableStyles tblStyles = ppt.getTableStyles();
        assertNotNull(tblStyles);

        assertEquals(0, tblStyles.getStyles().size());
    }
}