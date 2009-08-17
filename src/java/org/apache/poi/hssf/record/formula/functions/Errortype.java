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
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.hssf.usermodel.HSSFErrorConstants;

/**
 * Implementation for the ERROR.TYPE() Excel function.
 * <p>
 * <b>Syntax:</b><br/>
 * <b>ERROR.TYPE</b>(<b>errorValue</b>)</p>
 * <p>
 * Returns a number corresponding to the error type of the supplied argument.<p/>
 * <p>
 *    <table border="1" cellpadding="1" cellspacing="1" summary="Return values for ERROR.TYPE()">
 *      <tr><td>errorValue</td><td>Return Value</td></tr>
 *      <tr><td>#NULL!</td><td>1</td></tr>
 *      <tr><td>#DIV/0!</td><td>2</td></tr>
 *      <tr><td>#VALUE!</td><td>3</td></tr>
 *      <tr><td>#REF!</td><td>4</td></tr>
 *      <tr><td>#NAME?</td><td>5</td></tr>
 *      <tr><td>#NUM!</td><td>6</td></tr>
 *      <tr><td>#N/A!</td><td>7</td></tr>
 *      <tr><td>everything else</td><td>#N/A!</td></tr>
 *    </table>
 *
 * Note - the results of ERROR.TYPE() are different to the constants defined in
 * <tt>HSSFErrorConstants</tt>.
 * </p>
 *
 * @author Josh Micich
 */
public final class Errortype implements Function {

	public ValueEval evaluate(ValueEval[] args, int srcCellRow, short srcCellCol) {

		try {
			OperandResolver.getSingleValue(args[0], srcCellRow, srcCellCol);
			return ErrorEval.NA;
		} catch (EvaluationException e) {
			int result = translateErrorCodeToErrorTypeValue(e.getErrorEval().getErrorCode());
			return new NumberEval(result);
		}
	}

	private int translateErrorCodeToErrorTypeValue(int errorCode) {
		switch (errorCode) {
			case HSSFErrorConstants.ERROR_NULL:  return 1;
			case HSSFErrorConstants.ERROR_DIV_0: return 2;
			case HSSFErrorConstants.ERROR_VALUE: return 3;
			case HSSFErrorConstants.ERROR_REF:   return 4;
			case HSSFErrorConstants.ERROR_NAME:  return 5;
			case HSSFErrorConstants.ERROR_NUM:   return 6;
			case HSSFErrorConstants.ERROR_NA :   return 7;
		}
		throw new IllegalArgumentException("Invalid error code (" + errorCode + ")");
	}

}
