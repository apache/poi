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

package org.apache.poi.hssf.record.formula.function;
/**
 * Holds information about Excel built-in functions.
 * 
 * @author Josh Micich
 */
public final class FunctionMetadata {

	private final int _index;
	private final String _name;
	private final int _minParams;
	private final int _maxParams;

	/* package */ FunctionMetadata(int index, String name, int minParams, int maxParams) {
		_index = index;
		_name = name;
		_minParams = minParams;
		_maxParams = maxParams;
	}
	public int getIndex() {
		return _index;
	}
	public String getName() {
		return _name;
	}
	public int getMinParams() {
		return _minParams;
	}
	public int getMaxParams() {
		return _maxParams;
	}
	public boolean hasFixedArgsLength() {
		return _minParams == _maxParams;
	}
	public String toString() {
		StringBuffer sb = new StringBuffer(64);
		sb.append(getClass().getName()).append(" [");
		sb.append(_index).append(" ").append(_name);
		sb.append("]");
		return sb.toString();
	}
}