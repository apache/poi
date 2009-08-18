
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


package org.apache.poi.hssf.contrib.view;

import java.text.*;

/**
 * This class is used to format cells into their fractional format.
 *
 * I cant be 100% sure that the same fractional value will be displayed as in
 * excel but then again it is a lossy formating mode anyway
 *
 * @author     Jason Height
 * @since      15 July 2002
 */
public class SVFractionalFormat extends Format {
  private short ONE_DIGIT = 1;
  private short TWO_DIGIT = 2;
  private short THREE_DIGIT = 3;
  private short UNITS = 4;
  private int units = 1;
  private short mode = -1;

  /** Constructs a new FractionalFormatter
   *
   *  The formatStr defines how the number will be formatted
   *  # ?/? Up to one digit
   *  # ??/?? Up to two digits
   *  # ???/??? Up to three digits
   *  # ?/2 In halves
   *  # ?/4 In quarters
   *  # ?/8 In eighths
   *  # ?/16 In sixteenths
   *  # ?/10 In tenths
   *  # ?/100 In hundredths
   */
  public SVFractionalFormat(String formatStr) {
    if ("# ?/?".equals(formatStr))
      mode = ONE_DIGIT;
    else if ("# ??/??".equals(formatStr))
      mode = TWO_DIGIT;
    else if ("# ???/???".equals(formatStr))
      mode = THREE_DIGIT;
    else if ("# ?/2".equals(formatStr)) {
      mode = UNITS;
      units = 2;
    } else if ("# ?/4".equals(formatStr)) {
      mode = UNITS;
      units = 4;
    } else if ("# ?/8".equals(formatStr)) {
      mode = UNITS;
      units = 8;
    } else if ("# ?/16".equals(formatStr)) {
      mode = UNITS;
      units = 16;
    } else if ("# ?/10".equals(formatStr)) {
      mode = UNITS;
      units = 10;
    } else if ("# ?/100".equals(formatStr)) {
      mode = UNITS;
      units = 100;
    }
  }

  /**
   *  Returns a fractional string representation of a double to a maximum denominator size
   *
   * This code has been translated to java from the following web page.
   * http://www.codeproject.com/cpp/fraction.asp
   * Originally coded in c++ By Dean Wyant  dwyant@mindspring.com
   * The code on the web page is freely available.
   *
   * @param  f       Description of the Parameter
   * @param  MaxDen  Description of the Parameter
   * @return         Description of the Return Value
   */
  private String format(final double f, final int MaxDen) {
    long Whole = (long)f;
    int sign = 1;
    if (f < 0) {
      sign = -1;
    }
    double Precision = 0.00001;
    double AllowedError = Precision;
    double d = Math.abs(f);
    d -= Whole;
    double Frac = d;
    double Diff = Frac;
    long Num = 1;
    long Den = 0;
    long A = 0;
    long B = 0;
    long i = 0;
    if (Frac > Precision) {
      while (true) {
        d = 1.0 / d;
        i = (long) (d + Precision);
        d -= i;
        if (A > 0) {
          Num = i * Num + B;
        }
        Den = (long) (Num / Frac + 0.5);
        Diff = Math.abs((double) Num / Den - Frac);
        if (Den > MaxDen) {
          if (A > 0) {
            Num = A;
            Den = (long) (Num / Frac + 0.5);
            Diff = Math.abs((double) Num / Den - Frac);
          } else {
            Den = MaxDen;
            Num = 1;
            Diff = Math.abs((double) Num / Den - Frac);
            if (Diff > Frac) {
              Num = 0;
              Den = 1;
              // Keeps final check below from adding 1 and keeps Den from being 0
              Diff = Frac;
            }
          }
          break;
        }
        if ((Diff <= AllowedError) || (d < Precision)) {
          break;
        }
        Precision = AllowedError / Diff;
        // This calcualtion of Precision does not always provide results within
        // Allowed Error. It compensates for loss of significant digits that occurs.
        // It helps to round the inprecise reciprocal values to i.
        B = A;
        A = Num;
      }
    }
    if (Num == Den) {
      Whole++;
      Num = 0;
      Den = 0;
    } else if (Den == 0) {
      Num = 0;
    }
    if (sign < 0) {
      if (Whole == 0) {
        Num = -Num;
      } else {
        Whole = -Whole;
      }
    }
    return new StringBuffer().append(Whole).append(" ").append(Num).append("/").append(Den).toString();
  }

  /** This method formats the double in the units specified.
   *  The usints could be any number but in this current implementation it is
   *  halves (2), quaters (4), eigths (8) etc
   */
  private String formatUnit(double f, int units) {
    long Whole = (long)f;
    f -= Whole;
    long Num = Math.round(f * units);

    return new StringBuffer().append(Whole).append(" ").append(Num).append("/").append(units).toString();
  }

  public final String format(double val) {
    if (mode == ONE_DIGIT) {
      return format(val, 9);
    } else if (mode == TWO_DIGIT) {
      return format(val, 99);
    } else if (mode == THREE_DIGIT) {
      return format(val, 999);
    } else if (mode == UNITS) {
      return formatUnit(val , units);
    }
    throw new RuntimeException("Unexpected Case");
  }

  public StringBuffer format(Object obj,
                                      StringBuffer toAppendTo,
                                      FieldPosition pos) {
    if (obj instanceof Number) {
      toAppendTo.append(format(((Number)obj).doubleValue()));
      return toAppendTo;
    }
    throw new IllegalArgumentException("Can only handle Numbers");
  }

  public Object parseObject(String source,
                                   ParsePosition status) {
    //JMH TBD
    return null;
  }

  public Object parseObject(String source)
                   throws ParseException {
    //JMH TBD
    return null;
  }

  public Object clone() {
    //JMH TBD
    return null;
  }


}
