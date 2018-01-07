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

import java.util.Locale;

import org.apache.poi.util.Beta;

/**
 * Utility class to translate numbers in letters, usually for lists.
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 */
@Beta
public final class NumberFormatter {
    private static final String[] ROMAN_LETTERS = { "m", "cm", "d", "cd", "c",
            "xc", "l", "xl", "x", "ix", "v", "iv", "i" };

    private static final int[] ROMAN_VALUES = { 1000, 900, 500, 400, 100, 90,
            50, 40, 10, 9, 5, 4, 1 };

    private static final int T_ARABIC = 0;
    private static final int T_LOWER_LETTER = 4;
    private static final int T_LOWER_ROMAN = 2;
    private static final int T_ORDINAL = 5;
    private static final int T_UPPER_LETTER = 3;
    private static final int T_UPPER_ROMAN = 1;

    public static String getNumber( int num, int style )
    {
        switch ( style )
        {
        case T_UPPER_ROMAN:
            return toRoman( num ).toUpperCase(Locale.ROOT);
        case T_LOWER_ROMAN:
            return toRoman( num );
        case T_UPPER_LETTER:
            return toLetters( num ).toUpperCase(Locale.ROOT);
        case T_LOWER_LETTER:
            return toLetters( num );
        case T_ARABIC:
        case T_ORDINAL:
        default:
            return String.valueOf( num );
        }
    }
    
    private static String toLetters(int number) {
        if ( number <= 0 ) {
            throw new IllegalArgumentException( "Unsupported number: " + number );
        }

        int num = number;
        final int radix = 26;

        char buf[] = new char[33];
        int charPos = buf.length;

        while (num > 0) {
            num--; // 1 => a, not 0 => a
            int remainder = num % radix;
            buf[--charPos] = (char)('a'+remainder);
            num = (num - remainder) / radix;
        }

        return new String(buf, charPos, (buf.length - charPos));
    }

    private static String toRoman( int number )
    {
        if ( number <= 0 )
            throw new IllegalArgumentException( "Unsupported number: " + number );

        StringBuilder result = new StringBuilder();

        for ( int i = 0; i < ROMAN_LETTERS.length; i++ )
        {
            String letter = ROMAN_LETTERS[i];
            int value = ROMAN_VALUES[i];
            while ( number >= value )
            {
                number -= value;
                result.append( letter );
            }
        }
        return result.toString();
    }
}
