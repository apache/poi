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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.poi.hssf.HSSFTestDataSamples;
import org.apache.poi.hssf.record.PaletteRecord;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.junit.Before;
import org.junit.Test;

public final class TestHSSFPalette {
    private PaletteRecord _palette;
    private HSSFPalette _hssfPalette;


    @Before
    public void setUp() {
        _palette = new PaletteRecord();
        _hssfPalette = new HSSFPalette(_palette);
    }

    /**
     * Verifies that a custom palette can be created, saved, and reloaded
     */
    @Test
    public void testCustomPalette() {
        //reading sample xls
        HSSFWorkbook book = HSSFTestDataSamples.openSampleWorkbook("Simple.xls");

        //creating custom palette
        HSSFPalette palette = book.getCustomPalette();
        palette.setColorAtIndex((short) 0x12, (byte) 101, (byte) 230, (byte) 100);
        palette.setColorAtIndex((short) 0x3b, (byte) 0, (byte) 255, (byte) 52);

        //writing to disk; reading in and verifying palette
        book = HSSFTestDataSamples.writeOutAndReadBack(book);

        palette = book.getCustomPalette();
        HSSFColor color = palette.getColor(HSSFColorPredefined.CORAL.getIndex());  //unmodified
        assertNotNull("Unexpected null in custom palette (unmodified index)", color);
        short[] expectedRGB = HSSFColorPredefined.CORAL.getTriplet();
        short[] actualRGB = color.getTriplet();
        String msg = "Expected palette position to remain unmodified";
        assertEquals(msg, expectedRGB[0], actualRGB[0]);
        assertEquals(msg, expectedRGB[1], actualRGB[1]);
        assertEquals(msg, expectedRGB[2], actualRGB[2]);

        color = palette.getColor((short) 0x12);
        assertNotNull("Unexpected null in custom palette (modified)", color);
        actualRGB = color.getTriplet();
        msg = "Expected palette modification to be preserved across save";
        assertEquals(msg, (short) 101, actualRGB[0]);
        assertEquals(msg, (short) 230, actualRGB[1]);
        assertEquals(msg, (short) 100, actualRGB[2]);
    }

