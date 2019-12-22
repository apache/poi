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

import org.apache.poi.sl.usermodel.TextParagraph.TextAlign;
import org.apache.poi.util.GenericRecordUtil;

/**
 * Definition for the text alignment property.
 */
public class TextAlignmentProp extends TextProp {
	/**
	 * For horizontal text, left aligned.
	 * For vertical text, top aligned.
	 */
    public static final int LEFT = 0;

    /**
     * For horizontal text, centered.
     * For vertical text, middle aligned.
     */
	public static final int CENTER = 1;

	/**
	 * For horizontal text, right aligned.
	 * For vertical text, bottom aligned.
	 */
	public static final int RIGHT = 2;

	/**
	 * For horizontal text, flush left and right.
	 * For vertical text, flush top and bottom.
	 */
	public static final int JUSTIFY = 3;

	/**
	 * Distribute space between characters.
	 */
	public static final int DISTRIBUTED = 4;

	/**
	 * Thai distribution justification.
	 */
	public static final int THAIDISTRIBUTED = 5;

	/**
	 * Kashida justify low.
	 */
	public static final int JUSTIFYLOW = 6;

	public TextAlignmentProp() {
		super(2, 0x800, "alignment");
	}


	public TextAlignmentProp(TextAlignmentProp other) {
		super(other);
	}

	public TextAlign getTextAlign() {
		switch (getValue()) {
			default:
			case LEFT:
				return TextAlign.LEFT;
			case CENTER:
				return TextAlign.CENTER;
			case RIGHT:
				return TextAlign.RIGHT;
			case JUSTIFY:
				return TextAlign.JUSTIFY;
			case DISTRIBUTED:
				return TextAlign.DIST;
			case THAIDISTRIBUTED:
				return TextAlign.THAI_DIST;
		}
	}

	@Override
	public Map<String, Supplier<?>> getGenericProperties() {
		return GenericRecordUtil.getGenericProperties(
			"base", super::getGenericProperties,
			"textAlign", this::getTextAlign
		);
	}

	@Override
	public TextAlignmentProp copy() {
		return new TextAlignmentProp(this);
	}
}