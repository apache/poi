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

package org.apache.poi.sl.usermodel;

import java.util.Locale;

public enum AutoNumberingScheme {
    /** Lowercase alphabetic character enclosed in parentheses. Example: (a), (b), (c), ... */
    alphaLcParenBoth(0x0008, 1),
    /** Uppercase alphabetic character enclosed in parentheses. Example: (A), (B), (C), ... */
    alphaUcParenBoth(0x000A, 2),
    /** Lowercase alphabetic character followed by a closing parenthesis. Example: a), b), c), ... */
    alphaLcParenRight(0x0009, 3),
    /** Uppercase alphabetic character followed by a closing parenthesis. Example: A), B), C), ... */
    alphaUcParenRight(0x000B, 4),
    /** Lowercase Latin character followed by a period. Example: a., b., c., ... */
    alphaLcPeriod(0x0000, 5),
    /** Uppercase Latin character followed by a period. Example: A., B., C., ... */
    alphaUcPeriod(0x0001, 6),
    /** Arabic numeral enclosed in parentheses. Example: (1), (2), (3), ... */
    arabicParenBoth(0x000C, 7),
    /** Arabic numeral followed by a closing parenthesis. Example: 1), 2), 3), ... */
    arabicParenRight(0x0002, 8),
    /** Arabic numeral followed by a period. Example: 1., 2., 3., ... */
    arabicPeriod(0x0003, 9),
    /** Arabic numeral. Example: 1, 2, 3, ... */
    arabicPlain(0x000D, 10),
    /** Lowercase Roman numeral enclosed in parentheses. Example: (i), (ii), (iii), ... */
    romanLcParenBoth(0x0004, 11),
    /** Uppercase Roman numeral enclosed in parentheses. Example: (I), (II), (III), ... */
    romanUcParenBoth(0x000E, 12),
    /** Lowercase Roman numeral followed by a closing parenthesis. Example: i), ii), iii), ... */
    romanLcParenRight(0x0005, 13),
    /** Uppercase Roman numeral followed by a closing parenthesis. Example: I), II), III), .... */
    romanUcParenRight(0x000F, 14),
    /** Lowercase Roman numeral followed by a period. Example: i., ii., iii., ... */
    romanLcPeriod(0x0006, 15),
    /** Uppercase Roman numeral followed by a period. Example: I., II., III., ... */
    romanUcPeriod(0x0007, 16),
    /** Double byte circle numbers. */
    circleNumDbPlain(0x0012, 17),
    /** Wingdings black circle numbers. */
    circleNumWdBlackPlain(0x0014, 18),
    /** Wingdings white circle numbers. */
    circleNumWdWhitePlain(0x0013, 19),
    /** Double-byte Arabic numbers with double-byte period. */
    arabicDbPeriod(0x001D, 20),
    /** Double-byte Arabic numbers. */
    arabicDbPlain(0x001C, 21),
    /** Simplified Chinese with single-byte period. */
    ea1ChsPeriod(0x0011, 22),
    /** Simplified Chinese. */
    ea1ChsPlain(0x0010, 23),
    /** Traditional Chinese with single-byte period. */
    ea1ChtPeriod(0x0015, 24),
    /** Traditional Chinese. */
    ea1ChtPlain(0x0014, 25),
    /** Japanese with double-byte period. */
    ea1JpnChsDbPeriod(0x0026, 26),
    /** Japanese/Korean. */
    ea1JpnKorPlain(0x001A, 27),
    /** Japanese/Korean with single-byte period. */
    ea1JpnKorPeriod(0x001B, 28),
    /** Bidi Arabic 1 (AraAlpha) with ANSI minus symbol. */
    arabic1Minus(0x0017, 29),
    /** Bidi Arabic 2 (AraAbjad) with ANSI minus symbol. */
    arabic2Minus(0x0018, 30),
    /** Bidi Hebrew 2 with ANSI minus symbol. */
    hebrew2Minus(0x0019, 31),
    /** Thai alphabetic character followed by a period. */
    thaiAlphaPeriod(0x001E, 32),
    /** Thai alphabetic character followed by a closing parenthesis. */
    thaiAlphaParenRight(0x001F, 33),
    /** Thai alphabetic character enclosed by parentheses. */
    thaiAlphaParenBoth(0x0020, 34),
    /** Thai numeral followed by a period. */
    thaiNumPeriod(0x0021, 35),
    /** Thai numeral followed by a closing parenthesis. */
    thaiNumParenRight(0x0022, 36),
    /** Thai numeral enclosed in parentheses. */
    thaiNumParenBoth(0x0023, 37),
    /** Hindi alphabetic character followed by a period. */
    hindiAlphaPeriod(0x0024, 38),
    /** Hindi numeric character followed by a period. */
    hindiNumPeriod(0x0025, 39),
    /** Hindi numeric character followed by a closing parenthesis. */
    hindiNumParenRight(0x0027, 40),
    /** Hindi alphabetic character followed by a period. */
    hindiAlpha1Period(0x0027, 41);

