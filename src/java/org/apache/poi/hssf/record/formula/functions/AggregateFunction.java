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

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public abstract class AggregateFunction extends MultiOperandNumericFunction {

	protected AggregateFunction() {
		super(false, false);
	}

	public static final Function AVEDEV = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return StatsLib.avedev(values);
		}
	};
	public static final Function AVERAGE = new AggregateFunction() {
		protected double evaluate(double[] values) throws EvaluationException {
			if (values.length < 1) {
				throw new EvaluationException(ErrorEval.DIV_ZERO);
			}
			return MathX.average(values);
		}
	};
	public static final Function DEVSQ = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return StatsLib.devsq(values);
		}
	};
	public static final Function LARGE = new AggregateFunction() {
		protected double evaluate(double[] ops) throws EvaluationException {
			if (ops.length < 2) {
				throw new EvaluationException(ErrorEval.NUM_ERROR);
			}
			double[] values = new double[ops.length-1];
			int k = (int) ops[ops.length-1];
			System.arraycopy(ops, 0, values, 0, values.length);
			return StatsLib.kthLargest(values, k);
		}
	};
	public static final Function MAX = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return values.length > 0 ? MathX.max(values) : 0;
		}
	};
	public static final Function MEDIAN = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return StatsLib.median(values);
		}
	};
	public static final Function MIN = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return values.length > 0 ? MathX.min(values) : 0;
		}
	};
	public static final Function PRODUCT = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return MathX.product(values);
		}
	};
	public static final Function SMALL = new AggregateFunction() {
		protected double evaluate(double[] ops) throws EvaluationException {
			if (ops.length < 2) {
				throw new EvaluationException(ErrorEval.NUM_ERROR);
			}
			double[] values = new double[ops.length-1];
			int k = (int) ops[ops.length-1];
			System.arraycopy(ops, 0, values, 0, values.length);
			return StatsLib.kthSmallest(values, k);
		}
	};
	public static final Function STDEV = new AggregateFunction() {
		protected double evaluate(double[] values) throws EvaluationException {
			if (values.length < 1) {
				throw new EvaluationException(ErrorEval.DIV_ZERO);
			}
			return StatsLib.stdev(values);
		}
	};
	public static final Function SUM = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return MathX.sum(values);
		}
	};
	public static final Function SUMSQ = new AggregateFunction() {
		protected double evaluate(double[] values) {
			return MathX.sumsq(values);
		}
	};
}
