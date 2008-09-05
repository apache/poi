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


/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is a documentation of the observed behaviour of 
 * the '+' operator in Excel:
 * <ol>
 * <li> 1+TRUE = 2
 * <li> 1+FALSE = 1
 * <li> 1+"true" = #VALUE!
 * <li> 1+"1" = 2
 * <li> 1+A1 = #VALUE if A1 contains "1"
 * <li> 1+A1 = 2 if A1 contains ="1"
 * <li> 1+A1 = 2 if A1 contains TRUE or =TRUE
 * <li> 1+A1 = #VALUE! if A1 contains "TRUE" or ="TRUE"
 */
public final class AddEval extends TwoOperandNumericOperation {

	public static final OperationEval instance = new AddEval();

	private AddEval() {
	}

	protected double evaluate(double d0, double d1) {
		return d0 + d1;
	}
}
