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
 * Specifies possible values for the alignment of the contents of this run in
 * relation to the default appearance of the run's text. This allows the text to
 * be repositioned as subscript or superscript without altering the font size of
 * the run properties.
 * 
 * @author Gisella Bronzetti
 */
public enum VerticalAlign {

    /**
     * Specifies that the text in the parent run shall be located at the
     * baseline and presented in the same size as surrounding text.
     */
    BASELINE(1),
    /**
     * Specifies that this text should be subscript. This setting shall lower
     * the text in this run below the baseline and change it to a smaller size,
     * if a smaller size is available.
     */
    SUPERSCRIPT(2),
    /**
     * Specifies that this text should be superscript. This setting shall raise
     * the text in this run above the baseline and change it to a smaller size,
     * if a smaller size is available.
     */
    SUBSCRIPT(3);

    private final int value;

    private VerticalAlign(int val) {
	value = val;
    }

    public int getValue() {
	return value;
    }

    private static Map<Integer, VerticalAlign> imap = new HashMap<Integer, VerticalAlign>();
    static {
	for (VerticalAlign p : values()) {
	    imap.put(p.getValue(), p);
	}
    }

    public static VerticalAlign valueOf(int type) {
	VerticalAlign align = imap.get(type);
	if (align == null)
	    throw new IllegalArgumentException("Unknown vertical alignment: "
		    + type);
	return align;
    }

}
