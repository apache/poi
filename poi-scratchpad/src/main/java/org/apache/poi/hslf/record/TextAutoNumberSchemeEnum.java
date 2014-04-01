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

package org.apache.poi.hslf.record;

public enum TextAutoNumberSchemeEnum {
	//Name Value Meaning
	ANM_AlphaLcPeriod 			((short) 0x0000), // "Lowercase Latin character followed by a period. Example: a., b., c., ..."),
	ANM_AlphaUcPeriod 			((short) 0x0001), // "Uppercase Latin character followed by a period. Example: A., B., C., ..."),
	ANM_ArabicParenRight		((short) 0x0002), // "Arabic numeral followed by a closing parenthesis. Example: 1), 2), 3), ..."),
	ANM_ArabicPeriod			((short) 0x0003), // "Arabic numeral followed by a period. Example: 1., 2., 3., ..."),
	ANM_RomanLcParenBoth		((short) 0x0004), // "Lowercase Roman numeral enclosed in parentheses. Example: (i), (ii), (iii), ..."),            
	ANM_RomanLcParenRight		((short) 0x0005), // "Lowercase Roman numeral followed by a closing parenthesis. Example: i), ii), iii), ..."),     
	ANM_RomanLcPeriod			((short) 0x0006), // "Lowercase Roman numeral followed by a period. Example: i., ii., iii., ..."), 
	ANM_RomanUcPeriod			((short) 0x0007), // "Uppercase Roman numeral followed by a period. Example: I., II., III., ..."),
	ANM_AlphaLcParenBoth		((short) 0x0008), // "Lowercase alphabetic character enclosed in parentheses. Example: (a), (b), (c), ..."),
	ANM_AlphaLcParenRight		((short) 0x0009), // "Lowercase alphabetic character followed by a closing parenthesis. Example: a), b), c), ..."),
	ANM_AlphaUcParenBoth		((short) 0x000A), // "Uppercase alphabetic character enclosed in parentheses. Example: (A), (B), (C), ..."),
	ANM_AlphaUcParenRight		((short) 0x000B), // "Uppercase alphabetic character followed by a closing parenthesis. Example: A), B), C), ..."), 
	ANM_ArabicParenBoth			((short) 0x000C), // "Arabic numeral enclosed in parentheses. Example: (1), (2), (3), ..."),
	ANM_ArabicPlain				((short) 0x000D), // "Arabic numeral. Example: 1, 2, 3, ..."),
	ANM_RomanUcParenBoth		((short) 0x000E), // "Uppercase Roman numeral enclosed in parentheses. Example: (I), (II), (III), ..."),
	ANM_RomanUcParenRight		((short) 0x000F), // "Uppercase Roman numeral followed by a closing parenthesis. Example: I), II), III), ..."),
	ANM_ChsPlain				((short) 0x0010), // "Simplified Chinese."),
	ANM_ChsPeriod				((short) 0x0011), // "Simplified Chinese with single-byte period."),
	ANM_CircleNumDBPlain		((short) 0x0012), // "Double byte circle numbers."),
	ANM_CircleNumWDBWhitePlain	((short) 0x0013), // "Wingdings white circle numbers."),
	ANM_CircleNumWDBBlackPlain	((short) 0x0014), // "Wingdings black circle numbers."),
	ANM_ChtPlain				((short) 0x0015), // "Traditional Chinese."),
	ANM_ChtPeriod				((short) 0x0016), // "Traditional Chinese with single-byte period."),
	ANM_Arabic1Minus			((short) 0x0017), // "Bidi Arabic 1 (AraAlpha) with ANSI minus symbol."),
	ANM_Arabic2Minus			((short) 0x0018), // "Bidi Arabic 2 (AraAbjad) with ANSI minus symbol."),
	ANM_Hebrew2Minus			((short) 0x0019), // "Bidi Hebrew 2 with ANSI minus symbol."),
	ANM_JpnKorPlain				((short) 0x001A), // "Japanese/Korean."),
	ANM_JpnKorPeriod			((short) 0x001B), // "Japanese/Korean with single-byte period."),
	ANM_ArabicDbPlain			((short) 0x001C), // "Double-byte Arabic numbers."),
	ANM_ArabicDbPeriod			((short) 0x001D), // "Double-byte Arabic numbers with double-byte period."),
	ANM_ThaiAlphaPeriod			((short) 0x001E), // "Thai alphabetic character followed by a period."),
	ANM_ThaiAlphaParenRight		((short) 0x001F), // "Thai alphabetic character followed by a closing parenthesis."),
	ANM_ThaiAlphaParenBoth		((short) 0x0020), // "Thai alphabetic character enclosed by parentheses."),
	ANM_ThaiNumPeriod			((short) 0x0021), // "Thai numeral followed by a period."),
	ANM_ThaiNumParenRight		((short) 0x0022), // "Thai numeral followed by a closing parenthesis."),
	ANM_ThaiNumParenBoth		((short) 0x0023), // "Thai numeral enclosed in parentheses."),
	ANM_HindiAlphaPeriod		((short) 0x0024), // "Hindi alphabetic character followed by a period."),
	ANM_HindiNumPeriod			((short) 0x0025), // "Hindi numeric character followed by a period."),
	ANM_JpnChsDBPeriod			((short) 0x0026), // "Japanese with double-byte period."),
	ANM_HindiNumParenRight		((short) 0x0027), // "Hindi numeric character followed by a closing parenthesis."),
	ANM_HindiAlpha1Period		((short) 0x0028); // "Hindi alphabetic character followed by a period.");

