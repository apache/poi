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

import java.util.LinkedList;

import org.apache.poi.ss.usermodel.Font;

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

public class XSSFFont implements Font {


	public static final int SCHEME_MAJOR=2;
	public static final int SCHEME_MINOR=3;
	public static final int SCHEME_NONE=0;

	public static final int FONT_FAMILY_NOT_APPLICABLE=0;
	public static final int FONT_FAMILY_ROMAN=1;
	public static final int FONT_FAMILY_SWISS=2;
	public static final int FONT_FAMILY_MODERN=3;
	public static final int FONT_FAMILY_SCRIPT=4;
	public static final int FONT_FAMILY_DECORATIVE=5;


	public static final String DEFAULT_FONT_NAME="Calibri";
	public static final short DEFAULT_FONT_SIZE=11;
	public static final short DEFAULT_FONT_COLOR=(short)IndexedColors.BLACK;

	private int index=0;


	private CTFontWrapper fontWrapper;



	public XSSFFont(CTFont font) {
		this.fontWrapper=new CTFontWrapper(font);
	}

	/*	
	public XSSFFont(int index) {
		this.fontWrapper=new CTFontWrapper(font);
		this.index=index;
	}
	 */

	public XSSFFont() {
		this.fontWrapper = new CTFontWrapper(CTFont.Factory.newInstance());
	}


	public CTFont getCTFont(){
		return fontWrapper.getCTFont();
	}


	public short getBoldweight() {
		CTBooleanProperty bold=fontWrapper.getB();
		if(bold!=null && bold.getVal())
			return Font.BOLDWEIGHT_BOLD;
		else
			return Font.BOLDWEIGHT_NORMAL;
	} 



	public byte getCharSet() {
		CTIntProperty charset= fontWrapper.getCharset();
		if(charset!=null){
			//this value must be set -- can't be null
			switch (charset.getVal()) {
			case Charset.ANSI_CHARSET:
				return Font.ANSI_CHARSET;

			case Charset.DEFAULT_CHARSET:
				return Font.DEFAULT_CHARSET;

			case Charset.SYMBOL_CHARSET:
				return Font.SYMBOL_CHARSET;

			default://maight be correct to return this byte value???
				return Byte.parseByte(Integer.toString(charset.getVal()));
			}
		}
		else
			return Font.ANSI_CHARSET;
	}

	public short getColor() {
		CTColor color=fontWrapper.getColor();
		long index=color.getIndexed();
		if (index==XSSFFont.DEFAULT_FONT_COLOR){
			return Font.COLOR_NORMAL;
		}
		else if(index==IndexedColors.RED){
			return Font.COLOR_RED;
		}
		else{
			return Short.parseShort(new Long(index).toString());
		}
	}

	public short getFontHeight() {
		if(fontWrapper.getSz()!=null){
			double fontHeight= fontWrapper.getSz().getVal()/20;
			return (short)fontHeight;
		}
		else
			return DEFAULT_FONT_SIZE/20;
	}

	public short getFontHeightInPoints() {
		if(fontWrapper.getSz()!=null){
			double fontHeight= fontWrapper.getSz().getVal();// /72;
			return (short)fontHeight;//new Double(fontHeight).shortValue();
		}
		else
			return DEFAULT_FONT_SIZE;
	}

	//AGGIUNGERE CONTROLLO NULL
	public String getFontName() {	
		if(fontWrapper.getName()!=null)
			return fontWrapper.getName().getVal();
		else
			return DEFAULT_FONT_NAME;
	}


	public short getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getItalic() {
		if(fontWrapper.getI()!=null)
			return fontWrapper.getI().getVal();
		else
			return false;
	}

	public boolean getStrikeout() {
		if(fontWrapper.getStrike()!=null)
			return fontWrapper.getStrike().getVal();
		else
			return false;
	}

	public short getTypeOffset() {
		if(fontWrapper.getVertAlign()!=null){
			int val=fontWrapper.getVertAlign().getVal().intValue();
			switch (val) {
			case STVerticalAlignRun.INT_BASELINE:
				return Font.SS_NONE;
			case STVerticalAlignRun.INT_SUBSCRIPT:
				return Font.SS_SUB;
			case STVerticalAlignRun.INT_SUPERSCRIPT:
				return Font.SS_SUPER;
			default: throw new RuntimeException("Wrong offset value "+val);
			}
		}
		else
			return Font.SS_NONE;
	}

