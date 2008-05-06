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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Temporarily collects <tt>FunctionMetadata</tt> instances for creation of a
 * <tt>FunctionMetadataRegistry</tt>.
 * 
 * @author Josh Micich
 */
final class FunctionDataBuilder {
	private int _maxFunctionIndex;
	private final Map _functionDataByName;
	private final Map _functionDataByIndex;
	/** stores indexes of all functions with footnotes (i.e. whose definitions might change) */
	private final Set _mutatingFunctionIndexes;

	public FunctionDataBuilder(int sizeEstimate) {
		_maxFunctionIndex = -1;
		_functionDataByName = new HashMap(sizeEstimate * 3 / 2);
		_functionDataByIndex = new HashMap(sizeEstimate * 3 / 2);
		_mutatingFunctionIndexes = new HashSet();
	}

	public void add(int functionIndex, String functionName, int minParams, int maxParams,
			byte returnClassCode, byte[] parameterClassCodes, boolean hasFootnote) {
		FunctionMetadata fm = new FunctionMetadata(functionIndex, functionName, minParams, maxParams,
				returnClassCode, parameterClassCodes);
		
		Integer indexKey = new Integer(functionIndex);
		
		
		if(functionIndex > _maxFunctionIndex) {
			_maxFunctionIndex = functionIndex;
		}
		// allow function definitions to change only if both previous and the new items have footnotes
		FunctionMetadata prevFM;
		prevFM = (FunctionMetadata) _functionDataByName.get(functionName);
		if(prevFM != null) {
			if(!hasFootnote || !_mutatingFunctionIndexes.contains(indexKey)) {
				throw new RuntimeException("Multiple entries for function name '" + functionName + "'");
			}
			_functionDataByIndex.remove(new Integer(prevFM.getIndex()));
		}
		prevFM = (FunctionMetadata) _functionDataByIndex.get(indexKey);
		if(prevFM != null) {
			if(!hasFootnote || !_mutatingFunctionIndexes.contains(indexKey)) {
				throw new RuntimeException("Multiple entries for function index (" + functionIndex + ")");
			}
			_functionDataByName.remove(prevFM.getName());
		}
		if(hasFootnote) {
			_mutatingFunctionIndexes.add(indexKey);
		}
		_functionDataByIndex.put(indexKey, fm);
		_functionDataByName.put(functionName, fm);
	}

	public FunctionMetadataRegistry build() {

		FunctionMetadata[] jumbledArray =  new FunctionMetadata[_functionDataByName.size()];
		_functionDataByName.values().toArray(jumbledArray);
		FunctionMetadata[] fdIndexArray = new FunctionMetadata[_maxFunctionIndex+1];
		for (int i = 0; i < jumbledArray.length; i++) {
			FunctionMetadata fd = jumbledArray[i];
			fdIndexArray[fd.getIndex()] = fd;
		}
		
		return new FunctionMetadataRegistry(fdIndexArray, _functionDataByName);
	}
}