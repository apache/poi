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
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.CompiledFormula;
import org.apache.poi.ss.usermodel.StandaloneFormulaEvaluator;

/**
 * The compiled form of a formula for the standalone formula engine: the parsed token
 * array, shared read-only by all evaluators created from this instance.
 */
final class CompiledFormulaImpl implements CompiledFormula {

    private final StandaloneFormulaEngineImpl _engine;
    private final String _formula;
    private final Ptg[] _tokens;
    private final Map<String, Integer> _inputIndexes;

    CompiledFormulaImpl(StandaloneFormulaEngineImpl engine, String formula, Ptg[] tokens) {
        _engine = engine;
        _formula = formula;
        _tokens = tokens;
        _inputIndexes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        String[] names = engine.inputNames();
        for (int i = 0; i < names.length; i++) {
            _inputIndexes.put(names[i], i);
        }
    }

    @Override
    public String getFormula() {
        return _formula;
    }

    @Override
    public List<String> getInputNames() {
        return Collections.unmodifiableList(_engine.inputNamesList());
    }

    @Override
    public int getInputIndex(String name) {
        Integer index = _inputIndexes.get(name);
        return index == null ? -1 : index;
    }

    @Override
    public StandaloneFormulaEvaluator newEvaluator() {
        return new StandaloneFormulaEvaluatorImpl(this);
    }

    Ptg[] tokens() {
        return _tokens;
    }

    StandaloneFormulaEngineImpl engine() {
        return _engine;
    }
}
