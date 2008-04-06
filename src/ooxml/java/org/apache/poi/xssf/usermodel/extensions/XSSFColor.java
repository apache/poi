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
package org.apache.poi.xssf.usermodel.extensions;

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTBorderPr;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

public class XSSFColor {
	
	private CTColor color;
	
	public XSSFColor(CTColor color) {
		this.color = color;
		if (this.color == null) {
			this.color = CTColor.Factory.newInstance();
		}
	}
	
	public boolean isAuto() {
		return color.getAuto();
	}
	
	public void setAuto(boolean auto) {
		color.setAuto(auto);
	}
	
	public long getIndexed() {
		return color.getIndexed();
	}
	
	public void setIndexed(long indexed) {
		color.setIndexed(indexed);
	}
	
	public byte[] getRgb() {
		return color.getRgb();
	}
	
	public void setRgb(byte[] rgb) {
		color.setRgb(rgb);
	}
	
	public long getTheme() {
		return color.getTheme();
	}
	
	public void setTheme(long theme) {
		color.setTheme(theme);
	}
	
	public double getTint() {
		return color.getTint();
	}
	
	public void setTint(double tint) {
		color.setTint(tint);
	}

	public void setToBorder(CTBorderPr border) {
		border.setColor(this.color);
	}
}