    /**
     * Uses the palette from cell stylings
     */
    @SuppressWarnings("deprecation")
    @Test
    public void testPaletteFromCellColours() {
        HSSFWorkbook book = HSSFTestDataSamples.openSampleWorkbook("SimpleWithColours.xls");

        HSSFPalette p = book.getCustomPalette();

        HSSFCell cellA = book.getSheetAt(0).getRow(0).getCell(0);
        HSSFCell cellB = book.getSheetAt(0).getRow(1).getCell(0);
        HSSFCell cellC = book.getSheetAt(0).getRow(2).getCell(0);
        HSSFCell cellD = book.getSheetAt(0).getRow(3).getCell(0);
        HSSFCell cellE = book.getSheetAt(0).getRow(4).getCell(0);

        // Plain
        assertEquals("I'm plain", cellA.getStringCellValue());
        assertEquals(64, cellA.getCellStyle().getFillForegroundColor());
        assertEquals(64, cellA.getCellStyle().getFillBackgroundColor());
        assertEquals(Font.COLOR_NORMAL, cellA.getCellStyle().getFont(book).getColor());
        assertEquals(FillPatternType.NO_FILL, cellA.getCellStyle().getFillPattern());
        assertEquals(FillPatternType.NO_FILL, cellA.getCellStyle().getFillPatternEnum());
        assertEquals("0:0:0", p.getColor((short)64).getHexString());
        assertEquals(null, p.getColor((short)32767));

        // Red
        assertEquals("I'm red", cellB.getStringCellValue());
        assertEquals(64, cellB.getCellStyle().getFillForegroundColor());
        assertEquals(64, cellB.getCellStyle().getFillBackgroundColor());
        assertEquals(10, cellB.getCellStyle().getFont(book).getColor());
        assertEquals(FillPatternType.NO_FILL, cellB.getCellStyle().getFillPattern());
        assertEquals(FillPatternType.NO_FILL, cellB.getCellStyle().getFillPatternEnum());
        assertEquals("0:0:0", p.getColor((short)64).getHexString());
        assertEquals("FFFF:0:0", p.getColor((short)10).getHexString());

        // Red + green bg
        assertEquals("I'm red with a green bg", cellC.getStringCellValue());
        assertEquals(11, cellC.getCellStyle().getFillForegroundColor());
        assertEquals(64, cellC.getCellStyle().getFillBackgroundColor());
        assertEquals(10, cellC.getCellStyle().getFont(book).getColor());
        assertEquals(FillPatternType.SOLID_FOREGROUND, cellC.getCellStyle().getFillPattern());
        assertEquals(FillPatternType.SOLID_FOREGROUND, cellC.getCellStyle().getFillPatternEnum());
        assertEquals("0:FFFF:0", p.getColor((short)11).getHexString());
        assertEquals("FFFF:0:0", p.getColor((short)10).getHexString());

        // Pink with yellow
        assertEquals("I'm pink with a yellow pattern (none)", cellD.getStringCellValue());
        assertEquals(13, cellD.getCellStyle().getFillForegroundColor());
        assertEquals(64, cellD.getCellStyle().getFillBackgroundColor());
        assertEquals(14, cellD.getCellStyle().getFont(book).getColor());
        assertEquals(FillPatternType.NO_FILL, cellD.getCellStyle().getFillPattern());
        assertEquals(FillPatternType.NO_FILL, cellD.getCellStyle().getFillPatternEnum());
        assertEquals("FFFF:FFFF:0", p.getColor((short)13).getHexString());
        assertEquals("FFFF:0:FFFF", p.getColor((short)14).getHexString());

        // Pink with yellow - full
        assertEquals("I'm pink with a yellow pattern (full)", cellE.getStringCellValue());
        assertEquals(13, cellE.getCellStyle().getFillForegroundColor());
        assertEquals(64, cellE.getCellStyle().getFillBackgroundColor());
        assertEquals(14, cellE.getCellStyle().getFont(book).getColor());
        assertEquals(FillPatternType.NO_FILL, cellE.getCellStyle().getFillPattern());
        assertEquals(FillPatternType.NO_FILL, cellE.getCellStyle().getFillPatternEnum());
        assertEquals("FFFF:FFFF:0", p.getColor((short)13).getHexString());
        assertEquals("FFFF:0:FFFF", p.getColor((short)14).getHexString());
    }

    @Test
    public void testFindSimilar() throws IOException {
        HSSFWorkbook book = new HSSFWorkbook();
        HSSFPalette p = book.getCustomPalette();
        
        /* first test the defaults */
        assertArrayEquals(
        				new short[] {(short) 255, (short) 255, (short) 0}, // not [204, 255, 255]
        				p.findSimilarColor((byte) 204, (byte) 255, (byte) 0).getTriplet()
        );

        assertArrayEquals(
        				new short[] {(short) 153, (short) 204, (short) 0}, // not [128, 0, 0]
        				p.findSimilarColor((byte) 128, (byte) 255, (byte) 0).getTriplet()
        );

        assertArrayEquals(
        				new short[] {(short) 0, (short) 255, (short) 0}, // not [0, 51, 102]
        				p.findSimilarColor((byte) 0, (byte) 255, (byte) 102).getTriplet()
        );

        assertArrayEquals(
        				new short[] {(short) 0, (short) 102, (short) 204}, // not [255, 102, 0]
        				p.findSimilarColor((byte) 0, (byte) 102, (byte) 255).getTriplet()
        );

        assertArrayEquals(
        				new short[] {(short) 255, (short) 0, (short) 255}, // not [128, 0, 0]
        				p.findSimilarColor((byte) 128, (byte) 0, (byte) 255).getTriplet()
        );

        assertArrayEquals(
        				new short[] {(short) 255, (short) 0, (short) 255}, // not [255, 255, 153]
        				p.findSimilarColor((byte) 255, (byte) 0, (byte) 153).getTriplet()
        );


        // Add a few edge colours in
        p.setColorAtIndex((short)8, (byte)-1, (byte)0, (byte)0);
        p.setColorAtIndex((short)9, (byte)0, (byte)-1, (byte)0);
        p.setColorAtIndex((short)10, (byte)0, (byte)0, (byte)-1);

        // And some near a few of them
        p.setColorAtIndex((short)11, (byte)-1, (byte)2, (byte)2);
        p.setColorAtIndex((short)12, (byte)-2, (byte)2, (byte)10);
        p.setColorAtIndex((short)13, (byte)-4, (byte)0, (byte)0);
        p.setColorAtIndex((short)14, (byte)-8, (byte)0, (byte)0);

        assertEquals(
                "FFFF:0:0", p.getColor((short)8).getHexString()
        );

        // Now check we get the right stuff back
        assertEquals(
                p.getColor((short)8).getHexString(),
                p.findSimilarColor((byte)-1, (byte)0, (byte)0).getHexString()
        );
        assertEquals(
                p.getColor((short)8).getHexString(),
                p.findSimilarColor((byte)-2, (byte)0, (byte)0).getHexString()
        );
        assertEquals(
                p.getColor((short)8).getHexString(),
                p.findSimilarColor((byte)-1, (byte)1, (byte)0).getHexString()
        );
        assertEquals(
                p.getColor((short)11).getHexString(),
                p.findSimilarColor((byte)-1, (byte)2, (byte)1).getHexString()
        );
        assertEquals(
                p.getColor((short)12).getHexString(),
                p.findSimilarColor((byte)-1, (byte)2, (byte)10).getHexString()
        );
        
        // And with ints not bytes
        assertEquals(
                p.getColor((short)11).getHexString(),
                p.findSimilarColor(255, 2, 1).getHexString()
        );
        assertEquals(
                p.getColor((short)12).getHexString(),
                p.findSimilarColor(255, 2, 10).getHexString()
        );
        
        book.close();
    }
    
