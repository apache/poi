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

import java.awt.Color;

/**
 * Some text.
 */
public interface TextRun {
    enum TextCap {
        NONE,
        SMALL,
        ALL
    }
    
    String getRawText();
	void setText(String text);

	TextCap getTextCap();
	
	Color getFontColor();
	void setFontColor(Color color);
	
	
    /**
     * @return font size in points or null if font size is not set.
     */
	Double getFontSize();

    /**
     * @param fontSize font size in points, if null the underlying fontsize will be unset
     */
	void setFontSize(Double fontSize);
	String getFontFamily();
	
	boolean isBold();
	boolean isItalic();
	boolean isUnderlined();
	boolean isStrikethrough();
	boolean isSubscript();
	boolean isSuperscript();
	
	/**
	 * @return the pitch and family id or -1 if not applicable
	 */
	byte getPitchAndFamily();
}