	public byte getUnderline() {		
		if(fontWrapper.getU()!=null){
			//attenzione : -- get val pu˜ tornare null----
			switch (fontWrapper.getU().getVal().intValue()) {
			case STUnderlineValues.INT_DOUBLE:
				return Font.U_DOUBLE;	
			case STUnderlineValues.INT_DOUBLE_ACCOUNTING:
				return Font.U_DOUBLE_ACCOUNTING;	

			case STUnderlineValues.INT_SINGLE_ACCOUNTING:
				return Font.U_SINGLE_ACCOUNTING;	

			case STUnderlineValues.INT_NONE:
				return Font.U_NONE;

			case STUnderlineValues.INT_SINGLE:	
			default:
				return Font.U_SINGLE;
			}
		}
		return Font.U_NONE;
	}

	public void setBoldweight(short boldweight) {		
		if(boldweight==Font.BOLDWEIGHT_BOLD){

			CTBooleanProperty bold;
			if(fontWrapper.getCTFont().getBArray().length==0){
				bold=fontWrapper.getCTFont().addNewB();
			}
			else{
				bold=CTBooleanProperty.Factory.newInstance();
			}
			bold.setVal(true);
			fontWrapper.setB(bold);
		}
	}

	public void setCharSet(byte charset) {
		CTIntProperty charsetProperty;
		if(fontWrapper.getCTFont().getCharsetArray().length==0){
			charsetProperty=fontWrapper.getCTFont().addNewCharset();
		}
		else{
			charsetProperty=CTIntProperty.Factory.newInstance();
		}
		switch (charset) {
		case Font.ANSI_CHARSET:
			charsetProperty.setVal(Charset.ANSI_CHARSET);	
			break;
		case Font.SYMBOL_CHARSET:
			charsetProperty.setVal(Charset.SYMBOL_CHARSET);	
			break;
		case Font.DEFAULT_CHARSET:
			charsetProperty.setVal(Charset.DEFAULT_CHARSET);	
			break;
		default:
			throw new RuntimeException("Attention: an attempt to set a type of unknow charset and charset");
		}

		fontWrapper.setCharset(charsetProperty);
	}


	public void setColor(short color) {
		CTColor ctColor;
		if(fontWrapper.getCTFont().getColorArray().length==0){
			ctColor=fontWrapper.getCTFont().addNewColor();
		}
		else{
			ctColor=CTColor.Factory.newInstance();
		}
		
		switch (color) {
		case Font.COLOR_NORMAL:{
			ctColor.setIndexed(XSSFFont.DEFAULT_FONT_COLOR);
			break;
		}
		case Font.COLOR_RED:{
			ctColor.setIndexed(IndexedColors.RED);
			break;
		}
		default:
			ctColor.setIndexed(color);
		}

		fontWrapper.setColor(ctColor);
	}



	public void setFontHeight(short height) {
		CTFontSize fontSize;
		if(fontWrapper.getCTFont().getSzArray().length==0){
			fontSize=fontWrapper.getCTFont().addNewSz();
		}
		else{
			fontSize=CTFontSize.Factory.newInstance();
		}
		fontSize.setVal(height*20);
		fontWrapper.setSz(fontSize);
	}


	public void setFontHeightInPoints(short height) {
		CTFontSize fontSize;
		if(fontWrapper.getCTFont().getSzArray().length==0){
			fontSize=fontWrapper.getCTFont().addNewSz();
		}
		else{
			fontSize=CTFontSize.Factory.newInstance();
		}

		fontSize.setVal(height);
		fontWrapper.setSz(fontSize);
	}



	public void setFontName(String name) {
		CTFontName fontName;
		if(fontWrapper.getCTFont().getNameArray().length==0){
			fontName=fontWrapper.getCTFont().addNewName();
		}
		else{
			fontName=CTFontName.Factory.newInstance();
		}

		fontName.setVal(name);
		fontWrapper.setName(fontName);	
	}
	

	public void setItalic(boolean italic) {
		CTBooleanProperty bool;
		if(fontWrapper.getCTFont().getIArray().length==0){
			bool=fontWrapper.getCTFont().addNewI();			
		}
		else{
			bool=CTBooleanProperty.Factory.newInstance();
		}

		bool.setVal(italic);
		fontWrapper.setI(bool);
	}

	public void setStrikeout(boolean strikeout) {
		CTBooleanProperty strike;
		if(fontWrapper.getCTFont().getStrikeArray().length==0){
			strike=fontWrapper.getCTFont().addNewStrike();
		}
		else{
			strike=CTBooleanProperty.Factory.newInstance();
		}
		strike.setVal(strikeout);
		fontWrapper.setStrike(strike);
	}

