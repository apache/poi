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

import org.apache.poi.hssf.record.formula.eval.BoolEval;
import org.apache.poi.hssf.record.formula.eval.ErrorEval;
import org.apache.poi.hssf.record.formula.eval.EvaluationException;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.OperandResolver;
import org.apache.poi.hssf.record.formula.eval.RefEval;
import org.apache.poi.hssf.record.formula.eval.StringEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public abstract class LogicalFunction implements Function {

	/**
	 * recursively evaluate any RefEvals TODO - use {@link OperandResolver}
	 */
	private static ValueEval xlateRefEval(RefEval reval) {
		ValueEval retval = reval.getInnerValueEval();

		if (retval instanceof RefEval) {
			RefEval re = (RefEval) retval;
			retval = xlateRefEval(re);
		}

		return retval;
	}

	public final ValueEval evaluate(ValueEval[] operands, int srcCellRow, short srcCellCol) {
		if (operands.length != 1) {
			return ErrorEval.VALUE_INVALID;
		}
		ValueEval ve;
		try {
			ve = OperandResolver.getSingleValue(operands[0], srcCellRow, srcCellCol);
		} catch (EvaluationException e) {
			if (false) {
				// Note - it is more usual to propagate error codes straight to the result like this:
				return e.getErrorEval();
				// but logical functions behave a little differently
			}
			// this will usually cause a 'FALSE' result except for ISNONTEXT()
			ve = e.getErrorEval();
		}
		if (ve instanceof RefEval) {
			ve = xlateRefEval((RefEval) ve);
		}
		return BoolEval.valueOf(evaluate(ve));

	}
	protected abstract boolean evaluate(ValueEval arg);

	public static final Function IsLogical = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return arg instanceof BoolEval;
		}
	};
	public static final Function IsNonText = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return !(arg instanceof StringEval);
		}
	};
	public static final Function IsNumber = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return arg instanceof NumberEval;
		}
	};
	public static final Function IsText = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return arg instanceof StringEval;
		}
	};
}
