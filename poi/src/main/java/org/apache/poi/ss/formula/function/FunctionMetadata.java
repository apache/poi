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

package org.apache.poi.ss.formula.function;

/**
 * Holds information about Excel built-in functions.
 *
 * @author Josh Micich
 */
public final class FunctionMetadata {
	/**
	 * maxParams=30 in functionMetadata.txt means the maximum number arguments supported
	 * by the given version of Excel. Validation routines should take the actual limit (Excel 97 or 2007)
	 * from the SpreadsheetVersion enum.
	 * Perhaps a value like 'M' should be used instead of '30' in functionMetadata.txt
	 * to make that file more version neutral.
	 * @see org.apache.poi.ss.formula.FormulaParser#validateNumArgs(int, FunctionMetadata)
	 */
	@SuppressWarnings("JavadocReference")
	private static final short FUNCTION_MAX_PARAMS = 30;

	private final int _index;
	private final String _name;
	private final int _minParams;
	private final int _maxParams;
	private final byte _returnClassCode;
	private final byte[] _parameterClassCodes;

	/* package */ FunctionMetadata(int index, String name, int minParams, int maxParams,
			byte returnClassCode, byte[] parameterClassCodes) {
		_index = index;
		_name = name;
		_minParams = minParams;
		_maxParams = maxParams;
		_returnClassCode = returnClassCode;
		_parameterClassCodes = (parameterClassCodes == null) ? null : parameterClassCodes.clone();
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

	public byte getReturnClassCode() {
		return _returnClassCode;
	}

	public byte[] getParameterClassCodes() {
		return _parameterClassCodes.clone();
	}

	/**
	 * Some varags functions (like VLOOKUP) have a specific limit to the number of arguments that 
	 * can be passed.  Other functions (like SUM) don't have such a limit.  For those functions,
	 * the spreadsheet version determines the maximum number of arguments that can be passed.
	 * @return <code>true</code> if this function can the maximum number of arguments allowable by
	 * the {@link org.apache.poi.ss.SpreadsheetVersion}
	 */
	public boolean hasUnlimitedVarags() {
		return FUNCTION_MAX_PARAMS == _maxParams;
	}

	public String toString() {
		return getClass().getName() + " [" + _index + " " + _name + "]";
	}
}
