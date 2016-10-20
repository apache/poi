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
        HSSFClientAnchor[] anchors = {
            new HSSFClientAnchor( 0 , 0 , 0 , 0 ,(short)0, 1,(short)1,3),
            new HSSFClientAnchor( 100 , 0 , 900 , 255 ,(short)0, 1,(short)1,3),
            new HSSFClientAnchor( 900 , 0 , 100 , 255 ,(short)0, 1,(short)1,3)
        };
        for (HSSFClientAnchor anchor : anchors) {
            EscherClientAnchorRecord record = (EscherClientAnchorRecord)ConvertAnchor.createAnchor(anchor);
            assertEquals(anchor.getDx1(), record.getDx1());
            assertEquals(anchor.getDx2(), record.getDx2());
            assertEquals(anchor.getDy1(), record.getDy1());
            assertEquals(anchor.getDy2(), record.getDy2());
            assertEquals(anchor.getCol1(), record.getCol1());
            assertEquals(anchor.getCol2(), record.getCol2());
            assertEquals(anchor.getRow1(), record.getRow1());
            assertEquals(anchor.getRow2(), record.getRow2());
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

    /**
     * Check {@link HSSFClientAnchor} constructor does not treat 32768 as -32768.
     */
    public void testCanHaveRowGreaterThan32767() {
        // Maximum permitted row number should be 65535.
        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 32768, (short) 0, 32768);

        assertEquals(32768, anchor.getRow1());
        assertEquals(32768, anchor.getRow2());
    }

    /**
     * Check the maximum is not set at 255*256 instead of 256*256 - 1.
     */
    public void testCanHaveRowUpTo65535() {
        HSSFClientAnchor anchor = new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 65535, (short) 0, 65535);

        assertEquals(65535, anchor.getRow1());
        assertEquals(65535, anchor.getRow2());
    }

    public void testCannotHaveRowGreaterThan65535() {
        try {
            new HSSFClientAnchor(0, 0, 0, 0, (short) 0, 65536, (short) 0, 65536);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }

    /**
     * Check the same maximum value enforced when using {@link HSSFClientAnchor#setRow1}.
     */
    public void testCanSetRowUpTo65535() {
        HSSFClientAnchor anchor = new HSSFClientAnchor();
        anchor.setRow1(65535);
        anchor.setRow2(65535);

        assertEquals(65535, anchor.getRow1());
        assertEquals(65535, anchor.getRow2());
    }

    public void testCannotSetRow1GreaterThan65535() {
        try {
            new HSSFClientAnchor().setRow1(65536);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }
    public void testCannotSetRow2GreaterThan65535() {
        try {
            new HSSFClientAnchor().setRow2(65536);
            fail("Expected IllegalArgumentException to be thrown");
        } catch (IllegalArgumentException ex) {
            // pass
        }
    }
}
