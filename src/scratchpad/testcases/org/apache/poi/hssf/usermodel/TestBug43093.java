package org.apache.poi.hssf.usermodel;

import junit.framework.TestCase;

public class TestBug43093 extends TestCase {

	private static void addNewSheetWithCellsA1toD4(HSSFWorkbook book, int sheet) {
		
		HSSFSheet sht = book .createSheet("s" + sheet);
		for     (short r=0; r < 4; r++) {
			
			HSSFRow   row = sht.createRow (r);
			for (short c=0; c < 4; c++) {
			
				HSSFCell cel = row.createCell(c);
				/**/     cel.setCellValue(sheet*100 + r*10 + c);
			}
		}
	}

	public void testBug43093() throws Exception {
			HSSFWorkbook     xlw    = new HSSFWorkbook();

			addNewSheetWithCellsA1toD4(xlw, 1);
			addNewSheetWithCellsA1toD4(xlw, 2);
			addNewSheetWithCellsA1toD4(xlw, 3);
			addNewSheetWithCellsA1toD4(xlw, 4);

			HSSFSheet s2   = xlw.getSheet("s2");
			HSSFRow   s2r3 = s2.getRow(3);
			HSSFCell  s2E4 = s2r3.createCell((short)4);
			/**/      s2E4.setCellFormula("SUM(s3!B2:C3)");

			HSSFFormulaEvaluator eva = new HSSFFormulaEvaluator(s2, xlw);
			eva.setCurrentRow(s2r3);
			double d = eva.evaluate(s2E4).getNumberValue();

			// internalEvaluate(...) Area3DEval.: 311+312+321+322 expected
			assertEquals(d, (double)(311+312+321+322), 0.0000001);
			// System.out.println("Area3DEval ok.: 311+312+321+322=" + d);
	}
}