	private final short value;
	private TextAutoNumberSchemeEnum(final short code) {
		this.value = code;
	}
	private short getValue() { return value; }
	public String getDescription() {
		return TextAutoNumberSchemeEnum.getDescription(this);
	}
	public static String getDescription(final TextAutoNumberSchemeEnum code) {
		switch (code) {
		case ANM_AlphaLcPeriod			: return "Lowercase Latin character followed by a period. Example: a., b., c., ...";
		case ANM_AlphaUcPeriod 			: return "Uppercase Latin character followed by a period. Example: A., B., C., ...";
		case ANM_ArabicParenRight		: return "Arabic numeral followed by a closing parenthesis. Example: 1), 2), 3), ...";
		case ANM_ArabicPeriod			: return "Arabic numeral followed by a period. Example: 1., 2., 3., ...";
		case ANM_RomanLcParenBoth		: return "Lowercase Roman numeral enclosed in parentheses. Example: (i), (ii), (iii), ...";            
		case ANM_RomanLcParenRight		: return "Lowercase Roman numeral followed by a closing parenthesis. Example: i), ii), iii), ...";     
		case ANM_RomanLcPeriod			: return "Lowercase Roman numeral followed by a period. Example: i., ii., iii., ...";
		case ANM_RomanUcPeriod			: return "Uppercase Roman numeral followed by a period. Example: I., II., III., ...";
		case ANM_AlphaLcParenBoth		: return "Lowercase alphabetic character enclosed in parentheses. Example: (a), (b), (c), ...";
		case ANM_AlphaLcParenRight		: return "Lowercase alphabetic character followed by a closing parenthesis. Example: a), b), c), ...";
		case ANM_AlphaUcParenBoth		: return "Uppercase alphabetic character enclosed in parentheses. Example: (A), (B), (C), ...";
		case ANM_AlphaUcParenRight		: return "Uppercase alphabetic character followed by a closing parenthesis. Example: A), B), C), ..."; 
		case ANM_ArabicParenBoth		: return "Arabic numeral enclosed in parentheses. Example: (1), (2), (3), ...";
		case ANM_ArabicPlain			: return "Arabic numeral. Example: 1, 2, 3, ...";
		case ANM_RomanUcParenBoth		: return "Uppercase Roman numeral enclosed in parentheses. Example: (I), (II), (III), ...";
		case ANM_RomanUcParenRight		: return "Uppercase Roman numeral followed by a closing parenthesis. Example: I), II), III), ...";
		case ANM_ChsPlain				: return "Simplified Chinese.";
		case ANM_ChsPeriod				: return "Simplified Chinese with single-byte period.";
		case ANM_CircleNumDBPlain		: return "Double byte circle numbers.";
		case ANM_CircleNumWDBWhitePlain	: return "Wingdings white circle numbers.";
		case ANM_CircleNumWDBBlackPlain	: return "Wingdings black circle numbers.";
		case ANM_ChtPlain				: return "Traditional Chinese.";
		case ANM_ChtPeriod				: return "Traditional Chinese with single-byte period.";
		case ANM_Arabic1Minus			: return "Bidi Arabic 1 (AraAlpha) with ANSI minus symbol.";
		case ANM_Arabic2Minus			: return "Bidi Arabic 2 (AraAbjad) with ANSI minus symbol.";
		case ANM_Hebrew2Minus			: return "Bidi Hebrew 2 with ANSI minus symbol.";
		case ANM_JpnKorPlain			: return "Japanese/Korean.";
		case ANM_JpnKorPeriod			: return "Japanese/Korean with single-byte period.";
		case ANM_ArabicDbPlain			: return "Double-byte Arabic numbers.";
		case ANM_ArabicDbPeriod			: return "Double-byte Arabic numbers with double-byte period.";
		case ANM_ThaiAlphaPeriod		: return "Thai alphabetic character followed by a period.";
		case ANM_ThaiAlphaParenRight	: return "Thai alphabetic character followed by a closing parenthesis.";
		case ANM_ThaiAlphaParenBoth		: return "Thai alphabetic character enclosed by parentheses.";
		case ANM_ThaiNumPeriod			: return "Thai numeral followed by a period.";
		case ANM_ThaiNumParenRight		: return "Thai numeral followed by a closing parenthesis.";
		case ANM_ThaiNumParenBoth		: return "Thai numeral enclosed in parentheses.";
		case ANM_HindiAlphaPeriod		: return "Hindi alphabetic character followed by a period.";
		case ANM_HindiNumPeriod			: return "Hindi numeric character followed by a period.";
		case ANM_JpnChsDBPeriod			: return "Japanese with double-byte period.";
		case ANM_HindiNumParenRight		: return "Hindi numeric character followed by a closing parenthesis.";
		case ANM_HindiAlpha1Period		: return "Hindi alphabetic character followed by a period.";
		default							: return "Unknown Numbered Scheme";
		}
	}
	public static TextAutoNumberSchemeEnum valueOf(short autoNumberScheme) {
		for (TextAutoNumberSchemeEnum item: TextAutoNumberSchemeEnum.values()) {
			if (autoNumberScheme == item.getValue()) {
				return item;
			}
		}
		return null;
	}
}
