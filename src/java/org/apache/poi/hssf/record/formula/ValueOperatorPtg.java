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

package org.apache.poi.hssf.record.formula;

import org.apache.poi.util.LittleEndianOutput;

/**
 * Common superclass of all value operators. Subclasses include all unary and
 * binary operators except for the reference operators (IntersectionPtg,
 * RangePtg, UnionPtg)
 * 
 * @author Josh Micich
 */
public abstract class ValueOperatorPtg extends OperationPtg {

	/**
	 * All Operator <tt>Ptg</tt>s are base tokens (i.e. are not RVA classified)
	 */
	public final boolean isBaseToken() {
		return true;
	}

	public final byte getDefaultOperandClass() {
		return Ptg.CLASS_VALUE;
	}

	public void write(LittleEndianOutput out) {
		out.writeByte(getSid());
	}

	protected abstract byte getSid();

	public final int getSize() {
		return 1;
	}

	public final String toFormulaString() {
		// TODO - prune this method out of the hierarchy
		throw new RuntimeException("toFormulaString(String[] operands) should be used for subclasses of OperationPtgs");
	}
}
