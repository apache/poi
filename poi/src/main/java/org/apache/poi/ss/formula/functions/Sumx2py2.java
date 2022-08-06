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


/**
 * Implementation of Excel function SUMX2PY2()<p>
 *
 * Calculates the sum of squares in two arrays of the same size.<br>
 * <b>Syntax</b>:<br>
 * <b>SUMX2PY2</b>(<b>arrayX</b>, <b>arrayY</b>)<p>
 *
 * result = &Sigma;<sub>i: 0..n</sub>(x<sub>i</sub><sup>2</sup>+y<sub>i</sub><sup>2</sup>)
 */
public final class Sumx2py2 extends XYNumericFunction {

    private static final Accumulator XSquaredPlusYSquaredAccumulator = (x, y) -> x * x + y * y;

    @Override
    protected Accumulator createAccumulator() {
        return XSquaredPlusYSquaredAccumulator;
    }
}