    /**
     * Verifies that the generated gnumeric-format string values match the
     * hardcoded values in the HSSFColor default color palette
     */
    @Test
    public void testGnumericStrings() {
        compareToDefaults(new ColorComparator() {
            @Override
            public void compare(HSSFColor expected, HSSFColor palette)
            {
                assertEquals(expected.getHexString(), palette.getHexString());
            }
        });
    }

    /**
     * Verifies that the palette handles invalid palette indexes
     */
    @Test
    public void testBadIndexes() {
        //too small
        _hssfPalette.setColorAtIndex((short) 2, (byte) 255, (byte) 255, (byte) 255);
        //too large
        _hssfPalette.setColorAtIndex((short) 0x45, (byte) 255, (byte) 255, (byte) 255);

        //should still match defaults;
        compareToDefaults(new ColorComparator() {
            @Override
            public void compare(HSSFColor expected, HSSFColor palette)
            {
                short[] s1 = expected.getTriplet();
                short[] s2 = palette.getTriplet();
                assertEquals(s1[0], s2[0]);
                assertEquals(s1[1], s2[1]);
                assertEquals(s1[2], s2[2]);
            }
        });
    }

    private void compareToDefaults(ColorComparator c) {
        Map<Integer,HSSFColor> colors = HSSFColor.getIndexHash();
        Iterator<Integer> it = colors.keySet().iterator();
        while (it.hasNext())
        {
            Number index = it.next();
            HSSFColor expectedColor = colors.get(index);
            HSSFColor paletteColor = _hssfPalette.getColor(index.shortValue());
            c.compare(expectedColor, paletteColor);
        }
    }

    @Test
    public void testAddColor() {
        try
        {
            _hssfPalette.addColor((byte)10,(byte)10,(byte)10);
            fail();
        } catch (RuntimeException e) {
            // Failing because by default there are no colours left in the palette.
        }
    }

    private static interface ColorComparator {
        void compare(HSSFColor expected, HSSFColor palette);
    }

    @Test
    public void test48403() throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook();

        Color color = Color.decode("#006B6B");
        HSSFPalette palette = wb.getCustomPalette();
        
        HSSFColor hssfColor = palette.findColor((byte) color.getRed(),
                (byte) color.getGreen(), (byte) color.getBlue());
        assertNull(hssfColor);

        palette.setColorAtIndex(
                (short) (PaletteRecord.STANDARD_PALETTE_SIZE - 1),
                (byte) color.getRed(), (byte) color.getGreen(),
                (byte) color.getBlue());
        hssfColor = palette.getColor((short) (PaletteRecord.STANDARD_PALETTE_SIZE - 1));
        assertNotNull(hssfColor);
        assertEquals(55, hssfColor.getIndex());
        assertArrayEquals(new short[] {0, 107, 107}, hssfColor.getTriplet());
        
        wb.close();
    }
}
