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

package org.apache.poi.hssf.usermodel;

import org.apache.poi.hssf.record.HeaderRecord;
import org.apache.poi.hssf.record.aggregates.PageSettingsBlock;
import org.apache.poi.ss.usermodel.Header;

/**
 * Class to read and manipulate the header.
 * <P>
 * The header works by having a left, center, and right side.  The total cannot
 * be more that 255 bytes long.  One uses this class by getting the HSSFHeader
 * from HSSFSheet and then getting or setting the left, center, and right side.
 * For special things (such as page numbers and date), one can use a the methods
 * that return the characters used to represent these.  One can also change the
 * fonts by using similar methods.
 */
public final class HSSFHeader extends HeaderFooter implements Header {

	private final PageSettingsBlock _psb;

	protected HSSFHeader(PageSettingsBlock psb) {
		_psb = psb;
	}

	protected String getRawText() {
		HeaderRecord hf = _psb.getHeader();
		if (hf == null) {
			return "";
		}
		return hf.getText();
	}

	@Override
	protected void setHeaderFooterText(String text) {
		HeaderRecord hfr = _psb.getHeader();
		if (hfr == null) {
			hfr = new HeaderRecord(text);
			_psb.setHeader(hfr);
		} else {
			hfr.setText(text);
		}
	}
}
