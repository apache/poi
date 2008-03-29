/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on May 15, 2005
 *
 */
package org.apache.poi.hssf.record.formula.functions;

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.Eval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * Counts the number of cells that contain numeric data within
 *  the list of arguments. 
 *
 * Excel Syntax
 * COUNT(value1,value2,...)
 * Value1, value2, ...   are 1 to 30 arguments representing the values or ranges to be counted.
 * 
 * TODO: Check this properly matches excel on edge cases
 *  like formula cells, error cells etc
 */
public class Count implements Function {

	public Eval evaluate(Eval[] args, int srcCellRow, short srcCellCol) {
		int nArgs = args.length;
		if (nArgs < 1) {
			// too few arguments
			return ErrorEval.VALUE_INVALID;
		}

		if (nArgs > 30) {
			// too many arguments
			return ErrorEval.VALUE_INVALID;
		}
		
		int temp = 0;
		
		for(int i=0; i<nArgs; i++) {
			temp += countArg(args[i]);
			
		}
		return new NumberEval(temp);
	}

	private static int countArg(Eval eval) {
        if (eval instanceof AreaEval) {
            AreaEval ae = (AreaEval) eval;
            return countAreaEval(ae);
        }
        if (eval instanceof RefEval) {
            RefEval refEval = (RefEval)eval;
			return countValue(refEval.getInnerValueEval());
        }
        if (eval instanceof NumberEval) {
            return 1;
        }
		
		throw new RuntimeException("Unexpected eval type (" + eval.getClass().getName() + ")");
	}

	private static int countAreaEval(AreaEval ae) {
		
		int temp = 0;
		ValueEval[] values = ae.getValues();
		for (int i = 0; i < values.length; i++) {
			ValueEval val = values[i];
			if(val == null) {
				// seems to occur.  Really we would have expected BlankEval
				continue;
			}
			temp += countValue(val);
			
		}
		return temp;
	}

	private static int countValue(ValueEval valueEval) {
		
		if(valueEval == BlankEval.INSTANCE) {
			return 0;
		}
		
		if(valueEval instanceof BlankEval) {
			// wouldn't need this if BlankEval was final
			return 0;
		}

		if(valueEval instanceof ErrorEval) {
			// note - error values not counted
			return 0;
		}
		
		if(valueEval instanceof NumberEval)
			return 1;

		return 0;
	}
}