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
package org.apache.poi.xssf;

import org.apache.poi.UnsupportedFileFormatException;

/**
 * We don't support .xlsb for read and write via {@link org.apache.poi.xssf.usermodel.XSSFWorkbook}.
 * As of POI 3.16-beta3, we do support streaming reading of xlsb files
 * via {@link org.apache.poi.xssf.eventusermodel.XSSFBReader}
 */
public class XLSBUnsupportedException extends UnsupportedFileFormatException {
    private static final long serialVersionUID = 7849681804154571175L;
    public static final String MESSAGE = ".XLSB Binary Workbooks are not supported"; 

    public XLSBUnsupportedException() {
		super(MESSAGE);
	}
}