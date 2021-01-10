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

package org.apache.poi.ss.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Calendar;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.util.LocaleUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class TestDateParser {
    @ParameterizedTest
    @ValueSource(strings = {
        // no date
        "potato",
        // fail when looks like date but it isnt
        "potato/cucumber/banana",
        // fail when is invalid date
        "13/13/13"
    })
    void testFailWhenInvalidDate(String invalidDate) {
        EvaluationException e = assertThrows(EvaluationException.class,
            () -> DateParser.parseDate(invalidDate), "Shouldn't parse " + invalidDate);
        assertEquals(ErrorEval.VALUE_INVALID, e.getErrorEval());
    }

    @Test
    void testShouldParseValidDate() throws EvaluationException {
        Calendar expDate = LocaleUtil.getLocaleCalendar(1984, Calendar.OCTOBER, 20);
        Calendar actDate = DateParser.parseDate("1984/10/20");
        assertEquals(expDate, actDate,
            "Had: " + expDate.getTime() + " and " + actDate.getTime() + "/" +
            expDate.getTimeInMillis() + "ms and " + actDate.getTimeInMillis() + "ms");
    }

    @Test
    void testShouldIgnoreTimestamp() throws EvaluationException {
        Calendar expDate = LocaleUtil.getLocaleCalendar(1984, Calendar.OCTOBER, 20);
        Calendar actDate = DateParser.parseDate("1984/10/20 12:34:56");
        assertEquals(expDate, actDate,
            "Had: " + expDate.getTime() + " and " + actDate.getTime() + "/" +
            expDate.getTimeInMillis() + "ms and " + actDate.getTimeInMillis() + "ms");
    }

}
