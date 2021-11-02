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
package org.apache.poi.ss.formula.udf;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.stream.Stream;

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.atp.AnalysisToolPak;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.ss.formula.functions.FreeRefFunction;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class TestUDFFinder {
    public static Stream<Arguments> instances() {
        UDFFinder notImplFinder = new DefaultUDFFinder(
            new String[] { "NotImplemented" },
            new FreeRefFunction[] { TestUDFFinder::notImplemented }
        );

        AggregatingUDFFinder aggUDF = new AggregatingUDFFinder(notImplFinder);
        aggUDF.add(AnalysisToolPak.instance);

        return Stream.of(
            Arguments.of("NotImplemented", notImplFinder),
            Arguments.of("BESSELJ", new AggregatingUDFFinder(AnalysisToolPak.instance)),
            Arguments.of("BESSELJ", aggUDF)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("instances")
    void confirmFindFunction(String functionName, UDFFinder instance) {
        FreeRefFunction func = instance.findFunction(functionName);
        assertNotNull(func);
    }

    private static ValueEval notImplemented(ValueEval[] args, OperationEvaluationContext ec) {
        throw new RuntimeException("not implemented");
    }
}
