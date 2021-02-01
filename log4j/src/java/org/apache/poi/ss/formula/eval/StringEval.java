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

package org.apache.poi.ss.formula.eval;

//import org.checkerframework.checker.nullness.qual.NonNull;

import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.StringPtg;

/**
 * @author Amol S. Deshmukh &lt; amolweb at ya hoo dot com &gt;
 */
public final class StringEval implements StringValueEval {

	public static final StringEval EMPTY_INSTANCE = new StringEval("");

	//@NotNull
	private final String _value;

	public StringEval(Ptg ptg) {
		this(((StringPtg) ptg).getValue());
	}

	public StringEval(String value) {
		if (value == null) {
			throw new IllegalArgumentException("value must not be null");
		}
		_value = value;
	}

	public String getStringValue() {
		return _value;
	}

	public String toString() {
		return getClass().getName() + " [" +
				_value +
				"]";
	}
}
