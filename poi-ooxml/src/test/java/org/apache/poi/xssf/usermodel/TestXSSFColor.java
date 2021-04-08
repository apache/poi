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

package org.apache.poi.xssf.usermodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.apache.poi.xssf.XSSFTestDataSamples;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColors;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRgbColor;

public final class TestXSSFColor {

   @Test
   void testIndexedColour() throws Exception {
      try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48779.xlsx")) {
         // Check the CTColor is as expected
         XSSFColor indexed = wb.getCellStyleAt(1).getFillBackgroundXSSFColor();
         assertTrue(indexed.getCTColor().isSetIndexed());
         assertEquals(64, indexed.getCTColor().getIndexed());
         assertFalse(indexed.getCTColor().isSetRgb());
         assertNull(indexed.getCTColor().getRgb());

         // Now check the XSSFColor
         // Note - 64 is a special "auto" one with no rgb equiv
         assertEquals(64, indexed.getIndexed());
         assertNull(indexed.getRGB());
         assertNull(indexed.getRGBWithTint());
         assertNull(indexed.getARGBHex());
         assertFalse(indexed.hasAlpha());
         assertFalse(indexed.hasTint());

         // Now move to one with indexed rgb values
         indexed.setIndexed(59);
         assertTrue(indexed.getCTColor().isSetIndexed());
         assertEquals(59, indexed.getCTColor().getIndexed());
         assertFalse(indexed.getCTColor().isSetRgb());
         assertNull(indexed.getCTColor().getRgb());

         assertEquals(59, indexed.getIndexed());
         assertEquals("FF333300", indexed.getARGBHex());

         assertEquals(3, indexed.getRGB().length);
         assertEquals(0x33, indexed.getRGB()[0]);
         assertEquals(0x33, indexed.getRGB()[1]);
         assertEquals(0x00, indexed.getRGB()[2]);

         assertEquals(4, indexed.getARGB().length);
         assertEquals(-1, indexed.getARGB()[0]);
         assertEquals(0x33, indexed.getARGB()[1]);
         assertEquals(0x33, indexed.getARGB()[2]);
         assertEquals(0x00, indexed.getARGB()[3]);

         // You don't get tinted indexed colours, sorry...
         assertNull(indexed.getRGBWithTint());
      }
   }

   @Test
   void testRGBColour() throws IOException {
      try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50299.xlsx")) {

         // Check the CTColor is as expected
         XSSFColor rgb3 = wb.getCellStyleAt((short) 25).getFillForegroundXSSFColor();
         assertFalse(rgb3.getCTColor().isSetIndexed());
         assertEquals(0, rgb3.getCTColor().getIndexed());
         assertTrue(rgb3.getCTColor().isSetTint());
         assertEquals(-0.34999, rgb3.getCTColor().getTint(), 0.00001);
         assertTrue(rgb3.getCTColor().isSetRgb());
         assertEquals(3, rgb3.getCTColor().getRgb().length);

         // Now check the XSSFColor
         assertEquals(0, rgb3.getIndexed());
         assertEquals(-0.34999, rgb3.getTint(), 0.00001);
         assertFalse(rgb3.hasAlpha());
         assertTrue(rgb3.hasTint());

         assertEquals("FFFFFFFF", rgb3.getARGBHex());
         assertEquals(3, rgb3.getRGB().length);
         assertEquals(-1, rgb3.getRGB()[0]);
         assertEquals(-1, rgb3.getRGB()[1]);
         assertEquals(-1, rgb3.getRGB()[2]);

         assertEquals(4, rgb3.getARGB().length);
         assertEquals(-1, rgb3.getARGB()[0]);
         assertEquals(-1, rgb3.getARGB()[1]);
         assertEquals(-1, rgb3.getARGB()[2]);
         assertEquals(-1, rgb3.getARGB()[3]);

         // Tint doesn't have the alpha
         // tint = -0.34999
         // 255 * (1 + tint) = 165 truncated
         // or (byte) -91 (which is 165 - 256)
         assertEquals(3, rgb3.getRGBWithTint().length);
         assertEquals(-91, rgb3.getRGBWithTint()[0]);
         assertEquals(-91, rgb3.getRGBWithTint()[1]);
         assertEquals(-91, rgb3.getRGBWithTint()[2]);

         // Set the color to black (no theme).
         rgb3.setRGB(new byte[]{0, 0, 0});
         assertEquals("FF000000", rgb3.getARGBHex());
         assertEquals(0, rgb3.getCTColor().getRgb()[0]);
         assertEquals(0, rgb3.getCTColor().getRgb()[1]);
         assertEquals(0, rgb3.getCTColor().getRgb()[2]);

         // Set another, is fine
         rgb3.setRGB(new byte[]{16, 17, 18});
         assertFalse(rgb3.hasAlpha());
         assertEquals("FF101112", rgb3.getARGBHex());
         assertEquals(0x10, rgb3.getCTColor().getRgb()[0]);
         assertEquals(0x11, rgb3.getCTColor().getRgb()[1]);
         assertEquals(0x12, rgb3.getCTColor().getRgb()[2]);
      }
   }

   @Test
   void testARGBColour() throws IOException {
      try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48779.xlsx")) {

         // Check the CTColor is as expected
         XSSFColor rgb4 = wb.getCellStyleAt((short) 1).getFillForegroundXSSFColor();
         assertFalse(rgb4.getCTColor().isSetIndexed());
         assertEquals(0, rgb4.getCTColor().getIndexed());
         assertTrue(rgb4.getCTColor().isSetRgb());
         assertEquals(4, rgb4.getCTColor().getRgb().length);

         // Now check the XSSFColor
         assertEquals(0, rgb4.getIndexed());
         assertEquals(0.0, rgb4.getTint(), 0);
         assertFalse(rgb4.hasTint());
         assertTrue(rgb4.hasAlpha());

         assertEquals("FFFF0000", rgb4.getARGBHex());
         assertEquals(3, rgb4.getRGB().length);
         assertEquals(-1, rgb4.getRGB()[0]);
         assertEquals(0, rgb4.getRGB()[1]);
         assertEquals(0, rgb4.getRGB()[2]);

         assertEquals(4, rgb4.getARGB().length);
         assertEquals(-1, rgb4.getARGB()[0]);
         assertEquals(-1, rgb4.getARGB()[1]);
         assertEquals(0, rgb4.getARGB()[2]);
         assertEquals(0, rgb4.getARGB()[3]);

         // Tint doesn't have the alpha
         assertEquals(3, rgb4.getRGBWithTint().length);
         assertEquals(-1, rgb4.getRGBWithTint()[0]);
         assertEquals(0, rgb4.getRGBWithTint()[1]);
         assertEquals(0, rgb4.getRGBWithTint()[2]);


         // Turn on tinting, and check it behaves
         // TODO These values are suspected to be wrong...
         rgb4.setTint(0.4);
         assertTrue(rgb4.hasTint());
         assertEquals(0.4, rgb4.getTint(), 0);

         assertEquals(3, rgb4.getRGBWithTint().length);
         assertEquals(-1, rgb4.getRGBWithTint()[0]);
         assertEquals(102, rgb4.getRGBWithTint()[1]);
         assertEquals(102, rgb4.getRGBWithTint()[2]);
      }
   }

   @Test
   void testCustomIndexedColour() throws Exception {
       try (XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("customIndexedColors.xlsx")) {
          XSSFCell cell = wb.getSheetAt(1).getRow(0).getCell(0);
          XSSFColor color = cell.getCellStyle().getFillForegroundColorColor();
          CTColors ctColors = wb.getStylesSource().getCTStylesheet().getColors();

          CTRgbColor ctRgbColor = ctColors.getIndexedColors()
                  .getRgbColorList()
                  .get(color.getIndex());

          String hexRgb = ctRgbColor.getDomNode().getAttributes().getNamedItem("rgb").getNodeValue();

          assertEquals(hexRgb, color.getARGBHex());
       }
   }
}
