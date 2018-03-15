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

package org.apache.poi.ss.formula.atp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Calendar;

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.util.LocaleUtil;
import org.junit.Test;

public class TestDateParser {
    @Test
    public void testFailWhenNoDate() {
        try {
            DateParser.parseDate("potato");
            fail("Shouldn't parse potato!");
        } catch (EvaluationException e) {
            assertEquals(ErrorEval.VALUE_INVALID, e.getErrorEval());
        }
    }

    @Test
    public void testFailWhenLooksLikeDateButItIsnt() {
        try {
            DateParser.parseDate("potato/cucumber/banana");
            fail("Shouldn't parse this thing!");
        } catch (EvaluationException e) {
            assertEquals(ErrorEval.VALUE_INVALID, e.getErrorEval());
        }
    }

    @Test
    public void testFailWhenIsInvalidDate() {
        try {
            DateParser.parseDate("13/13/13");
            fail("Shouldn't parse this thing!");
        } catch (EvaluationException e) {
            assertEquals(ErrorEval.VALUE_INVALID, e.getErrorEval());
        }
    }

    @Test
    public void testShouldParseValidDate() throws EvaluationException {
        Calendar expDate = LocaleUtil.getLocaleCalendar(1984, Calendar.OCTOBER, 20);
        Calendar actDate = DateParser.parseDate("1984/10/20");
        assertEquals("Had: " + expDate.getTime() + " and " + actDate.getTime() + "/" + 
                expDate.getTimeInMillis() + "ms and " + actDate.getTimeInMillis() + "ms", 
                expDate, actDate);
    }

    @Test
    public void testShouldIgnoreTimestamp() throws EvaluationException {
        Calendar expDate = LocaleUtil.getLocaleCalendar(1984, Calendar.OCTOBER, 20);
        Calendar actDate = DateParser.parseDate("1984/10/20 12:34:56");
        assertEquals("Had: " + expDate.getTime() + " and " + actDate.getTime() + "/" + 
                expDate.getTimeInMillis() + "ms and " + actDate.getTimeInMillis() + "ms", 
                expDate, actDate);
    }

}
