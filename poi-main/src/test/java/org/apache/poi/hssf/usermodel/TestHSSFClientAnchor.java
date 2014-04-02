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

package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;
import org.apache.poi.ddf.EscherClientAnchorRecord;
import org.apache.poi.hssf.model.ConvertAnchor;

/**
 * Various tests for HSSFClientAnchor.
 *
 * @author Glen Stampoultzis (glens at apache.org)
 * @author Yegor Kozlov (yegor at apache.org)
 */
public final class TestHSSFClientAnchor extends TestCase {
    public void testGetAnchorHeightInPoints() {
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("test");
        HSSFClientAnchor a = new HSSFClientAnchor(0,0,1023,255,(short)0,0,(short)0,0);
        float p = a.getAnchorHeightInPoints(sheet);
        assertEquals(12.7,p,0.001);

        sheet.createRow(0).setHeightInPoints(14);
        a = new HSSFClientAnchor(0,0,1023,255,(short)0,0,(short)0,0);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(13.945,p,0.001);

        a = new HSSFClientAnchor(0,0,1023,127,(short)0,0,(short)0,0);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(6.945,p,0.001);

        a = new HSSFClientAnchor(0,126,1023,127,(short)0,0,(short)0,0);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(0.054,p,0.001);

        a = new HSSFClientAnchor(0,0,1023,0,(short)0,0,(short)0,1);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(14.0,p,0.001);

        sheet.createRow(0).setHeightInPoints(12);
        a = new HSSFClientAnchor(0,127,1023,127,(short)0,0,(short)0,1);
        p = a.getAnchorHeightInPoints(sheet);
        assertEquals(12.372,p,0.001);

    }

    /**
     * When HSSFClientAnchor is converted into EscherClientAnchorRecord
     * check that dx1, dx2, dy1 and dy2 are written "as is".
     * (Bug 42999 reported that dx1 and dx2 are swapped if dx1>dx2. It doesn't make sense for client anchors.)
     */
    public void testConvertAnchor() {
        HSSFClientAnchor[] anchor = {
            new HSSFClientAnchor( 0 , 0 , 0 , 0 ,(short)0, 1,(short)1,3),
            new HSSFClientAnchor( 100 , 0 , 900 , 255 ,(short)0, 1,(short)1,3),
            new HSSFClientAnchor( 900 , 0 , 100 , 255 ,(short)0, 1,(short)1,3)
        };
        for (int i = 0; i < anchor.length; i++) {
            EscherClientAnchorRecord record = (EscherClientAnchorRecord)ConvertAnchor.createAnchor(anchor[i]);
            assertEquals(anchor[i].getDx1(), record.getDx1());
            assertEquals(anchor[i].getDx2(), record.getDx2());
            assertEquals(anchor[i].getDy1(), record.getDy1());
            assertEquals(anchor[i].getDy2(), record.getDy2());
            assertEquals(anchor[i].getCol1(), record.getCol1());
            assertEquals(anchor[i].getCol2(), record.getCol2());
            assertEquals(anchor[i].getRow1(), record.getRow1());
            assertEquals(anchor[i].getRow2(), record.getRow2());
        }
    }

    public void testAnchorHeightInPoints(){
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet();

        HSSFClientAnchor[] anchor = {
            new HSSFClientAnchor( 0 , 0,    0 , 0 ,(short)0, 1,(short)1, 3),
            new HSSFClientAnchor( 0 , 254 , 0 , 126 ,(short)0, 1,(short)1, 3),
            new HSSFClientAnchor( 0 , 128 , 0 , 128 ,(short)0, 1,(short)1, 3),
            new HSSFClientAnchor( 0 , 0 , 0 , 128 ,(short)0, 1,(short)1, 3),
        };
        float[] ref = {25.5f, 19.125f, 25.5f, 31.875f};
        for (int i = 0; i < anchor.length; i++) {
            float height = anchor[i].getAnchorHeightInPoints(sheet);
            assertEquals(ref[i], height, 0);
        }
    }
}
