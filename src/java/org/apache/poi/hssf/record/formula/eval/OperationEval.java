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

import org.apache.poi.ss.formula.OperationEvaluationContext;

/**
 * Common interface for implementations of Excel formula operations.
 *
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 *
 */
public interface OperationEval {

	/**
	 * @param args the evaluated operation arguments. Elements of this array typically implement
	 * {@link ValueEval}.  Empty values are represented with {@link BlankEval} or {@link
	 * MissingArgEval}, never <code>null</code>.
	 * @param ec used to identify the current cell under evaluation, and potentially to
	 * dynamically create references
	 * @return The evaluated result, possibly an {@link ErrorEval}, never <code>null</code>.
	 */
	ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec);
	int getNumberOfOperands();
}
