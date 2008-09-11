package org.apache.poi.xssf.util;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBooleanProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontName;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontScheme;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFontSize;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTIntProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTUnderlineProperty;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTVerticalAlignFontProperty;

/*
 * The font element in xml is definited like <choice maxOccurs="unbounded">.
 * So in the java object CTFont all methods get and set returns an array of elements also if there is always defined
 * only one type of attribute per type.
 * This class is made to make simple using method get and set instead of getArray() or set(index,object).
 * We consider always the index 0 like the only one index to refer of CT_Font attribute.
 * 
 */


public class CTFontWrapper{

	private CTFont font;
		
	
	public CTFontWrapper(CTFont font){
		this.font=font;		
	}
	
	public CTFont getCTFont(){
		return font;
	}
	
	
	public CTBooleanProperty getB(){
		if( font.getBArray().length>0) 
			return font.getBArray(0);
		else 
			return null;
	}
	
	
	public CTIntProperty getCharset(){
		if(font.getCharsetArray().length>0)
			return font.getCharsetArray(0);
		else
			return null;
	}
	
	public CTColor getColor(){
		if(font.getColorArray().length>0)
			return font.getColorArray(0);
		else
			return null;
	}
	
	public CTBooleanProperty getStrike(){
		if(font.getStrikeArray().length>0)
			return font.getStrikeArray(0);
		else
			return null;
	}
	
	public CTVerticalAlignFontProperty getVertAlign() {
		if(font.getVertAlignArray().length>0)
			return font.getVertAlignArray(0);
		else
			return null;
	}
	
	public CTFontName setName(){
		if(font.getNameArray().length>0)
			return font.getNameArray(0);
		else
			return null;
	}
	
	public CTFontSize getSz(){
		if(font.getSzArray().length>0)
			return font.getSzArray(0);
		else
			return null;
	}
	
	public CTBooleanProperty getI(){
		if(font.getIArray().length>0)
			return font.getIArray(0);
		else
			return null;
	}
	
	
	
	public CTUnderlineProperty getU(){
		if(font.getUArray().length>0)
			return font.getUArray(0);
		else
			return null;
	}
	
	public void setB(CTBooleanProperty value){
		font.setBArray(0,value);
	}
	
	public void setCharset(CTIntProperty value){
		font.setCharsetArray(0, value);
	}
	
	public void setColor(CTColor value){
		font.setColorArray(0,value);
		}

	public void setFontName(CTFontName value){
		font.setNameArray(0,value);
	}
	
	public void setSz(CTFontSize value){
		font.setSzArray(0,value);
	}
	public void setI(CTBooleanProperty value){
		font.setIArray(0,value);
	}
	
	public void setU(CTUnderlineProperty value){
		font.setUArray(0,value);	
	}
	
	
	public void setStrike(CTBooleanProperty value){
		font.setStrikeArray(0,value);
	}
	
	
	public void setVertAlign(CTVerticalAlignFontProperty value){
		font.setVertAlignArray(0,value);
	}

	
	public void setName(CTFontName fontName) {
		font.setNameArray(0,fontName);
	}

	public CTFontName getName() {
		return font.getNameArray(0);
	}

	public CTIntProperty getFamily() {
		return font.getFamilyArray(0);		
	}

	public void setFamily(CTIntProperty family) {
		 font.setFamilyArray(0,family);		
	}

	
	public void setFontScheme(CTFontScheme ctFontScheme) {
		font.setSchemeArray(0,ctFontScheme);
	}

	public CTFontScheme getFontScheme() {
		return font.getSchemeArray(0);
	}
	
	// methods used in FontFormatting
	
	public CTBooleanProperty getOutline(){
		return font.getOutlineArray(0);
	}
	
	
	
}

	