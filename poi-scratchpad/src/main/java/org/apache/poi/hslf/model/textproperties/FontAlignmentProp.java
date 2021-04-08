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

import java.util.Map;
import java.util.function.Supplier;

import org.apache.poi.sl.usermodel.TextParagraph.FontAlign;
import org.apache.poi.util.GenericRecordUtil;

/**
 * Definition for the font alignment property.
 */
public class FontAlignmentProp extends TextProp {
	public static final String NAME = "fontAlign";
	public static final int BASELINE = 0;
	public static final int TOP = 1;
	public static final int CENTER = 2;
	public static final int BOTTOM = 3;

	public FontAlignmentProp() {
		super(2, 0x10000, NAME);
	}

	public FontAlignmentProp(FontAlignmentProp other) {
		super(other);
	}

	public FontAlign getFontAlign() {
		switch (getValue()) {
			default:
				return FontAlign.AUTO;
			case BASELINE:
				return FontAlign.BASELINE;
			case TOP:
				return FontAlign.TOP;
			case CENTER:
				return FontAlign.CENTER;
			case BOTTOM:
				return FontAlign.BOTTOM;
		}
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"base", super::getGenericProperties,
			"fontAlign", this::getFontAlign
		);
	}

	@Override
	public FontAlignmentProp copy() {
		return new FontAlignmentProp(this);
	}
}