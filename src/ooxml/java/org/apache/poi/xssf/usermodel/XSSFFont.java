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
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTFont;

public class XSSFFont implements Font {
	
	private CTFont font;

	public XSSFFont(CTFont font) {
		this.font = font;
	}
	
	public XSSFFont() {
		this.font = CTFont.Factory.newInstance();
	}

	public short getBoldweight() {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte getCharSet() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getColor() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getFontHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	public short getFontHeightInPoints() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getFontName() {
		// TODO Auto-generated method stub
		return null;
	}

	public short getIndex() {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean getItalic() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean getStrikeout() {
		// TODO Auto-generated method stub
		return false;
	}

	public short getTypeOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	public byte getUnderline() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setBoldweight(short boldweight) {
		// TODO Auto-generated method stub
		
	}

	public void setCharSet(byte charset) {
		// TODO Auto-generated method stub
		
	}

	public void setColor(short color) {
		// TODO Auto-generated method stub
		
	}

	public void setFontHeight(short height) {
		// TODO Auto-generated method stub
		
	}

	public void setFontHeightInPoints(short height) {
		// TODO Auto-generated method stub
		
	}

	public void setFontName(String name) {
		// TODO Auto-generated method stub
		
	}

	public void setItalic(boolean italic) {
		// TODO Auto-generated method stub
		
	}

	public void setStrikeout(boolean strikeout) {
		// TODO Auto-generated method stub
		
	}

	public void setTypeOffset(short offset) {
		// TODO Auto-generated method stub
		
	}

	public void setUnderline(byte underline) {
		// TODO Auto-generated method stub
		
	}
	
	public long putFont(LinkedList<CTFont> fonts) {
		if(fonts.contains(font)) {
			return fonts.indexOf(font);
		}
		fonts.add(font);
		return fonts.size() - 1;
	}

}
