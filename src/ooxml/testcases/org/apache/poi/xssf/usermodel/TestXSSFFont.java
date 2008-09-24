package org.apache.poi.xssf.usermodel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.extensions.XSSFColor;
import org.openxml4j.opc.Package;
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

public class TestXSSFFont extends TestCase{

	public void testConstructor(){
		XSSFFont xssfFont=new XSSFFont();
		assertNotNull(xssfFont);
		assertNotNull(xssfFont.getCTFont());
	}


	public void testBoldweight(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTBooleanProperty bool=ctFont.addNewB();
		bool.setVal(false);
		ctFont.setBArray(0,bool);
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(false, xssfFont.getBold());


		xssfFont.setBold(true);
		assertEquals(ctFont.getBArray().length,1);
		assertEquals(true, ctFont.getBArray(0).getVal());
	}

	public void testCharSet(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTIntProperty prop=ctFont.addNewCharset();
		prop.setVal(FontCharset.ANSI.getValue());

		ctFont.setCharsetArray(0,prop);
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.ANSI_CHARSET,xssfFont.getCharSet());

		xssfFont.setCharSet(FontCharset.DEFAULT);
		assertEquals(FontCharset.DEFAULT.getValue(),ctFont.getCharsetArray(0).getVal());
	}


	public void testFontName(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontName fname=ctFont.addNewName();
		fname.setVal("Arial");
		ctFont.setNameArray(0,fname);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals("Arial", xssfFont.getFontName());

		xssfFont.setFontName("Courier");
		assertEquals("Courier",ctFont.getNameArray(0).getVal());
	}


	public void testItalic(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTBooleanProperty bool=ctFont.addNewI();
		bool.setVal(false);
		ctFont.setIArray(0,bool);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(false, xssfFont.getItalic());

		xssfFont.setItalic(true);
		assertEquals(ctFont.getIArray().length,1);
		assertEquals(true, ctFont.getIArray(0).getVal());
		assertEquals(true,ctFont.getIArray(0).getVal());
	}


	public void testStrikeout(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTBooleanProperty bool=ctFont.addNewStrike();
		bool.setVal(false);
		ctFont.setStrikeArray(0,bool);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(false, xssfFont.getStrikeout());

		xssfFont.setStrikeout(true);
		assertEquals(ctFont.getStrikeArray().length,1);
		assertEquals(true, ctFont.getStrikeArray(0).getVal());
		assertEquals(true,ctFont.getStrikeArray(0).getVal());
	}


	public void testFontHeight(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontSize size=ctFont.addNewSz();
		size.setVal(11);
		ctFont.setSzArray(0,size);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(11,xssfFont.getFontHeight());

		xssfFont.setFontHeight((short)20);
		assertEquals(new Double(20).doubleValue(),ctFont.getSzArray(0).getVal());
	}


	public void testFontHeightInPoint(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontSize size=ctFont.addNewSz();
		size.setVal(14);
		ctFont.setSzArray(0,size);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(14,xssfFont.getFontHeightInPoints());

		xssfFont.setFontHeightInPoints((short)20);
		assertEquals(new Double(20).doubleValue(),ctFont.getSzArray(0).getVal());
	}


	public void testUnderline(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTUnderlineProperty underlinePropr=ctFont.addNewU();
		underlinePropr.setVal(STUnderlineValues.SINGLE);
		ctFont.setUArray(0,underlinePropr);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.U_SINGLE, xssfFont.getUnderline());

		xssfFont.setUnderline(Font.U_DOUBLE);
		assertEquals(ctFont.getUArray().length,1);
		assertEquals(STUnderlineValues.DOUBLE,ctFont.getUArray(0).getVal());
		
		xssfFont.setUnderline(FontUnderline.DOUBLE_ACCOUNTING);
		assertEquals(ctFont.getUArray().length,1);
		assertEquals(STUnderlineValues.DOUBLE_ACCOUNTING,ctFont.getUArray(0).getVal());
	}

	public void testColor(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTColor color=ctFont.addNewColor();
		color.setIndexed(XSSFFont.DEFAULT_FONT_COLOR);
		ctFont.setColorArray(0,color);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(IndexedColors.BLACK.getIndex(),xssfFont.getColor());

		xssfFont.setColor(IndexedColors.RED.getIndex());
		assertEquals(IndexedColors.RED.getIndex(), ctFont.getColorArray(0).getIndexed());
	}

	public void testRgbColor(){
	    CTFont ctFont=CTFont.Factory.newInstance();
	    CTColor color=ctFont.addNewColor();

	    color.setRgb(Integer.toHexString(0xFFFFFF).getBytes());
	    ctFont.setColorArray(0,color);

	    XSSFFont xssfFont=new XSSFFont(ctFont);
	    assertEquals(ctFont.getColorArray(0).getRgb()[0],xssfFont.getRgbColor().getRgb()[0]);
	    assertEquals(ctFont.getColorArray(0).getRgb()[1],xssfFont.getRgbColor().getRgb()[1]);
	    assertEquals(ctFont.getColorArray(0).getRgb()[2],xssfFont.getRgbColor().getRgb()[2]);
	    assertEquals(ctFont.getColorArray(0).getRgb()[3],xssfFont.getRgbColor().getRgb()[3]);

	    color.setRgb(Integer.toHexString(0xF1F1F1).getBytes());
	    XSSFColor newColor=new XSSFColor(color);
	    xssfFont.setRgbColor(newColor);
	    assertEquals(ctFont.getColorArray(0).getRgb()[2],newColor.getRgb()[2]);
	}

	public void testThemeColor(){
	    CTFont ctFont=CTFont.Factory.newInstance();
	    CTColor color=ctFont.addNewColor();
	    color.setTheme((long)1);
	    ctFont.setColorArray(0,color);

	    XSSFFont xssfFont=new XSSFFont(ctFont);
	    assertEquals(ctFont.getColorArray(0).getTheme(),xssfFont.getThemeColor());

	    xssfFont.setThemeColor(IndexedColors.RED.getIndex());
	    assertEquals(IndexedColors.RED.getIndex(),ctFont.getColorArray(0).getTheme());
	}

	public void testFamily(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTIntProperty family=ctFont.addNewFamily();
		family.setVal(FontFamily.MODERN.getValue());
		ctFont.setFamilyArray(0,family);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(FontFamily.MODERN.getValue(),xssfFont.getFamily());
	}


	public void testScheme(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontScheme scheme=ctFont.addNewScheme();
		scheme.setVal(STFontScheme.MAJOR);
		ctFont.setSchemeArray(0,scheme);

		XSSFFont font=new XSSFFont(ctFont);
		assertEquals(FontScheme.MAJOR,font.getScheme());

		font.setScheme(FontScheme.NONE);
		assertEquals(STFontScheme.NONE,ctFont.getSchemeArray(0).getVal());
	}

	public void testTypeOffset(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTVerticalAlignFontProperty valign=ctFont.addNewVertAlign();
		valign.setVal(STVerticalAlignRun.BASELINE);
		ctFont.setVertAlignArray(0,valign);

		XSSFFont font=new XSSFFont(ctFont);
		assertEquals(Font.SS_NONE,font.getTypeOffset());

		font.setTypeOffset(XSSFFont.SS_SUPER);
		assertEquals(STVerticalAlignRun.SUPERSCRIPT,ctFont.getVertAlignArray(0).getVal());
	}

	/**
	 * Tests that we can define fonts to a new
	 *  file, save, load, and still see them
	 * @throws Exception
	 */
	public void testCreateSave() throws Exception {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet s1 = (XSSFSheet)wb.createSheet();
		Row r1 = s1.createRow(0);
		Cell r1c1 = r1.createCell(0);
		r1c1.setCellValue(2.2);

		assertEquals(1, wb.getNumberOfFonts());

		XSSFFont font=wb.createFont();
		font.setBold(true);
		font.setStrikeout(true);
		font.setColor(IndexedColors.YELLOW.getIndex());
		font.setFontName("Courier");
        wb.createCellStyle().setFont(font);
        assertEquals(2, wb.getNumberOfFonts());

		CellStyle cellStyleTitle=wb.createCellStyle();
		cellStyleTitle.setFont(font);
		r1c1.setCellStyle(cellStyleTitle);

		// Save and re-load
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		wb.write(baos);
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());

		wb = new XSSFWorkbook(Package.open(bais));
		s1 = (XSSFSheet)wb.getSheetAt(0);

		assertEquals(2, wb.getNumberOfFonts());
		assertNotNull(s1.getRow(0).getCell(0).getCellStyle().getFont(wb));
		assertEquals(IndexedColors.YELLOW.getIndex(), s1.getRow(0).getCell(0).getCellStyle().getFont(wb).getColor());
		assertEquals("Courier", s1.getRow(0).getCell(0).getCellStyle().getFont(wb).getFontName());

		// Now add an orphaned one
		XSSFFont font2 = wb.createFont();
		font2.setItalic(true);
		font2.setFontHeightInPoints((short)15);
		wb.createCellStyle().setFont(font2);
        assertEquals(3, wb.getNumberOfFonts());

		// Save and re-load
		baos = new ByteArrayOutputStream();
		wb.write(baos);
		bais = new ByteArrayInputStream(baos.toByteArray());

		wb = new XSSFWorkbook(Package.open(bais));
		s1 = (XSSFSheet)wb.getSheetAt(0);

		assertEquals(3, wb.getNumberOfFonts());
		assertNotNull(wb.getFontAt((short)1));
		assertNotNull(wb.getFontAt((short)2));

		assertEquals(15, wb.getFontAt((short)2).getFontHeightInPoints());
		assertEquals(true, wb.getFontAt((short)2).getItalic());
	}


	public void testXSSFFont() throws IOException{
		XSSFWorkbook workbook=new XSSFWorkbook();
		//Font font1=workbook.createFont();

		Sheet sheet=workbook.createSheet("sheet 1 - test font");


		Row row=sheet.createRow(0);
		Cell cell=row.createCell(0);
		cell.setCellValue(new XSSFRichTextString("XSSFFont test example file"));
		XSSFFont font=new XSSFFont();
		font.setBold(true);
		font.setFontHeightInPoints((short)22);
		font.setColor(IndexedColors.BLUE.getIndex());
		font.setFontName("Verdana");
		CellStyle cellStyleTitle=workbook.createCellStyle();
		cellStyleTitle.setFont(font);
		cell.setCellStyle(cellStyleTitle);


		row=sheet.createRow(3);
		XSSFFont font1=new XSSFFont();
		font1.setBold(true);
		font1.setItalic(true);
		font1.setFontHeightInPoints((short)18);
		font1.setColor(IndexedColors.RED.getIndex());
		font1.setFontName("Arial");
		CellStyle cellStyle1=workbook.createCellStyle();
		cellStyle1.setFont(font1);

		Cell cell1=row.createCell(0);
		cell1.setCellValue(new XSSFRichTextString("red bold 18pt italic Arial"));
		cell1.setCellStyle(cellStyle1);

		
		row=sheet.createRow(4);
		Font font2=new XSSFFont();
		font2.setFontHeight((short)1);
		font2.setFontName("Courier");
		font2.setColor(IndexedColors.BLACK.getIndex());
		font2.setUnderline(Font.U_DOUBLE);
		CellStyle cellStyle2=workbook.createCellStyle();
		cellStyle2.setFont(font2);

		Cell cell2=row.createCell(0);
		cell2.setCellValue(new XSSFRichTextString("Something in courier underlined"));
		cell2.setCellStyle(cellStyle2);


		row=sheet.createRow(5);
		cell1=row.createCell(0);
		Font font3=new XSSFFont();
		font3.setFontHeightInPoints((short)9);
		font3.setFontName("Times");
		font3.setStrikeout(true);
		font3.setColor(IndexedColors.PINK.getIndex());
		CellStyle cellStyle3=workbook.createCellStyle();
		cellStyle3.setFont(font3);

		cell1.setCellValue(new XSSFRichTextString("pink italic Times 9pt strikeout!!!"));
		cell1.setCellStyle(cellStyle3);
		
		File tmpFile = new File("test-ooxml-font.xlsx");
		if(tmpFile.exists()) tmpFile.delete();
		FileOutputStream out = new FileOutputStream(tmpFile);
		workbook.write(out);
		out.close();
	}

}
