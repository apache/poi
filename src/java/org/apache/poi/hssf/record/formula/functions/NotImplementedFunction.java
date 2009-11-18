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

import org.apache.poi.hssf.record.formula.eval.ValueEval;
import org.apache.poi.ss.formula.eval.NotImplementedException;

/**
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 * This is the default implementation of a Function class.
 * The default behaviour is to raise a POI internal error
 * ({@link NotImplementedException}). This error should alert
 * the user that the formula contained a function that is not
 * yet implemented.
 */
public final class NotImplementedFunction implements Function {
	private final String _functionName;
	protected NotImplementedFunction() {
		_functionName = getClass().getName();
	}
	public NotImplementedFunction(String name) {
		_functionName = name;
	}

	public ValueEval evaluate(ValueEval[] operands, int srcRow, int srcCol) {
		throw new NotImplementedException(_functionName);
	}
	public String getFunctionName() {
		return _functionName;
	}
}
