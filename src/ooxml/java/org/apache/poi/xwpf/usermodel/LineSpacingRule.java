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
package org.apache.poi.xwpf.usermodel;

import java.util.HashMap;
import java.util.Map;

/**
 * Specifies the logic which shall be used to calculate the line spacing of the
 * parent object when it is displayed in the document.
 * 
 * @author Gisella Bronzetti
 */
public enum LineSpacingRule {

    /**
     * Specifies that the line spacing of the parent object shall be
     * automatically determined by the size of its contents, with no
     * predetermined minimum or maximum size.
     */

    AUTO(1),

    /**
     * Specifies that the height of the line shall be exactly the value
     * specified, regardless of the size of the contents If the contents are too
     * large for the specified height, then they shall be clipped as necessary.
     */
    EXACT(2),

    /**
     * Specifies that the height of the line shall be at least the value
     * specified, but may be expanded to fit its content as needed.
     */
    AT_LEAST(3);

    
    private final int value;

    private LineSpacingRule(int val) {
	value = val;
    }

    public int getValue() {
	return value;
    }

    private static Map<Integer, LineSpacingRule> imap = new HashMap<Integer, LineSpacingRule>();
    static {
	for (LineSpacingRule p : values()) {
	    imap.put(p.getValue(), p);
	}
    }

    public static LineSpacingRule valueOf(int type) {
	LineSpacingRule lineType = imap.get(type);
	if (lineType == null)
	    throw new IllegalArgumentException("Unknown line type: " + type);
	return lineType;
    }

}
