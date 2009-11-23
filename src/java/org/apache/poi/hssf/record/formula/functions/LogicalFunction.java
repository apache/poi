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

import org.apache.poi.hssf.record.formula.eval.AreaEval;
import org.apache.poi.hssf.record.formula.eval.BlankEval;
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
 * @author Josh Micich
 */
public abstract class LogicalFunction extends Fixed1ArgFunction {

	public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
		ValueEval ve;
		try {
			ve = OperandResolver.getSingleValue(arg0, srcRowIndex, srcColumnIndex);
		} catch (EvaluationException e) {
			if (false) {
				// Note - it is more usual to propagate error codes straight to the result like this:
				return e.getErrorEval();
				// but logical functions behave a little differently
			}
			// this will usually cause a 'FALSE' result except for ISNONTEXT()
			ve = e.getErrorEval();
		}
		return BoolEval.valueOf(evaluate(ve));

	}
	/**
	 * @param arg any {@link ValueEval}, potentially {@link BlankEval} or {@link ErrorEval}.
	 */
	protected abstract boolean evaluate(ValueEval arg);

	public static final Function ISLOGICAL = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return arg instanceof BoolEval;
		}
	};
	public static final Function ISNONTEXT = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return !(arg instanceof StringEval);
		}
	};
	public static final Function ISNUMBER = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return arg instanceof NumberEval;
		}
	};
	public static final Function ISTEXT = new LogicalFunction() {
		protected boolean evaluate(ValueEval arg) {
			return arg instanceof StringEval;
		}
	};

	public static final Function ISBLANK = new LogicalFunction() {

		protected boolean evaluate(ValueEval arg) {
			return arg instanceof BlankEval;
		}
	};

	public static final Function ISERROR = new LogicalFunction() {

		protected boolean evaluate(ValueEval arg) {
			return arg instanceof ErrorEval;
		}
	};

	/**
	 * Implementation for Excel ISNA() function.<p/>
	 *
	 * <b>Syntax</b>:<br/>
	 * <b>ISNA</b>(<b>value</b>)<p/>
	 *
	 * <b>value</b>  The value to be tested<br/>
	 * <br/>
	 * Returns <tt>TRUE</tt> if the specified value is '#N/A', <tt>FALSE</tt> otherwise.
	 */
	public static final Function ISNA = new LogicalFunction() {

		protected boolean evaluate(ValueEval arg) {
			return arg == ErrorEval.NA;
		}
	};

	public static final Function ISREF = new Fixed1ArgFunction() {

		public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
			if (arg0 instanceof RefEval || arg0 instanceof AreaEval) {
				return BoolEval.TRUE;
			}
			return BoolEval.FALSE;
		}
	};
}
