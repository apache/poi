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

package org.apache.poi.ss.formula.functions;

import org.apache.poi.ss.formula.eval.ValueEval;

/**
 * Interface specifying how an algorithm to be used by {@link DStarRunner} should look like.
 * Each implementing class should correspond to one of the D* functions.
 */
public interface IDStarAlgorithm {
    /**
     * Process a match that is found during a run through a database.
     * @param eval ValueEval of the cell in the matching row. References will already be resolved.
     * @return Whether we should continue iterating through the database.
     */
    boolean processMatch(ValueEval eval);
    /**
     * Return a result ValueEval that will be the result of the calculation.
     * This is always called at the end of a run through the database.
     * @return a ValueEval
     */
    ValueEval getResult();
}
