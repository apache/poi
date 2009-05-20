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


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public final class Even extends NumericFunction.OneArg {

	private static final long PARITY_MASK = 0xFFFFFFFFFFFFFFFEL;

	protected double evaluate(double d) {
		if (d==0) {
			return 0;
		}
		long result;
		if (d>0) {
			result = calcEven(d);
		} else {
			result = -calcEven(-d);
		}
		return result;
	}

	private static long calcEven(double d) {
		long x = ((long) d) & PARITY_MASK;
		if (x == d) {
			return x;
		}
		return x + 2;
	}
}
