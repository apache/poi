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

import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;

public class XSSFCreationHelper implements CreationHelper {
	private XSSFWorkbook workbook;
	XSSFCreationHelper(XSSFWorkbook wb) {
		workbook = wb;
	}
	
    /**
     * Creates a new XSSFRichTextString for you.
     */
	public XSSFRichTextString createRichTextString(String text) {
        XSSFRichTextString rt =new XSSFRichTextString(text);
        rt.setStylesTableReference(workbook.getStylesSource());
        return rt;
	}
	
	public XSSFDataFormat createDataFormat() {
		return workbook.createDataFormat();
	}
	
	public XSSFHyperlink createHyperlink(int type) {
		return new XSSFHyperlink(type);
	}
}
