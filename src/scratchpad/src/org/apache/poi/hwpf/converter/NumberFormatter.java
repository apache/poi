/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.hwpf.converter;

/**
 * Comment me
 * 
 * @author Ryan Ackley
 */
public final class NumberFormatter
{

    private static String[] C_LETTERS = new String[] { "a", "b", "c", "d", "e",
            "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "x", "y", "z" };

    private static String[] C_ROMAN = new String[] { "i", "ii", "iii", "iv",
            "v", "vi", "vii", "viii", "ix", "x", "xi", "xii", "xiii", "xiv",
            "xv", "xvi", "xvii", "xviii", "xix", "xx", "xxi", "xxii", "xxiii",
            "xxiv", "xxv", "xxvi", "xxvii", "xxviii", "xxix", "xxx", "xxxi",
            "xxxii", "xxxiii", "xxxiv", "xxxv", "xxxvi", "xxxvii", "xxxvii",
            "xxxviii", "xxxix", "xl", "xli", "xlii", "xliii", "xliv", "xlv",
            "xlvi", "xlvii", "xlviii", "xlix", "l" };

    private final static int T_ARABIC = 0;
    private final static int T_LOWER_LETTER = 4;
    private final static int T_LOWER_ROMAN = 2;
    private final static int T_ORDINAL = 5;
    private final static int T_UPPER_LETTER = 3;
    private final static int T_UPPER_ROMAN = 1;

    public static String getNumber( int num, int style )
    {
        switch ( style )
        {
        case T_UPPER_ROMAN:
            return C_ROMAN[num - 1].toUpperCase();
        case T_LOWER_ROMAN:
            return C_ROMAN[num - 1];
        case T_UPPER_LETTER:
            return C_LETTERS[num - 1].toUpperCase();
        case T_LOWER_LETTER:
            return C_LETTERS[num - 1];
        case T_ARABIC:
        case T_ORDINAL:
        default:
            return String.valueOf( num );
        }
    }
}
