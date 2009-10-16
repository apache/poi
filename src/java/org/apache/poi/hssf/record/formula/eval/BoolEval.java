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
 */
public final class BoolEval implements NumericValueEval, StringValueEval {

	private boolean _value;

	public static final BoolEval FALSE = new BoolEval(false);

	public static final BoolEval TRUE = new BoolEval(true);

	/**
	 * Convenience method for the following:<br/>
	 * <code>(b ? BoolEval.TRUE : BoolEval.FALSE)</code>
	 *
	 * @return the <tt>BoolEval</tt> instance representing <tt>b</tt>.
	 */
	public static final BoolEval valueOf(boolean b) {
		return b ? TRUE : FALSE;
	}

	private BoolEval(boolean value) {
		_value = value;
	}

	public boolean getBooleanValue() {
		return _value;
	}

	public double getNumberValue() {
		return _value ? 1 : 0;
	}

	public String getStringValue() {
		return _value ? "TRUE" : "FALSE";
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(getStringValue());
		sb.append("]");
		return sb.toString();
	}
}
