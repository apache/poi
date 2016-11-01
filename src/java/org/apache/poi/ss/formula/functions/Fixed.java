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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.poi.ss.formula.eval.BoolEval;
import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.StringEval;
import org.apache.poi.ss.formula.eval.ValueEval;

public final class Fixed implements Function1Arg, Function2Arg, Function3Arg {
    @Override
    public ValueEval evaluate(
            int srcRowIndex, int srcColumnIndex,
            ValueEval arg0, ValueEval arg1, ValueEval arg2) {
        return fixed(arg0, arg1, arg2, srcRowIndex, srcColumnIndex);
    }

    @Override
    public ValueEval evaluate(
            int srcRowIndex, int srcColumnIndex, ValueEval arg0, ValueEval arg1) {
        return fixed(arg0, arg1, BoolEval.FALSE, srcRowIndex, srcColumnIndex);
    }

    @Override
    public ValueEval evaluate(int srcRowIndex, int srcColumnIndex, ValueEval arg0) {
        return fixed(arg0, new NumberEval(2), BoolEval.FALSE, srcRowIndex, srcColumnIndex);
    }

    @Override
    public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
        switch (args.length) {
            case 1:
                return fixed(args[0], new NumberEval(2), BoolEval.FALSE,
                        srcRowIndex, srcColumnIndex);
            case 2:
                return fixed(args[0], args[1], BoolEval.FALSE,
                        srcRowIndex, srcColumnIndex);
            case 3:
                return fixed(args[0], args[1], args[2], srcRowIndex, srcColumnIndex);
        }
        return ErrorEval.VALUE_INVALID;
    }
    
    private ValueEval fixed(
            ValueEval numberParam, ValueEval placesParam,
            ValueEval skipThousandsSeparatorParam,
            int srcRowIndex, int srcColumnIndex) {
        try {
            ValueEval numberValueEval =
                    OperandResolver.getSingleValue(
                    numberParam, srcRowIndex, srcColumnIndex);
            BigDecimal number =
                    new BigDecimal(OperandResolver.coerceValueToDouble(numberValueEval));
            ValueEval placesValueEval =
                    OperandResolver.getSingleValue(
                    placesParam, srcRowIndex, srcColumnIndex);
            int places = OperandResolver.coerceValueToInt(placesValueEval);
            ValueEval skipThousandsSeparatorValueEval =
                    OperandResolver.getSingleValue(
                    skipThousandsSeparatorParam, srcRowIndex, srcColumnIndex);
            Boolean skipThousandsSeparator =
                    OperandResolver.coerceValueToBoolean(
                    skipThousandsSeparatorValueEval, false);
            
            // Round number to respective places.
            number = number.setScale(places, RoundingMode.HALF_UP);
            
            // Format number conditionally using a thousands separator.
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            DecimalFormat formatter = (DecimalFormat)nf;
            formatter.setGroupingUsed(!(skipThousandsSeparator != null && skipThousandsSeparator));
            formatter.setMinimumFractionDigits(places >= 0 ? places : 0);
            formatter.setMaximumFractionDigits(places >= 0 ? places : 0);
            String numberString = formatter.format(number.doubleValue());

            // Return the result as a StringEval.
            return new StringEval(numberString);
        } catch (EvaluationException e) {
            return e.getErrorEval();
        }
    }
}
