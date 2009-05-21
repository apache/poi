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

package org.apache.poi.hdf.model.util;


/**
 * Comment me
 *
 * @author Ryan Ackley
 */

public final class NumberFormatter
{
  private final static int ARABIC = 0;
  private final static int UPPER_ROMAN = 1;
  private final static int LOWER_ROMAN = 2;
  private final static int UPPER_LETTER = 3;
  private final static int LOWER_LETTER = 4;
  private final static int ORDINAL = 5;

  private static String[] _arabic = new String[] {"1", "2", "3", "4", "5", "6",
                                                  "7", "8", "9", "10", "11", "12",
                                                  "13", "14", "15", "16", "17", "18",
                                                  "19", "20", "21", "22", "23",
                                                  "24", "25", "26", "27", "28",
                                                  "29", "30", "31", "32", "33",
                                                  "34", "35", "36", "37", "38",
                                                  "39", "40", "41", "42", "43",
                                                  "44", "45", "46", "47", "48",
                                                  "49", "50", "51", "52", "53"};
  private static String[] _roman = new String[]{"i", "ii", "iii", "iv", "v", "vi",
                                                "vii", "viii", "ix", "x", "xi", "xii",
                                                "xiii","xiv", "xv", "xvi", "xvii",
                                                "xviii", "xix", "xx", "xxi", "xxii",
                                                "xxiii", "xxiv", "xxv", "xxvi",
                                                "xxvii", "xxviii", "xxix", "xxx",
                                                "xxxi", "xxxii", "xxxiii", "xxxiv",
                                                "xxxv", "xxxvi", "xxxvii", "xxxvii",
                                                "xxxviii", "xxxix", "xl", "xli", "xlii",
                                                "xliii", "xliv", "xlv", "xlvi", "xlvii",
                                                "xlviii", "xlix", "l"};
  private static String[] _letter = new String[]{"a", "b", "c", "d", "e", "f", "g",
                                                 "h", "i", "j", "k", "l", "m", "n",
                                                 "o", "p", "q", "r", "s", "t", "u",
                                                 "v", "x", "y", "z"};
  public NumberFormatter()
  {
  }
  public static String getNumber(int num, int style)
  {
    switch(style)
    {
      case ARABIC:
        return _arabic[num - 1];
      case UPPER_ROMAN:
        return _roman[num-1].toUpperCase();
      case LOWER_ROMAN:
        return _roman[num-1];
      case UPPER_LETTER:
        return _letter[num-1].toUpperCase();
      case LOWER_LETTER:
        return _letter[num-1];
      case ORDINAL:
        return _arabic[num - 1];
      default:
        return _arabic[num - 1];
    }
  }
}
