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

/**
 * Enumeration of various formula types.<br/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
public final class FormulaType {
	private FormulaType() {
		// no instances of this class
	}
	public static final int CELL = 0;
	public static final int SHARED = 1;
	public static final int ARRAY =2;
	public static final int CONDFORMAT = 3;
	public static final int NAMEDRANGE = 4;
	// this constant is currently very specific.  The exact differences from general data
	// validation formulas or conditional format formulas is not known yet
	public static final int DATAVALIDATION_LIST = 5;

}