    public final int nativeId, ooxmlId;

    AutoNumberingScheme(int nativeId, int ooxmlId) {
        this.nativeId = nativeId;
        this.ooxmlId = ooxmlId;
    }

    public static AutoNumberingScheme forNativeID(int nativeId) {
        for (AutoNumberingScheme ans : values()) {
            if (ans.nativeId == nativeId) return ans;
        }
        return null;
    }

    public static AutoNumberingScheme forOoxmlID(int ooxmlId) {
        for (AutoNumberingScheme ans : values()) {
            if (ans.ooxmlId == ooxmlId) return ans;
        }
        return null;
    }

    public String getDescription() {
        switch (this) {
        case alphaLcPeriod          : return "Lowercase Latin character followed by a period. Example: a., b., c., ...";
        case alphaUcPeriod          : return "Uppercase Latin character followed by a period. Example: A., B., C., ...";
        case arabicParenRight       : return "Arabic numeral followed by a closing parenthesis. Example: 1), 2), 3), ...";
        case arabicPeriod           : return "Arabic numeral followed by a period. Example: 1., 2., 3., ...";
        case romanLcParenBoth       : return "Lowercase Roman numeral enclosed in parentheses. Example: (i), (ii), (iii), ...";
        case romanLcParenRight      : return "Lowercase Roman numeral followed by a closing parenthesis. Example: i), ii), iii), ...";
        case romanLcPeriod          : return "Lowercase Roman numeral followed by a period. Example: i., ii., iii., ...";
        case romanUcPeriod          : return "Uppercase Roman numeral followed by a period. Example: I., II., III., ...";
        case alphaLcParenBoth       : return "Lowercase alphabetic character enclosed in parentheses. Example: (a), (b), (c), ...";
        case alphaLcParenRight      : return "Lowercase alphabetic character followed by a closing parenthesis. Example: a), b), c), ...";
        case alphaUcParenBoth       : return "Uppercase alphabetic character enclosed in parentheses. Example: (A), (B), (C), ...";
        case alphaUcParenRight      : return "Uppercase alphabetic character followed by a closing parenthesis. Example: A), B), C), ...";
        case arabicParenBoth        : return "Arabic numeral enclosed in parentheses. Example: (1), (2), (3), ...";
        case arabicPlain            : return "Arabic numeral. Example: 1, 2, 3, ...";
        case romanUcParenBoth       : return "Uppercase Roman numeral enclosed in parentheses. Example: (I), (II), (III), ...";
        case romanUcParenRight      : return "Uppercase Roman numeral followed by a closing parenthesis. Example: I), II), III), ...";
        case ea1ChsPlain            : return "Simplified Chinese.";
        case ea1ChsPeriod           : return "Simplified Chinese with single-byte period.";
        case circleNumDbPlain       : return "Double byte circle numbers.";
        case circleNumWdWhitePlain  : return "Wingdings white circle numbers.";
        case circleNumWdBlackPlain  : return "Wingdings black circle numbers.";
        case ea1ChtPlain            : return "Traditional Chinese.";
        case ea1ChtPeriod           : return "Traditional Chinese with single-byte period.";
        case arabic1Minus           : return "Bidi Arabic 1 (AraAlpha) with ANSI minus symbol.";
        case arabic2Minus           : return "Bidi Arabic 2 (AraAbjad) with ANSI minus symbol.";
        case hebrew2Minus           : return "Bidi Hebrew 2 with ANSI minus symbol.";
        case ea1JpnKorPlain         : return "Japanese/Korean.";
        case ea1JpnKorPeriod        : return "Japanese/Korean with single-byte period.";
        case arabicDbPlain          : return "Double-byte Arabic numbers.";
        case arabicDbPeriod         : return "Double-byte Arabic numbers with double-byte period.";
        case thaiAlphaPeriod        : return "Thai alphabetic character followed by a period.";
        case thaiAlphaParenRight    : return "Thai alphabetic character followed by a closing parenthesis.";
        case thaiAlphaParenBoth     : return "Thai alphabetic character enclosed by parentheses.";
        case thaiNumPeriod          : return "Thai numeral followed by a period.";
        case thaiNumParenRight      : return "Thai numeral followed by a closing parenthesis.";
        case thaiNumParenBoth       : return "Thai numeral enclosed in parentheses.";
        case hindiAlphaPeriod       : return "Hindi alphabetic character followed by a period.";
        case hindiNumPeriod         : return "Hindi numeric character followed by a period.";
        case ea1JpnChsDbPeriod      : return "Japanese with double-byte period.";
        case hindiNumParenRight     : return "Hindi numeric character followed by a closing parenthesis.";
        case hindiAlpha1Period      : return "Hindi alphabetic character followed by a period.";
        default                     : return "Unknown Numbered Scheme";
        }
    }

