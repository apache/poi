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

package org.apache.poi.xddf.usermodel.text;

import java.util.HashMap;

import org.openxmlformats.schemas.drawingml.x2006.main.STTextAutonumberScheme;

public enum AutonumberScheme {
    ALPHABETIC_LOWERCASE_PARENTHESES_BOTH(STTextAutonumberScheme.ALPHA_LC_PAREN_BOTH),
    ALPHABETIC_LOWERCASE_PARENTHESIS_RIGHT(STTextAutonumberScheme.ALPHA_LC_PAREN_R),
    ALPHABETIC_LOWERCASE_PERIOD(STTextAutonumberScheme.ALPHA_LC_PERIOD),
    ALPHABETIC_UPPERCASE_PARENTHESES_BOTH(STTextAutonumberScheme.ALPHA_UC_PAREN_BOTH),
    ALPHABETIC_UPPERCASE_PARENTHESIS_RIGHT(STTextAutonumberScheme.ALPHA_UC_PAREN_R),
    ALPHABETIC_UPPERCASE_PERIOD(STTextAutonumberScheme.ALPHA_UC_PERIOD),
    ARABIC_1_MINUS(STTextAutonumberScheme.ARABIC_1_MINUS),
    ARABIC_2_MINUS(STTextAutonumberScheme.ARABIC_2_MINUS),
    ARABIC_DOUBLE_BYTE_PERIOD(STTextAutonumberScheme.ARABIC_DB_PERIOD),
    ARABIC_DOUBLE_BYTE_PLAIN(STTextAutonumberScheme.ARABIC_DB_PLAIN),
    ARABIC_PARENTHESES_BOTH(STTextAutonumberScheme.ARABIC_PAREN_BOTH),
    ARABIC_PARENTHESIS_RIGHT(STTextAutonumberScheme.ARABIC_PAREN_R),
    ARABIC_PERIOD(STTextAutonumberScheme.ARABIC_PERIOD),
    ARABIC_PLAIN(STTextAutonumberScheme.ARABIC_PLAIN),
    CIRCLE_NUMBER_DOUBLE_BYTE_PLAIN(STTextAutonumberScheme.CIRCLE_NUM_DB_PLAIN),
    CIRCLE_NUMBER_WINGDINGS_BLACK_PLAIN(STTextAutonumberScheme.CIRCLE_NUM_WD_BLACK_PLAIN),
    CIRCLE_NUMBER_WINGDINGS_WHITE_PLAIN(STTextAutonumberScheme.CIRCLE_NUM_WD_WHITE_PLAIN),
    EAST_ASIAN_CHINESE_SIMPLIFIED_PERIOD(STTextAutonumberScheme.EA_1_CHS_PERIOD),
    EAST_ASIAN_CHINESE_SIMPLIFIED_PLAIN(STTextAutonumberScheme.EA_1_CHS_PLAIN),
    EAST_ASIAN_CHINESE_TRADITIONAL_PERIOD(STTextAutonumberScheme.EA_1_CHT_PERIOD),
    EAST_ASIAN_CHINESE_TRADITIONAL_PLAIN(STTextAutonumberScheme.EA_1_CHT_PLAIN),
    EAST_ASIAN_JAPANESE_DOUBLE_BYTE_PERIOD(STTextAutonumberScheme.EA_1_JPN_CHS_DB_PERIOD),
    EAST_ASIAN_JAPANESE_KOREAN_PERIOD(STTextAutonumberScheme.EA_1_JPN_KOR_PERIOD),
    EAST_ASIAN_JAPANESE_KOREAN_PLAIN(STTextAutonumberScheme.EA_1_JPN_KOR_PLAIN),
    HEBREW_2_MINUS(STTextAutonumberScheme.HEBREW_2_MINUS),
    HINDI_ALPHA_1_PERIOD(STTextAutonumberScheme.HINDI_ALPHA_1_PERIOD),
    HINDI_ALPHA_PERIOD(STTextAutonumberScheme.HINDI_ALPHA_PERIOD),
    HINDI_NUMBER_PARENTHESIS_RIGHT(STTextAutonumberScheme.HINDI_NUM_PAREN_R),
    HINDI_NUMBER_PERIOD(STTextAutonumberScheme.HINDI_NUM_PERIOD),
    ROMAN_LOWERCASE_PARENTHESES_BOTH(STTextAutonumberScheme.ROMAN_LC_PAREN_BOTH),
    ROMAN_LOWERCASE_PARENTHESIS_RIGHT(STTextAutonumberScheme.ROMAN_LC_PAREN_R),
    ROMAN_LOWERCASE_PERIOD(STTextAutonumberScheme.ROMAN_LC_PERIOD),
    ROMAN_UPPERCASE_PARENTHESES_BOTH(STTextAutonumberScheme.ROMAN_UC_PAREN_BOTH),
    ROMAN_UPPERCASE_PARENTHESIS_RIGHT(STTextAutonumberScheme.ROMAN_UC_PAREN_R),
    ROMAN_UPPERCASE_PERIOD(STTextAutonumberScheme.ROMAN_UC_PERIOD),
    THAI_ALPHABETIC_PARENTHESES_BOTH(STTextAutonumberScheme.THAI_ALPHA_PAREN_BOTH),
    THAI_ALPHABETIC_PARENTHESIS_RIGHT(STTextAutonumberScheme.THAI_ALPHA_PAREN_R),
    THAI_ALPHABETIC_PERIOD(STTextAutonumberScheme.THAI_ALPHA_PERIOD),
    THAI_NUMBER_PARENTHESES_BOTH(STTextAutonumberScheme.THAI_NUM_PAREN_BOTH),
    THAI_NUMBER_PARENTHESIS_RIGHT(STTextAutonumberScheme.THAI_NUM_PAREN_R),
    THAI_NUMBER_PERIOD(STTextAutonumberScheme.THAI_NUM_PERIOD);

    final STTextAutonumberScheme.Enum underlying;

    AutonumberScheme(STTextAutonumberScheme.Enum scheme) {
        this.underlying = scheme;
    }

    private final static HashMap<STTextAutonumberScheme.Enum, AutonumberScheme> reverse = new HashMap<>();
    static {
        for (AutonumberScheme value : values()) {
            reverse.put(value.underlying, value);
        }
    }

    static AutonumberScheme valueOf(STTextAutonumberScheme.Enum scheme) {
        return reverse.get(scheme);
    }
}
