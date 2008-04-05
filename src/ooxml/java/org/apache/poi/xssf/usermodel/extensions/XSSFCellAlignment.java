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

import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTCellAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STHorizontalAlignment;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STVerticalAlignment;


public class XSSFCellAlignment {
	
	private CTCellAlignment cellAlignement;
	
	public XSSFCellAlignment(CTCellAlignment cellAlignment) {
		this.cellAlignement = cellAlignment;
	}
	
	public STVerticalAlignment.Enum getVertical() {
		if (cellAlignement.getVertical() == null) {
			cellAlignement.setVertical(STVerticalAlignment.TOP);
		}
		return cellAlignement.getVertical();
	}
	
	public void setVertical(STVerticalAlignment.Enum vertical) {
		cellAlignement.setVertical(vertical);
	}
	
	public STHorizontalAlignment.Enum getHorizontal() {
		if (cellAlignement.getHorizontal() == null) {
			cellAlignement.setHorizontal(STHorizontalAlignment.GENERAL);
		}
		return cellAlignement.getHorizontal();
	}
	
	public void setHorizontal(STHorizontalAlignment.Enum horizontal) {
		cellAlignement.setHorizontal(horizontal);
	}
	
	public long getIndent() {
		return cellAlignement.getIndent();
	}
	
	public void setIndent(long indent) {
		cellAlignement.setIndent(indent);
	}
	
	public long getTextRotation() {
		return cellAlignement.getTextRotation();
	}
	
	public void setTextRotation(long rotation) {
		cellAlignement.setTextRotation(rotation);
	}
	
	public boolean getWrapText() {
		return cellAlignement.getWrapText();
	}
	
	public void setWrapText(boolean wrapped) {
		cellAlignement.setWrapText(wrapped);
	}
}
