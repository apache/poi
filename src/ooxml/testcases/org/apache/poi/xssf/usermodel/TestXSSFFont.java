package org.apache.poi.xssf.usermodel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.util.CTFontWrapper;
import org.apache.poi.xssf.util.Charset;
import org.apache.poi.xssf.util.IndexedColors;
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
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);

		CTBooleanProperty bool=wrapper.getCTFont().addNewB();
		bool.setVal(false);
		wrapper.setB(bool);	

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.BOLDWEIGHT_NORMAL, xssfFont.getBoldweight());


		xssfFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
		assertEquals(ctFont.getBArray().length,1);
		assertEquals(true, ctFont.getBArray(0).getVal());
		assertEquals(true,wrapper.getB().getVal());

	}

	public void testCharSet(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTIntProperty prop=ctFont.addNewCharset();
		prop.setVal(Charset.ANSI_CHARSET);
		
		wrapper.setCharset(prop);
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.ANSI_CHARSET,xssfFont.getCharSet());
		
		xssfFont.setCharSet(Font.DEFAULT_CHARSET);
		assertEquals(Charset.DEFAULT_CHARSET, wrapper.getCharset().getVal());
	}


	public void testFontName(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTFontName fname=ctFont.addNewName();
		fname.setVal("Arial");
		wrapper.setFontName(fname);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals("Arial", xssfFont.getFontName());

		xssfFont.setFontName("Courier");
		assertEquals("Courier",wrapper.getName().getVal());
	}


	public void testItalic(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);

		CTBooleanProperty bool=wrapper.getCTFont().addNewI();
		bool.setVal(false);
		wrapper.setI(bool);	

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(false, xssfFont.getItalic());

		xssfFont.setItalic(true);
		assertEquals(ctFont.getIArray().length,1);
		assertEquals(true, ctFont.getIArray(0).getVal());
		assertEquals(true,wrapper.getI().getVal());
	}


	public void testStrikeout(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);

		CTBooleanProperty bool=wrapper.getCTFont().addNewStrike();
		bool.setVal(false);
		wrapper.setStrike(bool);	

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(false, xssfFont.getStrikeout());

		xssfFont.setStrikeout(true);
		assertEquals(ctFont.getStrikeArray().length,1);
		assertEquals(true, ctFont.getStrikeArray(0).getVal());
		assertEquals(true,wrapper.getStrike().getVal());
	}


	public void testFontHeight(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTFontSize size=ctFont.addNewSz();
		size.setVal(11);
		wrapper.setSz(size);
		
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(11/20,xssfFont.getFontHeight());
		
		xssfFont.setFontHeight((short)20);
		assertEquals(new Double(20*20).doubleValue(),wrapper.getSz().getVal());	}


	public void testFontHeightInPoint(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTFontSize size=ctFont.addNewSz();
		size.setVal(14);
		wrapper.setSz(size);
		
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(14,xssfFont.getFontHeightInPoints());
		
		xssfFont.setFontHeightInPoints((short)20);
		assertEquals(new Double(20).doubleValue(),wrapper.getSz().getVal());
	}


	public void testUnderline(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);

		CTUnderlineProperty underlinePropr=wrapper.getCTFont().addNewU();
		underlinePropr.setVal(STUnderlineValues.SINGLE);
		wrapper.setU(underlinePropr);	

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.U_SINGLE, xssfFont.getUnderline());

		xssfFont.setUnderline(Font.U_DOUBLE);
		assertEquals(ctFont.getUArray().length,1);
		assertEquals(STUnderlineValues.DOUBLE,wrapper.getU().getVal());
		}			

	public void testColor(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTColor color=ctFont.addNewColor();
		//color.setIndexed(IndexedColors.DEFAULT_COLOR);
		color.setIndexed(XSSFFont.DEFAULT_FONT_COLOR);
		wrapper.setColor(color);
		
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.COLOR_NORMAL,xssfFont.getColor());
		
		xssfFont.setColor(Font.COLOR_RED);
		assertEquals(IndexedColors.RED,wrapper.getColor().getIndexed());
	}


	public void testFamily(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTIntProperty family=ctFont.addNewFamily();
		family.setVal(XSSFFont.FONT_FAMILY_MODERN);
		wrapper.setFamily(family);
		
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(XSSFFont.FONT_FAMILY_MODERN,xssfFont.getFamily());

	}

	
	public void testScheme(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTFontScheme scheme=ctFont.addNewScheme();
		scheme.setVal(STFontScheme.MAJOR);
		wrapper.setFontScheme(scheme);
		
		XSSFFont font=new XSSFFont(ctFont);
		assertEquals(XSSFFont.SCHEME_MAJOR,font.getScheme());
		
		font.setScheme(XSSFFont.SCHEME_NONE);
		assertEquals(STFontScheme.NONE,wrapper.getFontScheme().getVal());		
	}

	public void testTypeOffset(){
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontWrapper wrapper=new CTFontWrapper(ctFont);
		CTVerticalAlignFontProperty valign=ctFont.addNewVertAlign();
		valign.setVal(STVerticalAlignRun.BASELINE);
		wrapper.setVertAlign(valign);
		
		XSSFFont font=new XSSFFont(ctFont);
		assertEquals(Font.SS_NONE,font.getTypeOffset());
		
		font.setTypeOffset(XSSFFont.SS_SUPER);
		assertEquals(STVerticalAlignRun.SUPERSCRIPT,wrapper.getVertAlign().getVal());	
	}
	

	public void testXSSFFont() throws IOException{
		XSSFWorkbook workbook=new XSSFWorkbook();
		//Font font1=workbook.createFont();
		
		Sheet sheet=workbook.createSheet("sheet 1 - test font");
		
		
		Row row=sheet.createRow(0);
		Cell cell=row.createCell(0);
		cell.setCellValue(new XSSFRichTextString("XSSFFont test example file"));
		Font font=new XSSFFont();
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font.setFontHeightInPoints((short)22);
		font.setColor((short)IndexedColors.BLUE);
		font.setFontName("Verdana");
		CellStyle cellStyleTitle=workbook.createCellStyle();
		cellStyleTitle.setFont(font);
		cell.setCellStyle(cellStyleTitle);

		
		row=sheet.createRow(3);
		Font font1=new XSSFFont();
		font1.setBoldweight(Font.BOLDWEIGHT_BOLD);
		font1.setItalic(true);
		font1.setFontHeightInPoints((short)18);
		font1.setColor(Font.COLOR_RED);
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
		font2.setColor(Font.COLOR_NORMAL);
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
		font3.setColor((short)IndexedColors.PINK);
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