    public String format(int value) {
        String index = formatIndex(value);
        String cased = formatCase(index);
        return formatSeperator(cased);
    }

    private String formatSeperator(String cased) {
        String name = name().toLowerCase(Locale.ROOT);
        if (name.contains("plain")) return cased;
        if (name.contains("parenright")) return cased+")";
        if (name.contains("parenboth")) return "("+cased+")";
        if (name.contains("period")) return cased+".";
        if (name.contains("minus")) return cased+"-"; // ???
        return cased;
    }

    private String formatCase(String index) {
        String name = name().toLowerCase(Locale.ROOT);
        if (name.contains("lc")) return index.toLowerCase(Locale.ROOT);
        if (name.contains("uc")) return index.toUpperCase(Locale.ROOT);
        return index;
    }

    private static final String ARABIC_LIST = "0123456789";
    private static final String ALPHA_LIST = "abcdefghijklmnopqrstuvwxyz";
    private static final String WINGDINGS_WHITE_LIST =
            "\u0080\u0081\u0082\u0083\u0084\u0085\u0086\u0087\u0088\u0089";
    private static final String WINGDINGS_BLACK_LIST =
            "\u008B\u008C\u008D\u008E\u008F\u0090\u0091\u0092\u0093\u0094";
    private static final String CIRCLE_DB_LIST =
            "\u2776\u2777\u2778\u2779\u277A\u277B\u277C\u277D\u277E";

    private String formatIndex(int value) {
        String name = name().toLowerCase(Locale.ROOT);
        if (name.startsWith("roman")) {
            return formatRomanIndex(value);
        } else if (name.startsWith("arabic") && !name.contains("db")) {
            return getIndexedList(value, ARABIC_LIST, false);
        } else if (name.startsWith("alpha")) {
            return getIndexedList(value, ALPHA_LIST, true);
        } else if (name.contains("WdWhite")) {
            return (value == 10) ? "\u008A"
                : getIndexedList(value, WINGDINGS_WHITE_LIST, false);
        } else if (name.contains("WdBlack")) {
            return (value == 10) ? "\u0095"
                : getIndexedList(value, WINGDINGS_BLACK_LIST, false);
        } else if (name.contains("NumDb")) {
            return (value == 10) ? "\u277F"
                : getIndexedList(value, CIRCLE_DB_LIST, true);
        } else {
            return "?";
        }
    }

    private static String getIndexedList(int val, String list, boolean oneBased) {
        StringBuilder sb = new StringBuilder();
        addIndexedChar(val, list, oneBased, sb);
        return sb.toString();
    }

    private static void addIndexedChar(int val, String list, boolean oneBased, StringBuilder sb) {
        if (oneBased) val -= 1;
        final int len = list.length();
        if (val >= len) {
            addIndexedChar(val/len, list, oneBased, sb);
        }
        sb.append(list.charAt(val%len));
    }


    private String formatRomanIndex(int value) {
        //M (1000), CM (900), D (500), CD (400), C (100), XC (90), L (50), XL (40), X (10), IX (9), V (5), IV (4) and I (1).
        final int[] VALUES = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        final String[] ROMAN = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        final String[][] conciseList = {
                {"XLV", "VL"}, //45
                {"XCV", "VC"}, //95
                {"CDL", "LD"}, //450
                {"CML", "LM"}, //950
                {"CMVC", "LMVL"}, //995
                {"CDXC", "LDXL"}, //490
                {"CDVC", "LDVL"}, //495
                {"CMXC", "LMXL"}, //990
                {"XCIX", "VCIV"}, //99
                {"XLIX", "VLIV"}, //49
                {"XLIX", "IL"}, //49
                {"XCIX", "IC"}, //99
                {"CDXC", "XD"}, //490
                {"CDVC", "XDV"}, //495
                {"CDIC", "XDIX"}, //499
                {"LMVL", "XMV"}, //995
                {"CMIC", "XMIX"}, //999
                {"CMXC", "XM"}, // 990
                {"XDV", "VD"},  //495
                {"XDIX", "VDIV"}, //499
                {"XMV", "VM"}, // 995
                {"XMIX", "VMIV"}, //999
                {"VDIV", "ID"}, //499
                {"VMIV", "IM"} //999
        };

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 13; i++) {
            while (value >= VALUES[i]) {
                value -= VALUES[i];
                sb.append(ROMAN[i]);
            }
        }
        String result = sb.toString();
        for (String[] cc : conciseList) {
            result = result.replace(cc[0], cc[1]);
        }
        return result;
    }
}