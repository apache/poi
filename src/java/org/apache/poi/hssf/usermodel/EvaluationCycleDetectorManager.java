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

package org.apache.poi.hssf.usermodel;

/**
 * This class makes an <tt>EvaluationCycleDetector</tt> instance available to
 * each thread via a <tt>ThreadLocal</tt> in order to avoid adding a parameter
 * to a few protected methods within <tt>HSSFFormulaEvaluator</tt>.
 * 
 * @author Josh Micich
 */
final class EvaluationCycleDetectorManager {

	ThreadLocal tl = null;
	private static ThreadLocal _tlEvaluationTracker = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return new EvaluationCycleDetector();
		}
	};

	/**
	 * @return
	 */
	public static EvaluationCycleDetector getTracker() {
		return (EvaluationCycleDetector) _tlEvaluationTracker.get();
	}

	private EvaluationCycleDetectorManager() {
		// no instances of this class
	}
}
