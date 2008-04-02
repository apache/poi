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
package org.apache.poi.xssf.usermodel.helpers;

import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRPrElt;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTRst;

public class RichTextStringHelper {

	public static void convertToRst(RichTextString string, CTRst text) {
		// TODO: implement RichTextString to Rst conversion
		text.setT(string.getString());
	}

	public static RichTextString convertFromRst(CTRst ctText) {
		if(ctText == null) {
			return new XSSFRichTextString("");
		}
		if(ctText.getT() != null) {
			return new XSSFRichTextString(ctText.getT());
		}
		
		// Grab all the text
		StringBuffer t = new StringBuffer();
		for(CTRElt r : ctText.getRArray()) {
			t.append( r.getT() );
		}
		XSSFRichTextString rtxt = new XSSFRichTextString(t.toString());
		
		// Now get all the formatting
		// TODO: implement Rst/RpR to RichTextString conversion
		for(CTRElt r : ctText.getRArray()) {
			// Formatting info comes from rPr
			CTRPrElt rPr = r.getRPr();
			rPr.getRFontArray();
		}
		return rtxt;
	}
}
