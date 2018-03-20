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

import java.io.IOException;

import org.apache.poi.ss.usermodel.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class TestHSSFOptimiser {
	@Test
	public void testDoesNoHarmIfNothingToDo() {
		HSSFWorkbook wb = new HSSFWorkbook();

		// New files start with 4 built in fonts, and 21 built in styles
      assertEquals(4, wb.getNumberOfFonts());
      assertEquals(21, wb.getNumCellStyles());

      // Create a test font and style, and use them
      HSSFFont f = wb.createFont();
		f.setFontName("Testing");
		HSSFCellStyle s = wb.createCellStyle();
		s.setFont(f);
		
		HSSFSheet sheet = wb.createSheet();
		HSSFRow row = sheet.createRow(0);
		row.createCell(0).setCellStyle(s);

		// Should have one more than the default of each
		assertEquals(5, wb.getNumberOfFonts());
		assertEquals(22, wb.getNumCellStyles());

		// Optimise fonts
		HSSFOptimiser.optimiseFonts(wb);

		assertEquals(5, wb.getNumberOfFonts());
		assertEquals(22, wb.getNumCellStyles());

		assertEquals(f, s.getFont(wb));

		// Optimise styles
		HSSFOptimiser.optimiseCellStyles(wb);

		assertEquals(5, wb.getNumberOfFonts());
		assertEquals(22, wb.getNumCellStyles());

		assertEquals(f, s.getFont(wb));
	}

	@Test
	public void testOptimiseFonts() {
		HSSFWorkbook wb = new HSSFWorkbook();

		// Add 6 fonts, some duplicates
		HSSFFont f1 = wb.createFont();
		f1.setFontHeight((short) 11);
		f1.setFontName("Testing");

		HSSFFont f2 = wb.createFont();
		f2.setFontHeight((short) 22);
		f2.setFontName("Also Testing");

		HSSFFont f3 = wb.createFont();
		f3.setFontHeight((short) 33);
		f3.setFontName("Unique");

		HSSFFont f4 = wb.createFont();
		f4.setFontHeight((short) 11);
		f4.setFontName("Testing");

		HSSFFont f5 = wb.createFont();
		f5.setFontHeight((short) 22);
		f5.setFontName("Also Testing");

		HSSFFont f6 = wb.createFont();
		f6.setFontHeight((short) 66);
		f6.setFontName("Also Unique");

		// Use all three of the four in cell styles
		assertEquals(21, wb.getNumCellStyles());

		HSSFCellStyle cs1 = wb.createCellStyle();
		cs1.setFont(f1);
		assertEquals(5, cs1.getFontIndex());

		HSSFCellStyle cs2 = wb.createCellStyle();
		cs2.setFont(f4);
		assertEquals(8, cs2.getFontIndex());

		HSSFCellStyle cs3 = wb.createCellStyle();
		cs3.setFont(f5);
		assertEquals(9, cs3.getFontIndex());

		HSSFCellStyle cs4 = wb.createCellStyle();
		cs4.setFont(f6);
		assertEquals(10, cs4.getFontIndex());

		assertEquals(25, wb.getNumCellStyles());

		// And three in rich text
		HSSFSheet s = wb.createSheet();
		HSSFRow r = s.createRow(0);

		HSSFRichTextString rtr1 = new HSSFRichTextString("Test");
		rtr1.applyFont(0, 2, f1);
		rtr1.applyFont(3, 4, f2);
		r.createCell(0).setCellValue(rtr1);

		HSSFRichTextString rtr2 = new HSSFRichTextString("AlsoTest");
		rtr2.applyFont(0, 2, f3);
		rtr2.applyFont(3, 5, f5);
		rtr2.applyFont(6, 8, f6);
		r.createCell(1).setCellValue(rtr2);

		// Check what we have now
		assertEquals(10, wb.getNumberOfFonts());
		assertEquals(25, wb.getNumCellStyles());

		// Optimise
		HSSFOptimiser.optimiseFonts(wb);

		// Check font count
		assertEquals(8, wb.getNumberOfFonts());
		assertEquals(25, wb.getNumCellStyles());

		// Check font use in cell styles
		assertEquals(5, cs1.getFontIndex());
		assertEquals(5, cs2.getFontIndex()); // duplicate of 1
		assertEquals(6, cs3.getFontIndex()); // duplicate of 2
		assertEquals(8, cs4.getFontIndex()); // two have gone

		// And in rich text

		// RTR 1 had f1 and f2, unchanged
		assertEquals(5, r.getCell(0).getRichStringCellValue().getFontAtIndex(0));
		assertEquals(5, r.getCell(0).getRichStringCellValue().getFontAtIndex(1));
		assertEquals(6, r.getCell(0).getRichStringCellValue().getFontAtIndex(3));
		assertEquals(6, r.getCell(0).getRichStringCellValue().getFontAtIndex(4));

		// RTR 2 had f3 (unchanged), f5 (=f2) and f6 (moved down)
		assertEquals(7, r.getCell(1).getRichStringCellValue().getFontAtIndex(0));
		assertEquals(7, r.getCell(1).getRichStringCellValue().getFontAtIndex(1));
		assertEquals(6, r.getCell(1).getRichStringCellValue().getFontAtIndex(3));
		assertEquals(6, r.getCell(1).getRichStringCellValue().getFontAtIndex(4));
		assertEquals(8, r.getCell(1).getRichStringCellValue().getFontAtIndex(6));
		assertEquals(8, r.getCell(1).getRichStringCellValue().getFontAtIndex(7));
	}

	@Test
	public void testOptimiseStyles() {
	    HSSFWorkbook wb = new HSSFWorkbook();

	    // Two fonts
	    assertEquals(4, wb.getNumberOfFonts());

	    HSSFFont f1 = wb.createFont();
	    f1.setFontHeight((short) 11);
	    f1.setFontName("Testing");

	    HSSFFont f2 = wb.createFont();
	    f2.setFontHeight((short) 22);
	    f2.setFontName("Also Testing");

	    assertEquals(6, wb.getNumberOfFonts());

	    // Several styles
	    assertEquals(21, wb.getNumCellStyles());

	    HSSFCellStyle cs1 = wb.createCellStyle();
	    cs1.setFont(f1);

	    HSSFCellStyle cs2 = wb.createCellStyle();
	    cs2.setFont(f2);

	    HSSFCellStyle cs3 = wb.createCellStyle();
	    cs3.setFont(f1);

	    HSSFCellStyle cs4 = wb.createCellStyle();
	    cs4.setFont(f1);
	    cs4.setAlignment(HorizontalAlignment.CENTER);

	    HSSFCellStyle cs5 = wb.createCellStyle();
	    cs5.setFont(f2);
	    cs5.setAlignment(HorizontalAlignment.FILL);

	    HSSFCellStyle cs6 = wb.createCellStyle();
	    cs6.setFont(f2);

	    assertEquals(27, wb.getNumCellStyles());

	    // Use them
	    HSSFSheet s = wb.createSheet();
	    HSSFRow r = s.createRow(0);

	    r.createCell(0).setCellStyle(cs1);
	    r.createCell(1).setCellStyle(cs2);
	    r.createCell(2).setCellStyle(cs3);
	    r.createCell(3).setCellStyle(cs4);
	    r.createCell(4).setCellStyle(cs5);
	    r.createCell(5).setCellStyle(cs6);
	    r.createCell(6).setCellStyle(cs1);
	    r.createCell(7).setCellStyle(cs2);

	    assertEquals(21, r.getCell(0).getCellValueRecord().getXFIndex());
	    assertEquals(26, r.getCell(5).getCellValueRecord().getXFIndex());
	    assertEquals(21, r.getCell(6).getCellValueRecord().getXFIndex());

	    // Optimise
	    HSSFOptimiser.optimiseCellStyles(wb);

	    // Check
	    assertEquals(6, wb.getNumberOfFonts());
	    assertEquals(25, wb.getNumCellStyles());

	    // cs1 -> 21
	    assertEquals(21, r.getCell(0).getCellValueRecord().getXFIndex());
	    // cs2 -> 22
	    assertEquals(22, r.getCell(1).getCellValueRecord().getXFIndex());
	    assertEquals(22, r.getCell(1).getCellStyle().getFont(wb).getFontHeight());
	    // cs3 = cs1 -> 21
	    assertEquals(21, r.getCell(2).getCellValueRecord().getXFIndex());
	    // cs4 --> 24 -> 23
	    assertEquals(23, r.getCell(3).getCellValueRecord().getXFIndex());
	    // cs5 --> 25 -> 24
	    assertEquals(24, r.getCell(4).getCellValueRecord().getXFIndex());
	    // cs6 = cs2 -> 22
	    assertEquals(22, r.getCell(5).getCellValueRecord().getXFIndex());
	    // cs1 -> 21
	    assertEquals(21, r.getCell(6).getCellValueRecord().getXFIndex());
	    // cs2 -> 22
	    assertEquals(22, r.getCell(7).getCellValueRecord().getXFIndex());


	    // Add a new duplicate, and two that aren't used
	    HSSFCellStyle csD = wb.createCellStyle();
	    csD.setFont(f1);
	    r.createCell(8).setCellStyle(csD);

	    HSSFFont f3 = wb.createFont();
	    f3.setFontHeight((short) 23);
	    f3.setFontName("Testing 3");
	    HSSFFont f4 = wb.createFont();
	    f4.setFontHeight((short) 24);
	    f4.setFontName("Testing 4");

	    HSSFCellStyle csU1 = wb.createCellStyle();
	    csU1.setFont(f3);
	    HSSFCellStyle csU2 = wb.createCellStyle();
	    csU2.setFont(f4);

	    // Check before the optimise
	    assertEquals(8, wb.getNumberOfFonts());
	    assertEquals(28, wb.getNumCellStyles());

	    // Optimise, should remove the two un-used ones and the one duplicate
	    HSSFOptimiser.optimiseCellStyles(wb);

	    // Check
	    assertEquals(8, wb.getNumberOfFonts());
	    assertEquals(25, wb.getNumCellStyles());

	    // csD -> cs1 -> 21
	    assertEquals(21, r.getCell(8).getCellValueRecord().getXFIndex());
	}

	@Test
	public void testOptimiseStylesCheckActualStyles() {
	    HSSFWorkbook wb = new HSSFWorkbook();
	    
	    // Several styles
	    assertEquals(21, wb.getNumCellStyles());
	    
	    HSSFCellStyle cs1 = wb.createCellStyle();
	    cs1.setBorderBottom(BorderStyle.THICK);
	    
	    HSSFCellStyle cs2 = wb.createCellStyle();
	    cs2.setBorderBottom(BorderStyle.DASH_DOT);
	    
	    HSSFCellStyle cs3 = wb.createCellStyle(); // = cs1
	    cs3.setBorderBottom(BorderStyle.THICK);
	    
	    assertEquals(24, wb.getNumCellStyles());
	    
	    // Use them
	    HSSFSheet s = wb.createSheet();
	    HSSFRow r = s.createRow(0);
	    
	    r.createCell(0).setCellStyle(cs1);
	    r.createCell(1).setCellStyle(cs2);
	    r.createCell(2).setCellStyle(cs3);
	    
	    assertEquals(21, r.getCell(0).getCellValueRecord().getXFIndex());
	    assertEquals(22, r.getCell(1).getCellValueRecord().getXFIndex());
	    assertEquals(23, r.getCell(2).getCellValueRecord().getXFIndex());
	    
	    // Optimise
	    HSSFOptimiser.optimiseCellStyles(wb);
	    
	    // Check
	    assertEquals(23, wb.getNumCellStyles());
	    
	    assertEquals(BorderStyle.THICK, r.getCell(0).getCellStyle().getBorderBottom());
	    assertEquals(BorderStyle.DASH_DOT, r.getCell(1).getCellStyle().getBorderBottom());
	    assertEquals(BorderStyle.THICK, r.getCell(2).getCellStyle().getBorderBottom());
	}

	@Test
	public void testColumnAndRowStyles() {
		HSSFWorkbook wb = new HSSFWorkbook();
		assertEquals("Usually we have 21 pre-defined styles in a newly created Workbook, see InternalWorkbook.createWorkbook()",
				21, wb.getNumCellStyles());

		HSSFSheet sheet = wb.createSheet();

		Row row = sheet.createRow(0);
		row.createCell(0);
		row.createCell(1);
		row.setRowStyle(createColorStyle(wb, IndexedColors.RED));

		row = sheet.createRow(1);
		row.createCell(0);
		row.createCell(1);
		row.setRowStyle(createColorStyle(wb, IndexedColors.RED));

		sheet.setDefaultColumnStyle(0, createColorStyle(wb, IndexedColors.RED));
		sheet.setDefaultColumnStyle(1, createColorStyle(wb, IndexedColors.RED));

		// now the color should be equal for those two columns and rows
		checkColumnStyles(sheet, 0, 1, false);
		checkRowStyles(sheet, 0, 1, false);

		// Optimise styles
		HSSFOptimiser.optimiseCellStyles(wb);

		// We should have the same style-objects for these two columns and rows
		checkColumnStyles(sheet, 0, 1, true);
		checkRowStyles(sheet, 0, 1, true);
	}

	@Test
	public void testUnusedStyle() {
		HSSFWorkbook wb = new HSSFWorkbook();
		assertEquals("Usually we have 21 pre-defined styles in a newly created Workbook, see InternalWorkbook.createWorkbook()",
				21, wb.getNumCellStyles());

		HSSFSheet sheet = wb.createSheet();

		Row row = sheet.createRow(0);
		row.createCell(0);
		row.createCell(1).setCellStyle(
				createColorStyle(wb, IndexedColors.GREEN));


		row = sheet.createRow(1);
		row.createCell(0);
		row.createCell(1).setCellStyle(
				createColorStyle(wb, IndexedColors.RED));


		// Create style. But don't use it.
		for (int i = 0; i < 3; i++) {
			// Set Cell Color : AQUA
			createColorStyle(wb, IndexedColors.AQUA);
		}

		assertEquals(21 + 2 + 3, wb.getNumCellStyles());
		assertEquals(IndexedColors.GREEN.getIndex(), sheet.getRow(0).getCell(1).getCellStyle().getFillForegroundColor());
		assertEquals(IndexedColors.RED.getIndex(), sheet.getRow(1).getCell(1).getCellStyle().getFillForegroundColor());

		// Optimise styles
		HSSFOptimiser.optimiseCellStyles(wb);

		assertEquals(21 + 2, wb.getNumCellStyles());
		assertEquals(IndexedColors.GREEN.getIndex(), sheet.getRow(0).getCell(1).getCellStyle().getFillForegroundColor());
		assertEquals(IndexedColors.RED.getIndex(), sheet.getRow(1).getCell(1).getCellStyle().getFillForegroundColor());
	}

	@Test
	public void testUnusedStyleOneUsed() {
		HSSFWorkbook wb = new HSSFWorkbook();
		assertEquals("Usually we have 21 pre-defined styles in a newly created Workbook, see InternalWorkbook.createWorkbook()",
				21, wb.getNumCellStyles());

		HSSFSheet sheet = wb.createSheet();

		Row row = sheet.createRow(0);
		row.createCell(0);
		row.createCell(1).setCellStyle(
				createColorStyle(wb, IndexedColors.GREEN));

		// Create style. But don't use it.
		for (int i = 0; i < 3; i++) {
			// Set Cell Color : AQUA
			createColorStyle(wb, IndexedColors.AQUA);
		}

		row = sheet.createRow(1);
		row.createCell(0).setCellStyle(createColorStyle(wb, IndexedColors.AQUA));
		row.createCell(1).setCellStyle(
				createColorStyle(wb, IndexedColors.RED));

		assertEquals(21 + 3 + 3, wb.getNumCellStyles());
		assertEquals(IndexedColors.GREEN.getIndex(), sheet.getRow(0).getCell(1).getCellStyle().getFillForegroundColor());
		assertEquals(IndexedColors.AQUA.getIndex(), sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor());
		assertEquals(IndexedColors.RED.getIndex(), sheet.getRow(1).getCell(1).getCellStyle().getFillForegroundColor());

		// Optimise styles
		HSSFOptimiser.optimiseCellStyles(wb);

		assertEquals(21 + 3, wb.getNumCellStyles());
		assertEquals(IndexedColors.GREEN.getIndex(), sheet.getRow(0).getCell(1).getCellStyle().getFillForegroundColor());
		assertEquals(IndexedColors.AQUA.getIndex(), sheet.getRow(1).getCell(0).getCellStyle().getFillForegroundColor());
		assertEquals(IndexedColors.RED.getIndex(), sheet.getRow(1).getCell(1).getCellStyle().getFillForegroundColor());
	}

	@Test
    public void testDefaultColumnStyleWitoutCell() throws IOException {
        HSSFWorkbook wb = new HSSFWorkbook();
        assertEquals("Usually we have 21 pre-defined styles in a newly created Workbook, see InternalWorkbook.createWorkbook()",
        		21, wb.getNumCellStyles());

		HSSFSheet sheet = wb.createSheet();

        //Set CellStyle and RowStyle and ColumnStyle
        for (int i = 0; i < 2; i++) {
			sheet.createRow(i);
		}

        // Create a test font and style, and use them
		int obj_cnt = wb.getNumCellStyles();
		int cnt = wb.getNumCellStyles();

		// Set Column Color : Red
		sheet.setDefaultColumnStyle(3,
				createColorStyle(wb, IndexedColors.RED));
		obj_cnt++;

		// Set Column Color : Red
		sheet.setDefaultColumnStyle(4,
				createColorStyle(wb, IndexedColors.RED));
		obj_cnt++;

        assertEquals(obj_cnt, wb.getNumCellStyles());

        // now the color should be equal for those two columns and rows
		checkColumnStyles(sheet, 3, 4, false);

        // Optimise styles
        HSSFOptimiser.optimiseCellStyles(wb);

		// We should have the same style-objects for these two columns and rows
		checkColumnStyles(sheet, 3, 4, true);

        // (GREEN + RED + BLUE + CORAL) + YELLOW(2*2)
        assertEquals(cnt + 1, wb.getNumCellStyles());
    }

	@Test
	public void testUserDefinedStylesAreNeverOptimizedAway() throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook();
		assertEquals("Usually we have 21 pre-defined styles in a newly created Workbook, see InternalWorkbook.createWorkbook()",
				21, wb.getNumCellStyles());

		HSSFSheet sheet = wb.createSheet();

		//Set CellStyle and RowStyle and ColumnStyle
		for (int i = 0; i < 2; i++) {
			sheet.createRow(i);
		}

		// Create a test font and style, and use them
		int obj_cnt = wb.getNumCellStyles();
		int cnt = wb.getNumCellStyles();
		for (int i = 0; i < 3; i++) {
			HSSFCellStyle s = null;
			if (i == 0) {
				// Set cell color : +2(user style + proxy of it)
				s = (HSSFCellStyle) createColorStyle(wb,
						IndexedColors.YELLOW);
				s.setUserStyleName("user define");
				obj_cnt += 2;
			}

			HSSFRow row = sheet.getRow(1);
			row.createCell(i).setCellStyle(s);
		}

		// Create style. But don't use it.
		for (int i = 3; i < 6; i++) {
			// Set Cell Color : AQUA
			createColorStyle(wb, IndexedColors.AQUA);
			obj_cnt++;
		}

		// Set cell color : +2(user style + proxy of it)
		HSSFCellStyle s = (HSSFCellStyle) createColorStyle(wb,IndexedColors.YELLOW);
		s.setUserStyleName("user define2");
		obj_cnt += 2;

		sheet.createRow(10).createCell(0).setCellStyle(s);

		assertEquals(obj_cnt, wb.getNumCellStyles());

		// Confirm user style name
		checkUserStyles(sheet);

		// Optimise styles
		HSSFOptimiser.optimiseCellStyles(wb);

		// Confirm user style name
		checkUserStyles(sheet);

		// (GREEN + RED + BLUE + CORAL) + YELLOW(2*2)
		assertEquals(cnt + 2 * 2, wb.getNumCellStyles());
	}

	@Test
	public void testBug57517() throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook();
		assertEquals("Usually we have 21 pre-defined styles in a newly created Workbook, see InternalWorkbook.createWorkbook()",
				21, wb.getNumCellStyles());

		HSSFSheet sheet = wb.createSheet();

		//Set CellStyle and RowStyle and ColumnStyle
		for (int i = 0; i < 2; i++) {
			sheet.createRow(i);
		}

		// Create a test font and style, and use them
		int obj_cnt = wb.getNumCellStyles();
		int cnt = wb.getNumCellStyles();
		for (int i = 0; i < 3; i++) {
			// Set Cell Color : GREEN
			HSSFRow row = sheet.getRow(0);
			row.createCell(i).setCellStyle(
					createColorStyle(wb, IndexedColors.GREEN));
			obj_cnt++;

			// Set Column Color : Red
			sheet.setDefaultColumnStyle(i + 3,
					createColorStyle(wb, IndexedColors.RED));
			obj_cnt++;

			// Set Row Color : Blue
			row = sheet.createRow(i + 3);
			row.setRowStyle(createColorStyle(wb, IndexedColors.BLUE));
			obj_cnt++;

			HSSFCellStyle s = null;
			if (i == 0) {
				// Set cell color : +2(user style + proxy of it)
				s = (HSSFCellStyle) createColorStyle(wb,
						IndexedColors.YELLOW);
				s.setUserStyleName("user define");
				obj_cnt += 2;
			}

			row = sheet.getRow(1);
			row.createCell(i).setCellStyle(s);

		}

		// Create style. But don't use it.
		for (int i = 3; i < 6; i++) {
			// Set Cell Color : AQUA
			createColorStyle(wb, IndexedColors.AQUA);
			obj_cnt++;
		}

		// Set CellStyle and RowStyle and ColumnStyle
		for (int i = 9; i < 11; i++) {
			sheet.createRow(i);
		}

		//Set 0 or 255 index of ColumnStyle.
		HSSFCellStyle s = (HSSFCellStyle) createColorStyle(wb, IndexedColors.CORAL);
		obj_cnt++;
		sheet.setDefaultColumnStyle(0, s);
		sheet.setDefaultColumnStyle(255, s);

		// Create a test font and style, and use them
		for (int i = 3; i < 6; i++) {
			// Set Cell Color : GREEN
			HSSFRow row = sheet.getRow(0 + 9);
			row.createCell(i - 3).setCellStyle(
					createColorStyle(wb, IndexedColors.GREEN));
			obj_cnt++;

			// Set Column Color : Red
			sheet.setDefaultColumnStyle(i + 3,
					createColorStyle(wb, IndexedColors.RED));
			obj_cnt++;

			// Set Row Color : Blue
			row = sheet.createRow(i + 3);
			row.setRowStyle(createColorStyle(wb, IndexedColors.BLUE));
			obj_cnt++;

			if (i == 3) {
				// Set cell color : +2(user style + proxy of it)
				s = (HSSFCellStyle) createColorStyle(wb,
						IndexedColors.YELLOW);
				s.setUserStyleName("user define2");
				obj_cnt += 2;
			}

			row = sheet.getRow(1 + 9);
			row.createCell(i - 3).setCellStyle(s);
		}

		assertEquals(obj_cnt, wb.getNumCellStyles());

		// now the color should be equal for those two columns and rows
		checkColumnStyles(sheet, 3, 4, false);
		checkRowStyles(sheet, 3, 4, false);

		// Confirm user style name
		checkUserStyles(sheet);

