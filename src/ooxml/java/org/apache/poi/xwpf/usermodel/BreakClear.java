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
 * Specifies the set of possible restart locations which may be used as to
 * determine the next available line when a break's type attribute has a value
 * of textWrapping.
 * 
 * @author Gisella Bronzetti
 */
public enum BreakClear {

    /**
     * Specifies that the text wrapping break shall advance the text to the next
     * line in the WordprocessingML document, regardless of its position left to
     * right or the presence of any floating objects which intersect with the
     * line,
     * 
     * This is the setting for a typical line break in a document.
     */

    NONE(1),

    /**
     * Specifies that the text wrapping break shall behave as follows:
     * <ul>
     * <li> If this line is broken into multiple regions (a floating object in
     * the center of the page has text wrapping on both sides:
     * <ul>
     * <li> If this is the leftmost region of text flow on this line, advance
     * the text to the next position on the line </li>
     * <li>Otherwise, treat this as a text wrapping break of type all. </li>
     * </ul>
     * </li>
     * <li> If this line is not broken into multiple regions, then treat this
     * break as a text wrapping break of type none. </li>
     * </ul>
     * <li> If the parent paragraph is right to left, then these behaviors are
     * also reversed. </li>
     */
    LEFT(2),

    /**
     * Specifies that the text wrapping break shall behave as follows:
     * <ul>
     * <li> If this line is broken into multiple regions (a floating object in
     * the center of the page has text wrapping on both sides:
     * <ul>
     * <li> If this is the rightmost region of text flow on this line, advance
     * the text to the next position on the next line </li>
     * <li> Otherwise, treat this as a text wrapping break of type all. </li>
     * </ul>
     * <li> If this line is not broken into multiple regions, then treat this
     * break as a text wrapping break of type none. </li>
     * <li> If the parent paragraph is right to left, then these beha viors are
     * also reversed. </li>
     * </ul>
     */
    RIGHT(3),

    /**
     * Specifies that the text wrapping break shall advance the text to the next
     * line in the WordprocessingML document which spans the full width of the
     * line.
     */
    ALL(4);

    private final int value;

    private BreakClear(int val) {
	value = val;
    }

    public int getValue() {
	return value;
    }

    private static Map<Integer, BreakClear> imap = new HashMap<Integer, BreakClear>();
    static {
	for (BreakClear p : values()) {
	    imap.put(p.getValue(), p);
	}
    }

    public static BreakClear valueOf(int type) {
	BreakClear bType = imap.get(type);
	if (bType == null)
	    throw new IllegalArgumentException("Unknown break clear type: "
		    + type);
	return bType;
    }

}
