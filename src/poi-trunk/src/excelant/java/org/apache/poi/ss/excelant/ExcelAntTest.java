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

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.poi.ss.excelant.util.ExcelAntEvaluationResult;
import org.apache.poi.ss.excelant.util.ExcelAntWorkbookUtil;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * This class represents a single test.  In order for the test any and all
 * ExcelAntEvaluateCell evaluations must pass.  Therefore it is recommended
 * that you use only 1 evaluator but you can use more if you choose.
 * 
 * @author Jon Svede ( jon [at] loquatic [dot] com )
 * @author Brian Bush ( brian [dot] bush [at] nrel [dot] gov )
 *
 */
public class ExcelAntTest extends Task{
	private LinkedList<ExcelAntEvaluateCell> evaluators;
	
	private LinkedList<Task> testTasks;
	
	private String name;
	
	private double globalPrecision;
	
	private boolean showSuccessDetails;
	
	private boolean showFailureDetail;
	LinkedList<String> failureMessages;
	

	private ExcelAntWorkbookUtil workbookUtil;
	
	private boolean passed = true;

	
	public ExcelAntTest() {
		evaluators = new LinkedList<>();
		failureMessages = new LinkedList<>();
		testTasks = new LinkedList<>();
	}
	
	public void setPrecision( double precision ) {
		globalPrecision = precision;
	}
	
	public void setWorkbookUtil( ExcelAntWorkbookUtil wbUtil ) {
		workbookUtil = wbUtil;
	}
	
	
	public void setShowFailureDetail( boolean value ) {
		showFailureDetail = value;
	}
	
	public void setName( String nm ) {
		name = nm;
	}
	
	public String getName() {
		return name;
	}
	
	public void setShowSuccessDetails( boolean details ) {
	    showSuccessDetails = details;
	}
	
	public boolean showSuccessDetails() {
	    return showSuccessDetails;
	}
	
	public void addSetDouble( ExcelAntSetDoubleCell setter ) {
	    addSetter( setter );
	}
	
	public void addSetString( ExcelAntSetStringCell setter ){
	    addSetter( setter );
	}
	
	public void addSetFormula( ExcelAntSetFormulaCell setter ) {
	    addSetter( setter );
	}
	
	public void addHandler( ExcelAntHandlerTask handler ) {
	    testTasks.add( handler );
	}
	
	private void addSetter( ExcelAntSet setter ) {
//		setters.add( setter );
		testTasks.add( setter );
	}
	
	public void addEvaluate( ExcelAntEvaluateCell evaluator ) {
//		evaluators.add( evaluator );
		testTasks.add( evaluator );
	}
	
//	public LinkedList<ExcelAntSet> getSetters() {
//		return setters;
//	}

	protected LinkedList<ExcelAntEvaluateCell> getEvaluators() {
		return evaluators;
	}
	
	@Override
    public void execute() throws BuildException {
	    
	    Iterator<Task> taskIt = testTasks.iterator();

	    int testCount = evaluators.size();
	    int failureCount = 0;
	        
	    // roll over all sub task elements in one loop.  This allows the
	    // ordering of the sub elements to be considered. 
	    while( taskIt.hasNext() ) {
	        Task task = taskIt.next();
	        
	       // log( task.getClass().getName(), Project.MSG_INFO );
	        
	        if( task instanceof ExcelAntSet ) {
	            ExcelAntSet set = (ExcelAntSet) task;
	            set.setWorkbookUtil(workbookUtil);
	            set.execute();
	        }
	        
	        if( task instanceof ExcelAntHandlerTask ) {
	            ExcelAntHandlerTask handler = (ExcelAntHandlerTask)task;
	            handler.setEAWorkbookUtil(workbookUtil );
	            handler.execute();
	        }
	        
	        if (task instanceof ExcelAntEvaluateCell ) {
	            ExcelAntEvaluateCell eval = (ExcelAntEvaluateCell)task;
	            eval.setWorkbookUtil( workbookUtil );
	            
	            if( globalPrecision > 0 ) {
	                log( "setting globalPrecision to " + globalPrecision + " in the evaluator", Project.MSG_VERBOSE );
	                eval.setGlobalPrecision( globalPrecision );
	            }

	            try {
	                eval.execute();
	                ExcelAntEvaluationResult result = eval.getResult();
	                if( result.didTestPass() &&
							!result.evaluationCompleteWithError()) {
	                    if(showSuccessDetails) {
	                        log("Succeeded when evaluating " + 
	                         result.getCellName() + ".  It evaluated to " + 
                             result.getReturnValue() + " when the value of " + 
                             eval.getExpectedValue() + " with precision of " + 
                             eval.getPrecision(), Project.MSG_INFO );
	                    }
	                } else {
	                    if(showFailureDetail) {
	                        failureMessages.add( "\tFailed to evaluate cell " + 
	                         result.getCellName() + ".  It evaluated to " + 
	                         result.getReturnValue() + " when the value of " + 
	                         eval.getExpectedValue() + " with precision of " + 
	                         eval.getPrecision() + " was expected." );

	                    }
	                    passed = false;
	                    failureCount++;
	                    
	                    if(eval.requiredToPass()) {
	                        throw new BuildException( "\tFailed to evaluate cell " + 
	                                result.getCellName() + ".  It evaluated to " + 
	                                result.getReturnValue() + " when the value of " + 
	                                eval.getExpectedValue() + " with precision of " + 
	                                eval.getPrecision() + " was expected." );
	                    }
	                }
	            } catch( NullPointerException npe ) {
	                // this means the cell reference in the test is bad.
	                log( "Cell assignment " + eval.getCell() + " in test " + getName() + 
	                      " appears to point to an empy cell.  Please check the " +
	                      " reference in the ant script.", Project.MSG_ERR );
	            }
	        }
	    }
 
		if(!passed) {
			log( "Test named " + name + " failed because " + failureCount + 
					 " of " + testCount + " evaluations failed to " + 
					 "evaluate correctly.", 
					 Project.MSG_ERR );
			if(showFailureDetail && failureMessages.size() > 0 ) {
				for (String failureMessage : failureMessages) {
					log(failureMessage, Project.MSG_ERR);
				}
			}
		}
	}

	public boolean didTestPass() {
		
		return passed;
	}
 }
