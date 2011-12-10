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

package org.apache.poi.ss.excelant;

import org.apache.poi.ss.excelant.util.ExcelAntEvaluationResult;
import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Instances of this class are used to evaluate a single cell.  This is usually
 * after some values have been set.  The evaluation is actually performed
 * by a WorkbookUtil instance.  The evaluate() method of the WorkbookUtil
 * class returns an EvaluationResult which encapsulates the results and 
 * information from the evaluation.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )

 *
 */
public class ExcelAntEvaluateCell extends Task {

	private String cell ;
	private double expectedValue ;
	private double precision ;
	private double precisionToUse ;
	private double globalPrecision ;
	private boolean requiredToPass = false ;
	
	
	private ExcelAntEvaluationResult result  ;
	
	private ExcelAntWorkbookUtil wbUtil ;
	
	private boolean showDelta = false ;
	
	
	public ExcelAntEvaluateCell() {}

	protected void setWorkbookUtil( ExcelAntWorkbookUtil wb ) {
		wbUtil = wb ;
	}
	
	public void setShowDelta( boolean value ) {
		showDelta = value ;
	}
	
	protected boolean showDelta() {
		return showDelta ;
	}
	
	public void setCell(String cell) {
		this.cell = cell;
	}
	
	public void setRequiredToPass( boolean val ) {
	    requiredToPass = val ;
	}
	
	protected boolean requiredToPass() {
	    return requiredToPass ;
	}

	public void setExpectedValue(double expectedValue) {
		this.expectedValue = expectedValue;
	}

	public void setPrecision(double precision) {
		this.precision = precision;
	}
	
	protected void setGlobalPrecision( double prec ) {
		globalPrecision = prec ;
	}

	protected String getCell() {
		return cell;
	}

	protected double getExpectedValue() {
		return expectedValue;
	}

	protected double getPrecision() {
		return precisionToUse;
	}
	
	public void execute() throws BuildException {
		
		precisionToUse = 0 ;
		
		// if there is a globalPrecision we will use it unless there is also
		// precision set at the evaluate level, then we use that.  If there
		// is not a globalPrecision, we will use the local precision.
		log( "test precision = " + precision + "\tglobal precision = " + globalPrecision, Project.MSG_VERBOSE ) ;
		if( globalPrecision > 0 ) {
			if( precision > 0 ) {
				precisionToUse = precision ;
				log( "Using evaluate precision of " + precision + " over the " +
						  "global precision of " + globalPrecision, Project.MSG_VERBOSE ) ;
			} else {
				precisionToUse = globalPrecision ;
				log( "Using global precision of " + globalPrecision, Project.MSG_VERBOSE ) ;
			}
		} else {
			precisionToUse = precision ;
			log( "Using evaluate precision of " + precision, Project.MSG_VERBOSE ) ;
		}
		result = wbUtil.evaluateCell(cell, expectedValue, precisionToUse ) ;
		
		StringBuffer sb = new StringBuffer() ;
		sb.append( "evaluation of cell " ) ;
		sb.append( cell ) ; 
		sb.append( " resulted in " ) ;
		sb.append( result.getReturnValue() ) ;
		if( showDelta == true ) {
			sb.append( " with a delta of " + result.getDelta() ) ;
		}
		
		log( sb.toString(), Project.MSG_DEBUG) ;

	}
	
	public ExcelAntEvaluationResult getResult() {
		return result ;
	}
	
	
}
