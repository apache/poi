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
package org.apache.poi.ss.usermodel;

import org.apache.poi.ss.util.CellRangeAddressList;

/**
 * @author <a href="rjankiraman@emptoris.com">Radhakrishnan J</a>
 * 
 */
public interface DataValidationHelper {
	
	DataValidationConstraint createFormulaListConstraint(String listFormula);

	DataValidationConstraint createExplicitListConstraint(String[] listOfValues);

	DataValidationConstraint createNumericConstraint(int validationType,int operatorType, String formula1, String formula2);
	
	DataValidationConstraint createTextLengthConstraint(int operatorType, String formula1, String formula2);
	
	DataValidationConstraint createDecimalConstraint(int operatorType, String formula1, String formula2);
	
	DataValidationConstraint createIntegerConstraint(int operatorType, String formula1, String formula2);
	
	DataValidationConstraint createDateConstraint(int operatorType, String formula1, String formula2,String dateFormat);
	
	DataValidationConstraint createTimeConstraint(int operatorType, String formula1, String formula2);
	
	DataValidationConstraint createCustomConstraint(String formula);
	
	DataValidation createValidation(DataValidationConstraint constraint,CellRangeAddressList cellRangeAddressList);
}
