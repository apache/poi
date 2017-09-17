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

import org.apache.poi.ss.formula.eval.ErrorEval;
import org.apache.poi.ss.formula.eval.EvaluationException;
import org.apache.poi.ss.formula.eval.NumberEval;
import org.apache.poi.ss.formula.eval.OperandResolver;
import org.apache.poi.ss.formula.eval.ValueEval;
import org.apache.poi.util.POILogFactory;
import org.apache.poi.util.POILogger;

/**
 * Implements the Excel Rate function
 */
public class Rate implements Function {
    private static final POILogger LOG = POILogFactory.getLogger(Rate.class);
    
   public ValueEval evaluate(ValueEval[] args, int srcRowIndex, int srcColumnIndex) {
      if (args.length < 3) { //First 3 parameters are mandatory
         return ErrorEval.VALUE_INVALID;
      }

      double periods, payment, present_val, future_val = 0, type = 0, estimate = 0.1, rate;

      try {
         ValueEval v1 = OperandResolver.getSingleValue(args[0], srcRowIndex, srcColumnIndex);
         ValueEval v2 = OperandResolver.getSingleValue(args[1], srcRowIndex, srcColumnIndex);
         ValueEval v3 = OperandResolver.getSingleValue(args[2], srcRowIndex, srcColumnIndex);
         ValueEval v4 = null;
         if (args.length >= 4)
            v4 = OperandResolver.getSingleValue(args[3], srcRowIndex, srcColumnIndex);
         ValueEval v5 = null;
         if (args.length >= 5)
            v5 = OperandResolver.getSingleValue(args[4], srcRowIndex, srcColumnIndex);
         ValueEval v6 = null;
         if (args.length >= 6)
            v6 = OperandResolver.getSingleValue(args[5], srcRowIndex, srcColumnIndex);

         periods = OperandResolver.coerceValueToDouble(v1); 
         payment = OperandResolver.coerceValueToDouble(v2);
         present_val = OperandResolver.coerceValueToDouble(v3);
         if (args.length >= 4)
            future_val = OperandResolver.coerceValueToDouble(v4);
         if (args.length >= 5)
            type = OperandResolver.coerceValueToDouble(v5);
         if (args.length >= 6)
            estimate = OperandResolver.coerceValueToDouble(v6);
         rate = calculateRate(periods, payment, present_val, future_val, type, estimate) ;

         checkValue(rate);
      } catch (EvaluationException e) {
          LOG.log(POILogger.ERROR, "Can't evaluate rate function", e);
         return e.getErrorEval();
      }

      return new NumberEval( rate ) ;
   }

   private double calculateRate(double nper, double pmt, double pv, double fv, double type, double guess) {
      //FROM MS http://office.microsoft.com/en-us/excel-help/rate-HP005209232.aspx
      int FINANCIAL_MAX_ITERATIONS = 20;//Bet accuracy with 128
      double FINANCIAL_PRECISION = 0.0000001;//1.0e-8

      double y, y0, y1, x0, x1 = 0, f = 0, i = 0;
      double rate = guess;
      if (Math.abs(rate) < FINANCIAL_PRECISION) {
         y = pv * (1 + nper * rate) + pmt * (1 + rate * type) * nper + fv;
      } else {
         f = Math.exp(nper * Math.log(1 + rate));
         y = pv * f + pmt * (1 / rate + type) * (f - 1) + fv;
      }
      y0 = pv + pmt * nper + fv;
      y1 = pv * f + pmt * (1 / rate + type) * (f - 1) + fv;

      // find root by Newton secant method
      i = x0 = 0.0;
      x1 = rate;
      while ((Math.abs(y0 - y1) > FINANCIAL_PRECISION) && (i < FINANCIAL_MAX_ITERATIONS)) {
         rate = (y1 * x0 - y0 * x1) / (y1 - y0);
         x0 = x1;
         x1 = rate;

         if (Math.abs(rate) < FINANCIAL_PRECISION) {
            y = pv * (1 + nper * rate) + pmt * (1 + rate * type) * nper + fv;
         } else {
            f = Math.exp(nper * Math.log(1 + rate));
            y = pv * f + pmt * (1 / rate + type) * (f - 1) + fv;
         }

         y0 = y1;
         y1 = y;
         ++i;
      }
      return rate;
   }

   /**
    * Excel does not support infinities and NaNs, rather, it gives a #NUM! error in these cases
    * 
    * @throws EvaluationException (#NUM!) if <tt>result</tt> is <tt>NaN</> or <tt>Infinity</tt>
    */
   static void checkValue(double result) throws EvaluationException {
      if (Double.isNaN(result) || Double.isInfinite(result)) {
         throw new EvaluationException(ErrorEval.NUM_ERROR);
      }
   }
}
