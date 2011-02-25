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

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.XSSFITestDataProvider;
import org.apache.poi.xssf.XSSFTestDataSamples;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontSize;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIntProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTUnderlineProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTVerticalAlignFontProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnderlineValues;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignRun;

public final class TestXSSFColor extends TestCase {
   public void testIndexedColour() throws Exception {
      XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48779.xlsx");
      
      // Check the CTColor is as expected
      XSSFColor indexed = wb.getCellStyleAt((short)1).getFillBackgroundXSSFColor();
      assertEquals(true, indexed.getCTColor().isSetIndexed());
      assertEquals(64, indexed.getCTColor().getIndexed());
      assertEquals(false, indexed.getCTColor().isSetRgb());
      assertEquals(null, indexed.getCTColor().getRgb());
      
      // Now check the XSSFColor
      // Note - 64 is a special "auto" one with no rgb equiv
      assertEquals(64, indexed.getIndexed());
      assertEquals(null, indexed.getRgb());
      assertEquals(null, indexed.getRgbWithTint());
      assertEquals(null, indexed.getARGBHex());
      
      // Now move to one with indexed rgb values
      indexed.setIndexed(59);
      assertEquals(true, indexed.getCTColor().isSetIndexed());
      assertEquals(59, indexed.getCTColor().getIndexed());
      assertEquals(false, indexed.getCTColor().isSetRgb());
      assertEquals(null, indexed.getCTColor().getRgb());
      
      assertEquals(59, indexed.getIndexed());
      assertEquals("FF333300", indexed.getARGBHex());
      
      assertEquals(3, indexed.getRgb().length);
      assertEquals(0x33, indexed.getRgb()[0]);
      assertEquals(0x33, indexed.getRgb()[1]);
      assertEquals(0x00, indexed.getRgb()[2]);
      
      assertEquals(4, indexed.getARgb().length);
      assertEquals(-1, indexed.getARgb()[0]);
      assertEquals(0x33, indexed.getARgb()[1]);
      assertEquals(0x33, indexed.getARgb()[2]);
      assertEquals(0x00, indexed.getARgb()[3]);
      
      // You don't get tinted indexed colours, sorry...
      assertEquals(null, indexed.getRgbWithTint());
   }
   
   public void testRGBColour() throws Exception {
      XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("50299.xlsx");
      
      // Check the CTColor is as expected
      XSSFColor rgb3 = wb.getCellStyleAt((short)25).getFillForegroundXSSFColor();
      assertEquals(false, rgb3.getCTColor().isSetIndexed());
      assertEquals(0,     rgb3.getCTColor().getIndexed());
      assertEquals(true,  rgb3.getCTColor().isSetTint());
      assertEquals(-0.34999, rgb3.getCTColor().getTint(), 0.00001);
      assertEquals(true,  rgb3.getCTColor().isSetRgb());
      assertEquals(3,     rgb3.getCTColor().getRgb().length);
      
      // Now check the XSSFColor
      assertEquals(0, rgb3.getIndexed());
      assertEquals(-0.34999, rgb3.getTint(), 0.00001);
      
      assertEquals("FFFFFFFF", rgb3.getARGBHex());
      assertEquals(3, rgb3.getRgb().length);
      assertEquals(-1, rgb3.getRgb()[0]);
      assertEquals(-1, rgb3.getRgb()[1]);
      assertEquals(-1,  rgb3.getRgb()[2]);
      
      assertEquals(4, rgb3.getARgb().length);
      assertEquals(-1, rgb3.getARgb()[0]);
      assertEquals(-1, rgb3.getARgb()[1]);
      assertEquals(-1,  rgb3.getARgb()[2]);
      assertEquals(-1,  rgb3.getARgb()[3]);
      
      // Tint doesn't have the alpha
      assertEquals(3, rgb3.getRgbWithTint().length);
      assertEquals(0, rgb3.getRgbWithTint()[0]);
      assertEquals(0,  rgb3.getRgbWithTint()[1]);
      assertEquals(0,  rgb3.getRgbWithTint()[2]);
   }
   
   public void testARGBColour() throws Exception {
      XSSFWorkbook wb = XSSFTestDataSamples.openSampleWorkbook("48779.xlsx");
      
      // Check the CTColor is as expected
      XSSFColor rgb4 = wb.getCellStyleAt((short)1).getFillForegroundXSSFColor();
      assertEquals(false, rgb4.getCTColor().isSetIndexed());
      assertEquals(0,     rgb4.getCTColor().getIndexed());
      assertEquals(true, rgb4.getCTColor().isSetRgb());
      assertEquals(4, rgb4.getCTColor().getRgb().length);
      
      // Now check the XSSFColor
      assertEquals(0, rgb4.getIndexed());
      assertEquals(0.0, rgb4.getTint());
      
      assertEquals("FFFF0000", rgb4.getARGBHex());
      assertEquals(3, rgb4.getRgb().length);
      assertEquals(-1, rgb4.getRgb()[0]);
      assertEquals(0,  rgb4.getRgb()[1]);
      assertEquals(0,  rgb4.getRgb()[2]);
      
      assertEquals(4, rgb4.getARgb().length);
      assertEquals(-1, rgb4.getARgb()[0]);
      assertEquals(-1, rgb4.getARgb()[1]);
      assertEquals(0,  rgb4.getARgb()[2]);
      assertEquals(0,  rgb4.getARgb()[3]);
      
      // Tint doesn't have the alpha
      assertEquals(3, rgb4.getRgbWithTint().length);
      assertEquals(-1, rgb4.getRgbWithTint()[0]);
      assertEquals(0,  rgb4.getRgbWithTint()[1]);
      assertEquals(0,  rgb4.getRgbWithTint()[2]);

      
      // Turn on tinting, and check it behaves
      // TODO These values are suspected to be wrong...
      rgb4.setTint(0.4);
      assertEquals(0.4, rgb4.getTint());
      
      assertEquals(3, rgb4.getRgbWithTint().length);
      assertEquals(-1, rgb4.getRgbWithTint()[0]);
      assertEquals(102,  rgb4.getRgbWithTint()[1]);
      assertEquals(102,  rgb4.getRgbWithTint()[2]);
   }
}
