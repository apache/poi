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

import java.util.Map;
import java.util.Set;
/**
 * Allows clients to get {@link FunctionMetadata} instances for any built-in function of Excel.
 *
 * @author Josh Micich
 */
public final class FunctionMetadataRegistry {
	/**
	 * The name of the IF function (i.e. "IF").  Extracted as a constant for clarity.
	 */
	public static final String FUNCTION_NAME_IF = "IF";

	public static final int FUNCTION_INDEX_IF = 1;
	public static final short FUNCTION_INDEX_SUM = 4;
	public static final int FUNCTION_INDEX_CHOOSE = 100;
	public static final short FUNCTION_INDEX_INDIRECT = 148;
	public static final short FUNCTION_INDEX_EXTERNAL = 255;

	private static FunctionMetadataRegistry _instance;

	private final FunctionMetadata[] _functionDataByIndex;
	private final Map<String, FunctionMetadata> _functionDataByName;

	private static FunctionMetadataRegistry getInstance() {
		if (_instance == null) {
			_instance = FunctionMetadataReader.createRegistry();
		}
		return _instance;
	}

	/* package */ FunctionMetadataRegistry(FunctionMetadata[] functionDataByIndex, Map<String, FunctionMetadata> functionDataByName) {
		_functionDataByIndex = functionDataByIndex;
		_functionDataByName = functionDataByName;
	}

	/* package */ Set<String> getAllFunctionNames() {
		return _functionDataByName.keySet();
	}


	public static FunctionMetadata getFunctionByIndex(int index) {
		return getInstance().getFunctionByIndexInternal(index);
	}

	private FunctionMetadata getFunctionByIndexInternal(int index) {
		return _functionDataByIndex[index];
	}
	/**
	 * Resolves a built-in function index.
	 * @param name uppercase function name
	 * @return a negative value if the function name is not found.
	 * This typically occurs for external functions.
	 */
	public static short lookupIndexByName(String name) {
		FunctionMetadata fd = getInstance().getFunctionByNameInternal(name);
		if (fd == null) {
			return -1;
		}
		return (short) fd.getIndex();
	}

	private FunctionMetadata getFunctionByNameInternal(String name) {
		return _functionDataByName.get(name);
	}


	public static FunctionMetadata getFunctionByName(String name) {
		return getInstance().getFunctionByNameInternal(name);
	}
}
