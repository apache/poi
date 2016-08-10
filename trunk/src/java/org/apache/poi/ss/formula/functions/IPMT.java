/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;

public class IPMT extends NumericFunction {

	@Override
	public double eval(ValueEval[] args, int srcCellRow, int srcCellCol) throws EvaluationException {
   		
		if(args.length != 4)
        		throw new EvaluationException(ErrorEval.VALUE_INVALID);

		double result;

		ValueEval v1 = OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol); 
		ValueEval v2 = OperandResolver.getSingleValue(args[1], srcCellRow, srcCellCol); 
		ValueEval v3 = OperandResolver.getSingleValue(args[2], srcCellRow, srcCellCol); 
		ValueEval v4 = OperandResolver.getSingleValue(args[3], srcCellRow, srcCellCol); 

		double interestRate = OperandResolver.coerceValueToDouble(v1);
		int period = OperandResolver.coerceValueToInt(v2);
		int numberPayments = OperandResolver.coerceValueToInt(v3);
		double PV = OperandResolver.coerceValueToDouble(v4);

		result = Finance.ipmt(interestRate, period, numberPayments, PV) ;

		checkValue(result);
		
		return result;
	}

	

}
