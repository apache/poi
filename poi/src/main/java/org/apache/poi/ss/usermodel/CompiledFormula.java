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

package org.apache.poi.ss.usermodel;

import java.util.List;

/**
 * A formula compiled against a {@link StandaloneFormulaEngine}'s inputs, ready to be
 * evaluated repeatedly with different values.
 *
 * <p>Instances are immutable and thread-safe. For evaluation, obtain a
 * {@link StandaloneFormulaEvaluator} - which is <em>not</em> thread-safe and is meant
 * to be confined to a single thread and reused across rows.</p>
 *
 * @see StandaloneFormulaEngine#compile(String)
 */
public interface CompiledFormula {

    /**
     * @return the formula text this instance was compiled from
     */
    String getFormula();

    /**
     * @return the declared input names, in order - the entry at index {@code i}
     *         maps to virtual cell in column {@code i} of row {@code 0}
     */
    List<String> getInputNames();

    /**
     * Case-insensitive lookup of an input index.
     *
     * @param name the input name
     * @return the index to use in {@link StandaloneFormulaEvaluator} setters,
     *         or {@code -1} if the name is not a declared input
     */
    int getInputIndex(String name);

    /**
     * Creates a new evaluator bound to this formula. Evaluators are cheap but
     * <strong>not thread-safe</strong>: create one per thread and reuse it across rows.
     *
     * @return a new evaluator, with all inputs blank
     */
    StandaloneFormulaEvaluator newEvaluator();
}
