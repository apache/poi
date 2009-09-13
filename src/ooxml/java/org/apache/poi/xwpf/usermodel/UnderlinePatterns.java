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
 * Specifies the types of patterns which may be used to create the underline
 * applied beneath the text in a run.
 * 
 * @author Gisella Bronzetti
 */
public enum UnderlinePatterns {

    /**
     * Specifies an underline consisting of a single line beneath all characters
     * in this run.
     */
    SINGLE(1),

    /**
     * Specifies an underline consisting of a single line beneath all non-space
     * characters in the run. There shall be no underline beneath any space
     * character (breaking or non-breaking).
     */
    WORDS(2),

    /**
     * Specifies an underline consisting of two lines beneath all characters in
     * this run
     */
    DOUBLE(3),

    /**
     * Specifies an underline consisting of a single thick line beneath all
     * characters in this run.
     */
    THICK(4),

    /**
     * Specifies an underline consisting of a series of dot characters beneath
     * all characters in this run.
     */
    DOTTED(5),

    /**
     * Specifies an underline consisting of a series of thick dot characters
     * beneath all characters in this run.
     */
    DOTTED_HEAVY(6),

    /**
     * Specifies an underline consisting of a dashed line beneath all characters
     * in this run.
     */
    DASH(7),

    /**
     * Specifies an underline consisting of a series of thick dashes beneath all
     * characters in this run.
     */
    DASHED_HEAVY(8),

    /**
     * Specifies an underline consisting of long dashed characters beneath all
     * characters in this run.
     */
    DASH_LONG(9),

    /**
     * Specifies an underline consisting of thick long dashed characters beneath
     * all characters in this run.
     */
    DASH_LONG_HEAVY(10),

    /**
     * Specifies an underline consisting of a series of dash, dot characters
     * beneath all characters in this run.
     */
    DOT_DASH(11),

    /**
     * Specifies an underline consisting of a series of thick dash, dot
     * characters beneath all characters in this run.
     */
    DASH_DOT_HEAVY(12),

    /**
     * Specifies an underline consisting of a series of dash, dot, dot
     * characters beneath all characters in this run.
     */
    DOT_DOT_DASH(13),

    /**
     * Specifies an underline consisting of a series of thick dash, dot, dot
     * characters beneath all characters in this run.
     */
    DASH_DOT_DOT_HEAVY(14),

    /**
     * Specifies an underline consisting of a single wavy line beneath all
     * characters in this run.
     */
    WAVE(15),

    /**
     * Specifies an underline consisting of a single thick wavy line beneath all
     * characters in this run.
     */
    WAVY_HEAVY(16),

    /**
     * Specifies an underline consisting of a pair of wavy lines beneath all
     * characters in this run.
     */
    WAVY_DOUBLE(17),

    /**
     * Specifies no underline beneath this run.
     */
    NONE(18);

    private final int value;

    private UnderlinePatterns(int val) {
	value = val;
    }

    public int getValue() {
	return value;
    }

    private static Map<Integer, UnderlinePatterns> imap = new HashMap<Integer, UnderlinePatterns>();
    static {
	for (UnderlinePatterns p : values()) {
	    imap.put(p.getValue(), p);
	}
    }

    public static UnderlinePatterns valueOf(int type) {
	UnderlinePatterns align = imap.get(type);
	if (align == null)
	    throw new IllegalArgumentException("Unknown underline pattern: "
		    + type);
	return align;
    }

}
