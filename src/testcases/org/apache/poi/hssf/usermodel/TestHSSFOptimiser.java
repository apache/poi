/* ====================================================================
   Copyright 2002-2004   Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

public class TestHSSFOptimiser extends TestCase {
	public void testDoesNoHarmIfNothingToDo() throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook();
		
		HSSFFont f = wb.createFont();
		f.setFontName("Testing");
		HSSFCellStyle s = wb.createCellStyle();
		s.setFont(f);
		
		assertEquals(5, wb.getNumberOfFonts());
		assertEquals(22, wb.getNumCellStyles());
		
		// Optimise fonts
		HSSFOptimiser.optimiseFonts(wb);
		
		assertEquals(5, wb.getNumberOfFonts());
		assertEquals(22, wb.getNumCellStyles());
		
		assertEquals(f, s.getFont(wb));
		
		// Optimise styles
//		HSSFOptimiser.optimiseCellStyles(wb);
		
		assertEquals(5, wb.getNumberOfFonts());
		assertEquals(22, wb.getNumCellStyles());
		
		assertEquals(f, s.getFont(wb));
	}
	
	public void testOptimiseFonts() throws Exception {
		HSSFWorkbook wb = new HSSFWorkbook();
		
		// Add 6 fonts, some duplicates
		HSSFFont f1 = wb.createFont();
		f1.setFontHeight((short)11);
		f1.setFontName("Testing");
		
		HSSFFont f2 = wb.createFont();
		f2.setFontHeight((short)22);
		f2.setFontName("Also Testing");
		
		HSSFFont f3 = wb.createFont();
		f3.setFontHeight((short)33);
		f3.setFontName("Unique");
		
		HSSFFont f4 = wb.createFont();
		f4.setFontHeight((short)11);
		f4.setFontName("Testing");
		
		HSSFFont f5 = wb.createFont();
		f5.setFontHeight((short)22);
		f5.setFontName("Also Testing");
		
		HSSFFont f6 = wb.createFont();
		f6.setFontHeight((short)66);
		f6.setFontName("Also Unique");
		
		
		
		// Use all three of the four in cell styles
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
		
		
		// And three in rich text
		HSSFSheet s = wb.createSheet();
		HSSFRow r = s.createRow(0);
		
		HSSFRichTextString rtr1 = new HSSFRichTextString("Test");
		rtr1.applyFont(0, 2, f1);
		rtr1.applyFont(3, 4, f2);
		r.createCell((short)0).setCellValue(rtr1);
		
		HSSFRichTextString rtr2 = new HSSFRichTextString("AlsoTest");
		rtr2.applyFont(0, 2, f3);
		rtr2.applyFont(3, 5, f5);
		rtr2.applyFont(6, 8, f6);
		r.createCell((short)1).setCellValue(rtr2);
		
		
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
}
