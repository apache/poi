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

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.XSSFITestDataProvider;
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

public final class TestXSSFFont extends BaseTestFont{

	@Override
	protected XSSFITestDataProvider getTestDataProvider() {
		return XSSFITestDataProvider.getInstance();
	}

	public void testDefaultFont() {
		baseTestDefaultFont("Calibri", (short) 220, IndexedColors.BLACK.getIndex());
	}

	public void testConstructor() {
		XSSFFont xssfFont=new XSSFFont();
		assertNotNull(xssfFont.getCTFont());
	}

	public void testBoldweight() {
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

	public void testCharSet() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTIntProperty prop=ctFont.addNewCharset();
		prop.setVal(FontCharset.ANSI.getValue());

		ctFont.setCharsetArray(0,prop);
		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(Font.ANSI_CHARSET,xssfFont.getCharSet());

		xssfFont.setCharSet(FontCharset.DEFAULT);
		assertEquals(FontCharset.DEFAULT.getValue(),ctFont.getCharsetArray(0).getVal());
	}

	public void testFontName() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontName fname=ctFont.addNewName();
		fname.setVal("Arial");
		ctFont.setNameArray(0,fname);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals("Arial", xssfFont.getFontName());

		xssfFont.setFontName("Courier");
		assertEquals("Courier",ctFont.getNameArray(0).getVal());
	}

	public void testItalic() {
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

	public void testStrikeout() {
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

	public void testFontHeight() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontSize size=ctFont.addNewSz();
		size.setVal(11);
		ctFont.setSzArray(0,size);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(11,xssfFont.getFontHeightInPoints());

		xssfFont.setFontHeight(20);
		assertEquals(20.0, ctFont.getSzArray(0).getVal(), 0.0);
	}

	public void testFontHeightInPoint() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontSize size=ctFont.addNewSz();
		size.setVal(14);
		ctFont.setSzArray(0,size);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(14,xssfFont.getFontHeightInPoints());

		xssfFont.setFontHeightInPoints((short)20);
		assertEquals(20.0, ctFont.getSzArray(0).getVal(), 0.0);
	}

	public void testUnderline() {
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

	public void testColor() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTColor color=ctFont.addNewColor();
		color.setIndexed(XSSFFont.DEFAULT_FONT_COLOR);
		ctFont.setColorArray(0,color);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(IndexedColors.BLACK.getIndex(),xssfFont.getColor());

		xssfFont.setColor(IndexedColors.RED.getIndex());
		assertEquals(IndexedColors.RED.getIndex(), ctFont.getColorArray(0).getIndexed());
	}

	public void testRgbColor() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTColor color=ctFont.addNewColor();

		color.setRgb(Integer.toHexString(0xFFFFFF).getBytes());
		ctFont.setColorArray(0,color);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(ctFont.getColorArray(0).getRgb()[0],xssfFont.getXSSFColor().getRgb()[0]);
		assertEquals(ctFont.getColorArray(0).getRgb()[1],xssfFont.getXSSFColor().getRgb()[1]);
		assertEquals(ctFont.getColorArray(0).getRgb()[2],xssfFont.getXSSFColor().getRgb()[2]);
		assertEquals(ctFont.getColorArray(0).getRgb()[3],xssfFont.getXSSFColor().getRgb()[3]);

		color.setRgb(Integer.toHexString(0xF1F1F1).getBytes());
		XSSFColor newColor=new XSSFColor(color);
		xssfFont.setColor(newColor);
		assertEquals(ctFont.getColorArray(0).getRgb()[2],newColor.getRgb()[2]);
	}

	public void testThemeColor() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTColor color=ctFont.addNewColor();
		color.setTheme(1);
		ctFont.setColorArray(0,color);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(ctFont.getColorArray(0).getTheme(),xssfFont.getThemeColor());

		xssfFont.setThemeColor(IndexedColors.RED.getIndex());
		assertEquals(IndexedColors.RED.getIndex(),ctFont.getColorArray(0).getTheme());
	}

	public void testFamily() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTIntProperty family=ctFont.addNewFamily();
		family.setVal(FontFamily.MODERN.getValue());
		ctFont.setFamilyArray(0,family);

		XSSFFont xssfFont=new XSSFFont(ctFont);
		assertEquals(FontFamily.MODERN.getValue(),xssfFont.getFamily());
	}

	public void testScheme() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTFontScheme scheme=ctFont.addNewScheme();
		scheme.setVal(STFontScheme.MAJOR);
		ctFont.setSchemeArray(0,scheme);

		XSSFFont font=new XSSFFont(ctFont);
		assertEquals(FontScheme.MAJOR,font.getScheme());

		font.setScheme(FontScheme.NONE);
		assertEquals(STFontScheme.NONE,ctFont.getSchemeArray(0).getVal());
	}

	public void testTypeOffset() {
		CTFont ctFont=CTFont.Factory.newInstance();
		CTVerticalAlignFontProperty valign=ctFont.addNewVertAlign();
		valign.setVal(STVerticalAlignRun.BASELINE);
		ctFont.setVertAlignArray(0,valign);

		XSSFFont font=new XSSFFont(ctFont);
		assertEquals(Font.SS_NONE,font.getTypeOffset());

		font.setTypeOffset(XSSFFont.SS_SUPER);
		assertEquals(STVerticalAlignRun.SUPERSCRIPT,ctFont.getVertAlignArray(0).getVal());
	}
}
