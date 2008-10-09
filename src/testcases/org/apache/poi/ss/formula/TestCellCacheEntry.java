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

package org.apache.poi.ss.formula;

import org.apache.poi.hssf.record.formula.eval.BlankEval;
import org.apache.poi.hssf.record.formula.eval.NumberEval;
import org.apache.poi.hssf.record.formula.eval.ValueEval;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Tests {@link CellCacheEntry}.
 *
 * @author Josh Micich
 */
public class TestCellCacheEntry extends TestCase {

	public void testBasic() {
		CellCacheEntry cce = new CellCacheEntry();
		cce.updatePlainValue(new NumberEval(42.0));
		ValueEval ve = cce.getValue();
		assertEquals(42, ((NumberEval)ve).getNumberValue(), 0.0);
		
		cce.setFormulaResult(new NumberEval(10.0), new CellLocation[] { });
	}

	public void testBlank() {
		CellCacheEntry cce = new CellCacheEntry();
		cce.updatePlainValue(BlankEval.INSTANCE);
		try {
			cce.updatePlainValue(BlankEval.INSTANCE);
		} catch (IllegalStateException e) {
			// bug was visible around svn r700356
			throw new AssertionFailedError("cache entry does not handle blank values properly");
		}
	}
}
