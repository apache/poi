/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;

/**
 * Test <code>HSSFPicture</code>.
 *
 * @author Yegor Kozlov (yegor at apache.org)
 */
public final class TestHSSFPicture extends TestCase{

    public void testResize() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh1 = wb.createSheet();
        HSSFPatriarch p1 = sh1.createDrawingPatriarch();

        byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("logoKarmokar4.png");
        int idx1 = wb.addPicture( pictureData, HSSFWorkbook.PICTURE_TYPE_PNG );
        HSSFPicture picture1 = p1.createPicture(new HSSFClientAnchor(), idx1);
        HSSFClientAnchor anchor1 = picture1.getPreferredSize();

        //assert against what would BiffViewer print if we insert the image in xls and dump the file
        assertEquals(0, anchor1.getCol1());
        assertEquals(0, anchor1.getRow1());
        assertEquals(1, anchor1.getCol2());
        assertEquals(9, anchor1.getRow2());
        assertEquals(0, anchor1.getDx1());
        assertEquals(0, anchor1.getDy1());
        assertEquals(848, anchor1.getDx2());
        assertEquals(240, anchor1.getDy2());
    }

    /**
     * Bug # 45829 reported ArithmeticException (/ by zero) when resizing png with zero DPI.
     */
    public void test45829() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sh1 = wb.createSheet();
        HSSFPatriarch p1 = sh1.createDrawingPatriarch();

        byte[] pictureData = HSSFTestDataSamples.getTestDataFileContent("45829.png");
        int idx1 = wb.addPicture( pictureData, HSSFWorkbook.PICTURE_TYPE_PNG );
        HSSFPicture pic = p1.createPicture(new HSSFClientAnchor(), idx1);
        pic.resize();
    }
}
