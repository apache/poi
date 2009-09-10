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

package org.apache.poi.hssf.record.formula.eval;

import org.apache.poi.hssf.record.formula.AbstractFunctionPtg;
import org.apache.poi.hssf.record.formula.function.FunctionMetadata;
import org.apache.poi.hssf.record.formula.function.FunctionMetadataRegistry;
import org.apache.poi.hssf.record.formula.functions.*;
import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.NotImplementedException;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public final class FunctionEval implements OperationEval {
	/**
	 * Some function IDs that require special treatment
	 */
	private static final class FunctionID {
		/** 4 */
		public static final int SUM = FunctionMetadataRegistry.FUNCTION_INDEX_SUM;
		/** 78 */
		public static final int OFFSET = 78;
		/** 148 */
		public static final int INDIRECT = 148;
		/** 255 */
		public static final int EXTERNAL_FUNC = FunctionMetadataRegistry.FUNCTION_INDEX_EXTERNAL;
	}
	// convenient access to namespace
	private static final FunctionID ID = null;

	/**
	 * Array elements corresponding to unimplemented functions are <code>null</code>
	 */
	protected static final Function[] functions = produceFunctions();

	private static Function[] produceFunctions() {
		Function[] retval = new Function[368];

		retval[0] = new Count();
		retval[1] = new If();
		retval[2] = new IsNa();
		retval[3] = new IsError();
		retval[ID.SUM] = AggregateFunction.SUM;
		retval[5] = AggregateFunction.AVERAGE;
		retval[6] = AggregateFunction.MIN;
		retval[7] = AggregateFunction.MAX;
		retval[8] = new Row(); // ROW
		retval[9] = new Column();
		retval[10] = new Na();
		retval[11] = new Npv();
		retval[12] = AggregateFunction.STDEV;
		retval[13] = NumericFunction.DOLLAR;

		retval[15] = NumericFunction.SIN;
		retval[16] = NumericFunction.COS;
		retval[17] = NumericFunction.TAN;
		retval[18] = NumericFunction.ATAN;
		retval[19] = new Pi();
		retval[20] = NumericFunction.SQRT;
		retval[21] = NumericFunction.EXP;
		retval[22] = NumericFunction.LN;
		retval[23] = NumericFunction.LOG10;
		retval[24] = NumericFunction.ABS;
		retval[25] = NumericFunction.INT;
		retval[26] = NumericFunction.SIGN;
		retval[27] = NumericFunction.ROUND;
		retval[28] = new Lookup();
		retval[29] = new Index();

		retval[31] = TextFunction.MID;
		retval[32] = TextFunction.LEN;
		retval[33] = new Value();
		retval[34] = new True();
		retval[35] = new False();
		retval[36] = new And();
		retval[37] = new Or();
		retval[38] = new Not();
		retval[39] = NumericFunction.MOD;

		retval[56] = FinanceFunction.PV;
		retval[57] = FinanceFunction.FV;
		retval[58] = FinanceFunction.NPER;
		retval[59] = FinanceFunction.PMT;

		retval[63] = new Rand();
		retval[64] = new Match();
		retval[65] = DateFunc.instance;
		retval[66] = new Time();
		retval[67] = CalendarFieldFunction.DAY;
		retval[68] = CalendarFieldFunction.MONTH;
		retval[69] = CalendarFieldFunction.YEAR;

		retval[74] = new Now();

		retval[76] = new Rows();
		retval[77] = new Columns();
		retval[ID.OFFSET] = new Offset();

		retval[97] = NumericFunction.ATAN2;
		retval[98] = NumericFunction.ASIN;
		retval[99] = NumericFunction.ACOS;
		retval[100] = new Choose();
		retval[101] = new Hlookup();
		retval[102] = new Vlookup();

		retval[105] = new Isref();

		retval[109] = NumericFunction.LOG;

		retval[112] = TextFunction.LOWER;
		retval[113] = TextFunction.UPPER;

		retval[115] = TextFunction.LEFT;
		retval[116] = TextFunction.RIGHT;
		retval[117] = TextFunction.EXACT;
		retval[118] = TextFunction.TRIM;
		retval[119] = new Replace();
		retval[120] = new Substitute();

		retval[124] = new Find();

		retval[127] = LogicalFunction.IsText;
		retval[128] = LogicalFunction.IsNumber;
		retval[129] = new Isblank();
		retval[130] = new T();

		retval[ID.INDIRECT] = null; // Indirect.evaluate has different signature

		retval[169] = new Counta();

		retval[183] = AggregateFunction.PRODUCT;
		retval[184] = NumericFunction.FACT;

		retval[190] = LogicalFunction.IsNonText;

		retval[198] = LogicalFunction.IsLogical;

		retval[212] = NumericFunction.ROUNDUP;
		retval[213] = NumericFunction.ROUNDDOWN;

        retval[220] = new Days360();
		retval[221] = new Today();

		retval[227] = AggregateFunction.MEDIAN;
		retval[228] = new Sumproduct();
		retval[229] = NumericFunction.SINH;
		retval[230] = NumericFunction.COSH;
		retval[231] = NumericFunction.TANH;
		retval[232] = NumericFunction.ASINH;
		retval[233] = NumericFunction.ACOSH;
		retval[234] = NumericFunction.ATANH;

		retval[ID.EXTERNAL_FUNC] = null; // ExternalFunction is a FreeREfFunction

		retval[261] = new Errortype();

		retval[269] = AggregateFunction.AVEDEV;

		retval[276] = NumericFunction.COMBIN;

		retval[279] = new Even();

		retval[285] = NumericFunction.FLOOR;

		retval[288] = NumericFunction.CEILING;

		retval[298] = new Odd();

		retval[303] = new Sumxmy2();
		retval[304] = new Sumx2my2();
		retval[305] = new Sumx2py2();

		retval[318] = AggregateFunction.DEVSQ;

		retval[321] = AggregateFunction.SUMSQ;

		retval[325] = AggregateFunction.LARGE;
		retval[326] = AggregateFunction.SMALL;

		retval[330] = new Mode();

		retval[336] = TextFunction.CONCATENATE;
		retval[337] = NumericFunction.POWER;

		retval[342] = NumericFunction.RADIANS;
		retval[343] = NumericFunction.DEGREES;

		retval[345] = new Sumif();
		retval[346] = new Countif();

		retval[359] = new Hyperlink();

		retval[362] = MinaMaxa.MAXA;
		retval[363] = MinaMaxa.MINA;

		for (int i = 0; i < retval.length; i++) {
			Function f = retval[i];
			if (f == null) {
				FunctionMetadata fm = FunctionMetadataRegistry.getFunctionByIndex(i);
				if (fm == null) {
					continue;
				}
				retval[i] = new NotImplementedFunction(fm.getName());
			}
		}
		return retval;
	}

	private AbstractFunctionPtg _delegate;

	public FunctionEval(AbstractFunctionPtg funcPtg) {
		_delegate = funcPtg;
	}

	public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
		int fidx = _delegate.getFunctionIndex();
		// check for 'free ref' functions first
		switch (fidx) {
			case FunctionID.INDIRECT:
				return Indirect.instance.evaluate(args, ec);
			case FunctionID.EXTERNAL_FUNC:
				return UserDefinedFunction.instance.evaluate(args, ec);
		}
		// else - must be plain function
		Function f = functions[fidx];
		if (f == null) {
			throw new NotImplementedException("FuncIx=" + fidx);
		}
		int srcCellRow = ec.getRowIndex();
		int srcCellCol = ec.getColumnIndex();
		return f.evaluate(args, srcCellRow, (short) srcCellCol);
	}

	public int getNumberOfOperands() {
		return _delegate.getNumberOfOperands();
	}
}
