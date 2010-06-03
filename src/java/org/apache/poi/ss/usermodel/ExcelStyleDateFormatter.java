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
package org.apache.poi.ss.usermodel;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.math.RoundingMode;
import java.text.*;

/**
 * A wrapper around a {@link SimpleDateFormat} instance,
 *  which handles a few Excel-style extensions that 
 *  are not supported by {@link SimpleDateFormat}.
 * Currently, the extensions are around the handling
 *  of elapsed time, eg rendering 1 day 2 hours
 *  as 26 hours.
 */
public class ExcelStyleDateFormatter extends SimpleDateFormat {
   public static final char MMMMM_START_SYMBOL = '\ue001';
   public static final char MMMMM_TRUNCATE_SYMBOL = '\ue002';
   public static final char H_BRACKET_SYMBOL  = '\ue010';
   public static final char HH_BRACKET_SYMBOL = '\ue011';
   public static final char M_BRACKET_SYMBOL  = '\ue012';
   public static final char MM_BRACKET_SYMBOL = '\ue013';
   public static final char S_BRACKET_SYMBOL  = '\ue014';
   public static final char SS_BRACKET_SYMBOL = '\ue015';
   
   private DecimalFormat format1digit = new DecimalFormat("0");
   private DecimalFormat format2digits = new DecimalFormat("00");
   {
      DataFormatter.setExcelStyleRoundingMode(format1digit);
      DataFormatter.setExcelStyleRoundingMode(format2digits);
   }
   
   private double dateToBeFormatted = 0.0;
   
   public ExcelStyleDateFormatter() {
      super();
   }
   public ExcelStyleDateFormatter(String pattern) {
      super(processFormatPattern(pattern));
   }
   public ExcelStyleDateFormatter(String pattern,
         DateFormatSymbols formatSymbols) {
      super(processFormatPattern(pattern), formatSymbols);
   }
   public ExcelStyleDateFormatter(String pattern, Locale locale) {
      super(processFormatPattern(pattern), locale);
   }

   /**
    * Takes a format String, and replaces Excel specific bits
    *  with our detection sequences
    */
   private static String processFormatPattern(String f) {
      f = f.replaceAll("MMMMM", MMMMM_START_SYMBOL + "MMM" + MMMMM_TRUNCATE_SYMBOL);
      f = f.replaceAll("\\[H\\]",  String.valueOf(H_BRACKET_SYMBOL));
      f = f.replaceAll("\\[HH\\]", String.valueOf(HH_BRACKET_SYMBOL));
      f = f.replaceAll("\\[m\\]",  String.valueOf(M_BRACKET_SYMBOL));
      f = f.replaceAll("\\[mm\\]", String.valueOf(MM_BRACKET_SYMBOL));
      f = f.replaceAll("\\[s\\]",  String.valueOf(S_BRACKET_SYMBOL));
      f = f.replaceAll("\\[ss\\]", String.valueOf(SS_BRACKET_SYMBOL));
      return f;
   }

   /**
    * Used to let us know what the date being
    *  formatted is, in Excel terms, which we
    *  may wish to use when handling elapsed
    *  times.
    */
   public void setDateToBeFormatted(double date) {
      this.dateToBeFormatted = date;
   }
   
   @Override
   public StringBuffer format(Date date, StringBuffer paramStringBuffer,
         FieldPosition paramFieldPosition) {
      // Do the normal format
      String s = super.format(date, paramStringBuffer, paramFieldPosition).toString();
      
      // Now handle our special cases
      if(s.indexOf(MMMMM_START_SYMBOL) != -1) {
         s = s.replaceAll(
               MMMMM_START_SYMBOL + "(\\w)\\w+" + MMMMM_TRUNCATE_SYMBOL,
               "$1"
         );
      }
      
      if(s.indexOf(H_BRACKET_SYMBOL) != -1 ||
            s.indexOf(HH_BRACKET_SYMBOL) != -1) {
         double hours = dateToBeFormatted * 24;
         s = s.replaceAll(
               String.valueOf(H_BRACKET_SYMBOL), 
               format1digit.format(hours)
         );
         s = s.replaceAll(
               String.valueOf(HH_BRACKET_SYMBOL),
               format2digits.format(hours)
         );
      }
      
      if(s.indexOf(M_BRACKET_SYMBOL) != -1 ||
           s.indexOf(MM_BRACKET_SYMBOL) != -1) {
         double minutes = dateToBeFormatted * 24 * 60;
         s = s.replaceAll(
               String.valueOf(M_BRACKET_SYMBOL), 
               format1digit.format(minutes)
         );
         s = s.replaceAll(
               String.valueOf(MM_BRACKET_SYMBOL),
               format2digits.format(minutes)
         );
      }
      if(s.indexOf(S_BRACKET_SYMBOL) != -1 ||
            s.indexOf(SS_BRACKET_SYMBOL) != -1) {
         double seconds = dateToBeFormatted * 24 * 60 * 60;
         s = s.replaceAll(
               String.valueOf(S_BRACKET_SYMBOL), 
               format1digit.format(seconds)
         );
         s = s.replaceAll(
               String.valueOf(SS_BRACKET_SYMBOL),
               format2digits.format(seconds)
         );
      }

      return new StringBuffer(s);
   }
}
