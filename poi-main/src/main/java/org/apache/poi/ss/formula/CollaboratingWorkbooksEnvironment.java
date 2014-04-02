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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Manages a collection of {@link WorkbookEvaluator}s, in order to support evaluation of formulas
 * across spreadsheets.<p/>
 *
 * For POI internal use only
 *
 * @author Josh Micich
 */
public final class CollaboratingWorkbooksEnvironment {

	public static final class WorkbookNotFoundException extends Exception {
		WorkbookNotFoundException(String msg) {
			super(msg);
		}
	}

	public static final CollaboratingWorkbooksEnvironment EMPTY = new CollaboratingWorkbooksEnvironment();

	private final Map<String, WorkbookEvaluator> _evaluatorsByName;
	private final WorkbookEvaluator[] _evaluators;

	private boolean _unhooked;
	private CollaboratingWorkbooksEnvironment() {
		_evaluatorsByName = Collections.emptyMap();
		_evaluators = new WorkbookEvaluator[0];
	}
	public static void setup(String[] workbookNames, WorkbookEvaluator[] evaluators) {
		int nItems = workbookNames.length;
		if (evaluators.length != nItems) {
			throw new IllegalArgumentException("Number of workbook names is " + nItems
					+ " but number of evaluators is " + evaluators.length);
		}
		if (nItems < 1) {
			throw new IllegalArgumentException("Must provide at least one collaborating worbook");
		}
		new CollaboratingWorkbooksEnvironment(workbookNames, evaluators, nItems);
	}

	private CollaboratingWorkbooksEnvironment(String[] workbookNames, WorkbookEvaluator[] evaluators, int nItems) {
		Map<String, WorkbookEvaluator> m = new HashMap<String, WorkbookEvaluator>(nItems * 3 / 2);
		IdentityHashMap<WorkbookEvaluator, String> uniqueEvals = new IdentityHashMap<WorkbookEvaluator, String>(nItems * 3 / 2);
		for(int i=0; i<nItems; i++) {
			String wbName = workbookNames[i];
			WorkbookEvaluator wbEval = evaluators[i];
			if (m.containsKey(wbName)) {
				throw new IllegalArgumentException("Duplicate workbook name '" + wbName + "'");
			}
			if (uniqueEvals.containsKey(wbEval)) {
				String msg = "Attempted to register same workbook under names '"
					+ uniqueEvals.get(wbEval) + "' and '" + wbName + "'";
				throw new IllegalArgumentException(msg);
			}
			uniqueEvals.put(wbEval, wbName);
			m.put(wbName, wbEval);
		}
		unhookOldEnvironments(evaluators);
		hookNewEnvironment(evaluators, this);
		_unhooked = false;
		_evaluators = evaluators;
		_evaluatorsByName = m;
	}

	private static void hookNewEnvironment(WorkbookEvaluator[] evaluators, CollaboratingWorkbooksEnvironment env) {

		// All evaluators will need to share the same cache.
		// but the cache takes an optional evaluation listener.
		int nItems = evaluators.length;
		IEvaluationListener evalListener = evaluators[0].getEvaluationListener();
		// make sure that all evaluators have the same listener
		for(int i=0; i<nItems; i++) {
			if(evalListener != evaluators[i].getEvaluationListener()) {
				// This would be very complex to support
				throw new RuntimeException("Workbook evaluators must all have the same evaluation listener");
			}
		}
		EvaluationCache cache = new EvaluationCache(evalListener);

		for(int i=0; i<nItems; i++) {
			evaluators[i].attachToEnvironment(env, cache, i);
		}
	}

	/**
	 * Completely dismantles all workbook environments that the supplied evaluators are part of
	 */
	private void unhookOldEnvironments(WorkbookEvaluator[] evaluators) {
		Set<CollaboratingWorkbooksEnvironment> oldEnvs = new HashSet<CollaboratingWorkbooksEnvironment>();
		for(int i=0; i<evaluators.length; i++) {
			oldEnvs.add(evaluators[i].getEnvironment());
		}
		CollaboratingWorkbooksEnvironment[] oldCWEs = new CollaboratingWorkbooksEnvironment[oldEnvs.size()];
		oldEnvs.toArray(oldCWEs);
		for (int i = 0; i < oldCWEs.length; i++) {
			oldCWEs[i].unhook();
		}
	}

	/**
	 * Tell all contained evaluators that this environment should be discarded
	 */
	private void unhook() {
		if (_evaluators.length < 1) {
			// Never dismantle the EMPTY environment
			return;
		}
		for (int i = 0; i < _evaluators.length; i++) {
			_evaluators[i].detachFromEnvironment();
		}
		_unhooked = true;
	}

	public WorkbookEvaluator getWorkbookEvaluator(String workbookName) throws WorkbookNotFoundException {
		if (_unhooked) {
			throw new IllegalStateException("This environment has been unhooked");
		}
		WorkbookEvaluator result = _evaluatorsByName.get(workbookName);
		if (result == null) {
			StringBuffer sb = new StringBuffer(256);
			sb.append("Could not resolve external workbook name '").append(workbookName).append("'.");
			if (_evaluators.length < 1) {
				sb.append(" Workbook environment has not been set up.");
			} else {
				sb.append(" The following workbook names are valid: (");
				Iterator<String> i = _evaluatorsByName.keySet().iterator();
				int count=0;
				while(i.hasNext()) {
					if (count++>0) {
						sb.append(", ");
					}
					sb.append("'").append(i.next()).append("'");
				}
				sb.append(")");
			}
			throw new WorkbookNotFoundException(sb.toString());
		}
		return result;
	}
}
