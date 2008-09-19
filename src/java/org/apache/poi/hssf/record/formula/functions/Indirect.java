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

package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.EvaluationWorkbook;

/**
 * Implementation for Excel function INDIRECT<p/>
 * 
 * INDIRECT() returns the cell or area reference denoted by the text argument.<p/> 
 * 
 * <b>Syntax</b>:</br>
 * <b>INDIRECT</b>(<b>ref_text</b>,isA1Style)<p/>
 * 
 * <b>ref_text</b> a string representation of the desired reference as it would normally be written
 * in a cell formula.<br/>
 * <b>isA1Style</b> (default TRUE) specifies whether the ref_text should be interpreted as A1-style
 * or R1C1-style.
 * 
 * 
 * @author Josh Micich
 */
public final class Indirect implements FreeRefFunction {

	public ValueEval evaluate(Eval[] args, EvaluationWorkbook workbook, int srcCellSheet, int srcCellRow, int srcCellCol) {
		// TODO - implement INDIRECT()
		return ErrorEval.FUNCTION_NOT_IMPLEMENTED;
	}

}