//        out = new FileOutputStream(new File(tmpDirName, "out.xls"));
//        wb.write(out);
//        out.close();

		// Optimise styles
		HSSFOptimiser.optimiseCellStyles(wb);

//        out = new FileOutputStream(new File(tmpDirName, "out_optimised.xls"));
//        wb.write(out);
//        out.close();

		// We should have the same style-objects for these two columns and rows
		checkColumnStyles(sheet, 3, 4, true);
		checkRowStyles(sheet, 3, 4, true);

		// Confirm user style name
		checkUserStyles(sheet);

		// (GREEN + RED + BLUE + CORAL) + YELLOW(2*2)
		assertEquals(cnt + 4 + 2 * 2, wb.getNumCellStyles());
	}

	private void checkUserStyles(HSSFSheet sheet) {
		HSSFCellStyle parentStyle1 = sheet.getRow(1).getCell(0).getCellStyle().getParentStyle();
		assertNotNull(parentStyle1);
		assertEquals(parentStyle1.getUserStyleName(), "user define");

		HSSFCellStyle parentStyle10 = sheet.getRow(10).getCell(0).getCellStyle().getParentStyle();
		assertNotNull(parentStyle10);
		assertEquals(parentStyle10.getUserStyleName(), "user define2");
	}

	private void checkColumnStyles(HSSFSheet sheet, int col1, int col2, boolean checkEquals) {
		// we should have the same color for the column styles
		HSSFCellStyle columnStyle1 = sheet.getColumnStyle(col1);
		assertNotNull(columnStyle1);
		HSSFCellStyle columnStyle2 = sheet.getColumnStyle(col2);
		assertNotNull(columnStyle2);
		assertEquals(columnStyle1.getFillForegroundColor(), columnStyle2.getFillForegroundColor());
		if(checkEquals) {
			assertEquals(columnStyle1.getIndex(), columnStyle2.getIndex());
			assertEquals(columnStyle1, columnStyle2);
		}
	}

	private void checkRowStyles(HSSFSheet sheet, int row1, int row2, boolean checkEquals) {
		// we should have the same color for the row styles
		HSSFCellStyle rowStyle1 = sheet.getRow(row1).getRowStyle();
		assertNotNull(rowStyle1);
		HSSFCellStyle rowStyle2 = sheet.getRow(row2).getRowStyle();
		assertNotNull(rowStyle2);
		assertEquals(rowStyle1.getFillForegroundColor(), rowStyle2.getFillForegroundColor());
		if(checkEquals) {
			assertEquals(rowStyle1.getIndex(), rowStyle2.getIndex());
			assertEquals(rowStyle1, rowStyle2);
		}
	}

	private CellStyle createColorStyle(Workbook wb, IndexedColors c) {
        CellStyle cs = wb.createCellStyle();
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setFillForegroundColor(c.getIndex());
        return cs;
    }
}