	public void setTypeOffset(short offset) {
		CTVerticalAlignFontProperty offsetProperty;
		if(fontWrapper.getCTFont().getVertAlignArray().length==0){
			offsetProperty=fontWrapper.getCTFont().addNewVertAlign();			
		}
		else{
			offsetProperty=CTVerticalAlignFontProperty.Factory.newInstance();
		}
		switch (offset) {
		case Font.SS_NONE:
			offsetProperty.setVal(STVerticalAlignRun.BASELINE);
			break;
		case Font.SS_SUB:
			offsetProperty.setVal(STVerticalAlignRun.SUBSCRIPT);
			break;
		case Font.SS_SUPER:
			offsetProperty.setVal(STVerticalAlignRun.SUPERSCRIPT);
			break;
		}
		fontWrapper.setVertAlign(offsetProperty);
	}

	public void setUnderline(byte underline) {
		CTUnderlineProperty ctUnderline;
		if(fontWrapper.getCTFont().getUArray().length==0){
			ctUnderline=fontWrapper.getCTFont().addNewU();
		}
		else{
			ctUnderline=CTUnderlineProperty.Factory.newInstance();
		}
		switch (underline) {
		case Font.U_DOUBLE:
			ctUnderline.setVal(STUnderlineValues.DOUBLE);	
			break;
		case Font.U_DOUBLE_ACCOUNTING:
			ctUnderline.setVal(STUnderlineValues.DOUBLE_ACCOUNTING);	
			break;
		case Font.U_SINGLE_ACCOUNTING:
			ctUnderline.setVal(STUnderlineValues.SINGLE_ACCOUNTING);	
			break;
		case Font.U_NONE:
			ctUnderline.setVal(STUnderlineValues.NONE);	
			break;

		case Font.U_SINGLE:
		default:
			ctUnderline.setVal(STUnderlineValues.SINGLE);
		break;
		}

		fontWrapper.setU(ctUnderline);
	}


	public long putFont(LinkedList<CTFont> fonts) {
		//TODO
		/*
		 * we need to implement a method equals to check that 2 instances of CTFont
		 * are different by comparison of all font attributes.
		 * NB: take a look to findFont method in XSSFWorkbook
		 */
		CTFont font=fontWrapper.getCTFont();
		if(fonts.contains(font)) {
			return fonts.indexOf(font);
		}
		fonts.add(font);
		return fonts.size() - 1;
	}

	// solo di xssfFont - non di Font-
	//sono utilizzati nel metodo createDefaultFont in StylesTable insta.

	public int getScheme(){
		int fontScheme = fontWrapper.getFontScheme().getVal().intValue();
		switch (fontScheme) {
		case STFontScheme.INT_MAJOR:
			return XSSFFont.SCHEME_MAJOR; 
		case STFontScheme.INT_MINOR:
			return XSSFFont.SCHEME_MINOR;
		case STFontScheme.INT_NONE:
			return XSSFFont.SCHEME_NONE;

		default:
			return fontScheme;
		} 
	}


	public void setScheme(int scheme){
		CTFontScheme ctFontScheme;
		if(fontWrapper.getCTFont().getSchemeArray().length==0){
			ctFontScheme=fontWrapper.getCTFont().addNewScheme();
		}
		else{
			ctFontScheme=CTFontScheme.Factory.newInstance();
		}
		switch (scheme) {
		case XSSFFont.SCHEME_MAJOR:
			ctFontScheme.setVal(STFontScheme.MAJOR);
			break;
		case XSSFFont.SCHEME_MINOR:
			ctFontScheme.setVal(STFontScheme.MINOR);
			break;
		case XSSFFont.SCHEME_NONE:
			ctFontScheme.setVal(STFontScheme.NONE);
			break;
		default:
			throw new RuntimeException("Schema value ["+ scheme +"] not supported in XSSFFont");
		}

		fontWrapper.setFontScheme(ctFontScheme);
	}	



	public int getFamily(){
		if(fontWrapper.getFamily()!=null)
			return fontWrapper.getFamily().getVal();
		else
			return XSSFFont.FONT_FAMILY_SWISS;
	}

	public void setFamily(int value){
		//TODO
		CTIntProperty family;
		if(fontWrapper.getCTFont().getSchemeArray().length==0){
			family=fontWrapper.getCTFont().addNewFamily();
		}
		else{
			family=CTIntProperty.Factory.newInstance();
		}
		family.setVal(value);
		//fontWrapper.setFamily
	}	



}
