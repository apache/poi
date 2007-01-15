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

	public CharFlagsTextProp() {
		super(2,0xffff, "char_flags", new String[] {
				"bold",          // 0x0001
				"italic",        // 0x0002
				"underline",     // 0x0004
				"char_unknown_1",// 0x0008
				"shadow",        // 0x0010
				"char_unknown_2",// 0x0020
				"char_unknown_3",// 0x0040
				"char_unknown_4",// 0x0080
				"strikethrough", // 0x0100
				"relief",        // 0x0200
				"reset_numbering",    // 0x0400
				"enable_numbering_1", // 0x0800
				"enable_numbering_2", // 0x1000
			}
		);
	}
}