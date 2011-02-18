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

/**
 * A simple class that encapsulates information about a cell evaluation
 * from POI.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public class ExcelAntEvaluationResult {
	
	/**
	 * This boolean flag is used to determine if the evaluation completed
	 * without error.  This alone doesn't ensure that the evaluation was 
	 * sucessful.
	 */
	private boolean evaluationCompletedWithError ;
	
	/**
	 * This boolean flag is used to determine if the result was within
	 * the specified precision.
	 */
	private boolean didPass ;
	
	/**
	 * This is the actual value returned from the evaluation.
	 */
	private double returnValue ;
	
	/**
	 * Any error message String values that need to be returned.
	 */
	private String errorMessage ;
	
	/**
	 * Stores the absolute value of the delta for this evaluation.
	 */
	private double actualDelta ;
	
	/**
	 * This stores the fully qualified cell name (sheetName!cellId).
	 */
	private String cellName ;
	
	

	public ExcelAntEvaluationResult( boolean completedWithError,
			                 boolean passed, 
			                 double retValue, 
			                 String errMessage, 
			                 double delta,
			                 String cellId ) {

		evaluationCompletedWithError = completedWithError;
		didPass = passed;
		returnValue = retValue;
		errorMessage = errMessage;
		actualDelta = delta ;
		cellName = cellId ;
	}

	public double getReturnValue() {
		return returnValue;
	}

	public String getErrorMessage() {
		return errorMessage;
	}
	
	public boolean didTestPass() {
		return didPass ;
	}
	
	public boolean evaluationCompleteWithError() {
		return evaluationCompletedWithError ;
	}
	
	public double getDelta() {
		return actualDelta ;
	}
	
	public String getCellName() {
		return cellName ;
	}

	@Override
	public String toString() {
		return "ExcelAntEvaluationResult [evaluationCompletedWithError="
				+ evaluationCompletedWithError + ", didPass=" + didPass
				+ ", returnValue=" + returnValue + ", errorMessage="
				+ errorMessage + ", actualDelta=" + actualDelta + ", cellName="
				+ cellName + "]";
	}

	
}
