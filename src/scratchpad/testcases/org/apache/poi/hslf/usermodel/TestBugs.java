
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
package org.apache.poi.hslf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.hslf.HSLFSlideShow;
import org.apache.poi.hslf.model.Picture;

import java.io.*;

/**
 * Testcases for bugs entered in bugzilla
 * the Test name contains the bugzilla bug id
 *
 * @author Yegor Kozlov
 */
public class TestBugs extends TestCase {
    protected String cwd = System.getProperty("HSLF.testdata.path");

    /**
     * Bug 41384: Array index wrong in record creation
     */
    public void test41384() throws Exception {
        FileInputStream is = new FileInputStream(new File(cwd, "41384.ppt"));
        HSLFSlideShow hslf = new HSLFSlideShow(is);
        is.close();

        SlideShow ppt = new SlideShow(hslf);
        assertTrue("No Exceptions while reading file", true);

        assertEquals(1, ppt.getSlides().length);

        PictureData[] pict = ppt.getPictureData();
        assertEquals(2, pict.length);
        assertEquals(Picture.JPEG, pict[0].getType());
        assertEquals(Picture.JPEG, pict[1].getType());
    }
}
