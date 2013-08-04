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
package org.apache.poi.ss.excelant.util;

import org.apache.poi.ss.formula.udf.UDFFinder;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A helper class to allow testing of protected methods and constructors.
 * 
 * @author jsvede
 *
 */
public class ExcelAntWorkbookUtilTestHelper extends ExcelAntWorkbookUtil {

	public ExcelAntWorkbookUtilTestHelper(String fName) {
		super(fName);
	}

	public ExcelAntWorkbookUtilTestHelper(Workbook wb) {
		super(wb);
	}

	@Override
	public UDFFinder getFunctions() {
		return super.getFunctions();
	}

	@Override
	public FormulaEvaluator getEvaluator(String excelFileName) {
		return super.getEvaluator(excelFileName);
	}

	
}
