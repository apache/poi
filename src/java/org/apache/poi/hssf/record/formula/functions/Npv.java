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

import org.apache.poi.hssf.record.formula.eval.EvaluationException;

/**
 * Calculates the net present value of an investment by using a discount rate
 * and a series of future payments (negative values) and income (positive
 * values). Minimum 2 arguments, first arg is the rate of discount over the
 * length of one period others up to 254 arguments representing the payments and
 * income.
 * 
 * @author SPetrakovsky
 */
public class Npv extends NumericFunction.MultiArg {

	public Npv() {
		super(2, 255);
	}

	@Override
	protected double evaluate(double[] ds) throws EvaluationException {
		double rate = ds[0];
		double sum = 0;
		for (int i = 1; i < ds.length; i++) {
			sum += ds[i] / Math.pow(rate + 1, i);
		}
		return sum;
	}

}
