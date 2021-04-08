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

package org.apache.poi.hslf.model.textproperties;

/**
 * Definition for the common character text property bitset, which
 *  handles bold/italic/underline etc.
 */
public class CharFlagsTextProp extends BitMaskTextProp {
	public static final int BOLD_IDX = 0;
	public static final int ITALIC_IDX = 1;
	public static final int UNDERLINE_IDX = 2;
	public static final int SHADOW_IDX = 4;
	public static final int STRIKETHROUGH_IDX = 8;
	public static final int RELIEF_IDX = 9;
	public static final int RESET_NUMBERING_IDX = 10;
	public static final int ENABLE_NUMBERING_1_IDX = 11;
	public static final int ENABLE_NUMBERING_2_IDX = 12;

    public static final String NAME = "char_flags";
	public CharFlagsTextProp() {
		super(2, 0xffff, NAME,
			"bold",                 // 0x0001  A bit that specifies whether the characters are bold.
			"italic",               // 0x0002  A bit that specifies whether the characters are italicized.
			"underline",            // 0x0004  A bit that specifies whether the characters are underlined.
			"unused1",              // 0x0008  Undefined and MUST be ignored.
			"shadow",               // 0x0010  A bit that specifies whether the characters have a shadow effect.
			"fehint",               // 0x0020  A bit that specifies whether characters originated from double-byte input.
			"unused2",              // 0x0040  Undefined and MUST be ignored.
			"kumi",                 // 0x0080  A bit that specifies whether Kumimoji are used for vertical text.
			"strikethrough",        // 0x0100  aka "unused3" - sometimes contains the strikethrough flag
			"emboss",               // 0x0200  A bit that specifies whether the characters are embossed.
            "pp9rt_1",              // 0x0400  An unsigned integer that specifies the run grouping of additional text properties in StyleTextProp9Atom record.
            "pp9rt_2",              // 0x0800
            "pp9rt_3",              // 0x1000
            "pp9rt_4",              // 0x2000
            "unused4_1",            // 0x4000  Undefined and MUST be ignored.
            "unused4_2"             // 0x8000  Undefined and MUST be ignored.
		);
	}

	public CharFlagsTextProp(CharFlagsTextProp other) {
		super(other);
	}

	@Override
	public CharFlagsTextProp copy() {
		return new CharFlagsTextProp(this);
	}
}