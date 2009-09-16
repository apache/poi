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

package org.apache.poi.hssf.record.formula.toolpack;

import org.apache.poi.hssf.record.formula.functions.FreeRefFunction;

/**
 * Common interface for any new tool pack with executors for functions.
 * 
 * @author PUdalau
 */
public interface ToolPack {
	/**
	 * Returns executor by specified name. Returns <code>null</code> if tool
	 * pack doesn't contains such function.
	 * 
	 * @param name Name of function.
	 * @return Function executor.
	 */
	FreeRefFunction findFunction(String name);

	/**
	 * Add new function with executor.
	 * @param name Name of function.
	 * @param evaluator Function executor.
	 */
	void addFunction(String name, FreeRefFunction evaluator);

	/**
	 * Returns executor by specified name if it exists.
	 * @param name Name of function.
	 */
	void removeFunction(String name);

	/**
	 * Checks if such function exists in tool pack. 
	 * @param name Name of function.
	 * @return true if tool pack contains such function.
	 */
	boolean containsFunction(String name);
}
