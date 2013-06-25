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

import org.apache.poi.ss.formula.OperationEvaluationContext;
import org.apache.poi.ss.formula.eval.*;
import org.apache.poi.ss.usermodel.DateUtil;

import java.util.Calendar;
import java.util.Date;

/**
 * Implementation for Excel EDATE () function.
 */
public class EDate implements FreeRefFunction {
    public static final FreeRefFunction instance = new EDate();

    public ValueEval evaluate(ValueEval[] args, OperationEvaluationContext ec) {
        if (args.length != 2) {
            return ErrorEval.VALUE_INVALID;
        }
        try {
            double startDateAsNumber = getValue(args[0]);
            NumberEval offsetInYearsValue = (NumberEval) args[1];
            int offsetInMonthAsNumber = (int) offsetInYearsValue.getNumberValue();

            Date startDate = DateUtil.getJavaDate(startDateAsNumber);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(startDate);
            calendar.add(Calendar.MONTH, offsetInMonthAsNumber);
            return new NumberEval(DateUtil.getExcelDate(calendar.getTime()));
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }

    private double getValue(ValueEval arg) throws EvaluationException {
        if (arg instanceof NumberEval) {
            return ((NumberEval) arg).getNumberValue();
        }
        if (arg instanceof RefEval) {
            ValueEval innerValueEval = ((RefEval) arg).getInnerValueEval();
            return ((NumberEval) innerValueEval).getNumberValue();
        }
        throw new EvaluationException(ErrorEval.REF_INVALID);
    }
}
