/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache POI" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache POI", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */


package org.apache.poi.hdf.extractor.util;


/**
 * Comment me
 *
 * @author Ryan Ackley 
 */

public class NumberFormatter
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