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
 * Definition for the common paragraph text property bitset.
 *
 * @author Yegor Kozlov
 */
public final class ParagraphFlagsTextProp extends BitMaskTextProp {
	public static final int BULLET_IDX = 0;
	public static final int BULLET_HARDFONT_IDX = 1;
	public static final int BULLET_HARDCOLOR_IDX = 2;
	public static final int BULLET_HARDSIZE_IDX = 4;

    public static final String NAME = "paragraph_flags";

	public ParagraphFlagsTextProp() {
		super(2,  0xF, NAME,
			"bullet",
            "bullet.hardfont",
			"bullet.hardcolor",
            "bullet.hardsize"
		);
	}

	public ParagraphFlagsTextProp(ParagraphFlagsTextProp other) {
		super(other);
	}

	@Override
	public ParagraphFlagsTextProp copy() {
		return new ParagraphFlagsTextProp(this);
	}
}
