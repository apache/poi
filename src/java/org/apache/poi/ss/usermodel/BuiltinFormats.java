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
package org.apache.poi.ss.usermodel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility to identify built-in formats.  The following is a list of the formats as
 * returned by this class.<p/>
 *<p/>
 *       0, "General"<br/>
 *       1, "0"<br/>
 *       2, "0.00"<br/>
 *       3, "#,##0"<br/>
 *       4, "#,##0.00"<br/>
 *       5, "$#,##0_);($#,##0)"<br/>
 *       6, "$#,##0_);[Red]($#,##0)"<br/>
 *       7, "$#,##0.00);($#,##0.00)"<br/>
 *       8, "$#,##0.00_);[Red]($#,##0.00)"<br/>
 *       9, "0%"<br/>
 *       0xa, "0.00%"<br/>
 *       0xb, "0.00E+00"<br/>
 *       0xc, "# ?/?"<br/>
 *       0xd, "# ??/??"<br/>
 *       0xe, "m/d/yy"<br/>
 *       0xf, "d-mmm-yy"<br/>
 *       0x10, "d-mmm"<br/>
 *       0x11, "mmm-yy"<br/>
 *       0x12, "h:mm AM/PM"<br/>
 *       0x13, "h:mm:ss AM/PM"<br/>
 *       0x14, "h:mm"<br/>
 *       0x15, "h:mm:ss"<br/>
 *       0x16, "m/d/yy h:mm"<br/>
 *<p/>
 *       // 0x17 - 0x24 reserved for international and undocumented
 *       0x25, "#,##0_);(#,##0)"<br/>
 *       0x26, "#,##0_);[Red](#,##0)"<br/>
 *       0x27, "#,##0.00_);(#,##0.00)"<br/>
 *       0x28, "#,##0.00_);[Red](#,##0.00)"<br/>
 *       0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)"<br/>
 *       0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)"<br/>
 *       0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)"<br/>
 *       0x2c, "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)"<br/>
 *       0x2d, "mm:ss"<br/>
 *       0x2e, "[h]:mm:ss"<br/>
 *       0x2f, "mm:ss.0"<br/>
 *       0x30, "##0.0E+0"<br/>
 *       0x31, "@" - This is text format.<br/>
 *       0x31  "text" - Alias for "@"<br/>
 * <p/>
 *
 * @author Yegor Kozlov
 *
 * Modified 6/17/09 by Stanislav Shor - positive formats don't need starting '('
 *
 */
public final class BuiltinFormats {
	/**
	 * The first user-defined format starts at 164.
	 */
	public static final int FIRST_USER_DEFINED_FORMAT_INDEX = 164;

	private final static String[] _formats;

	static {
		List<String> m = new ArrayList<String>();
		putFormat(m, 0, "General");
		putFormat(m, 1, "0");
		putFormat(m, 2, "0.00");
		putFormat(m, 3, "#,##0");
		putFormat(m, 4, "#,##0.00");
		putFormat(m, 5, "$#,##0_);($#,##0)");
		putFormat(m, 6, "$#,##0_);[Red]($#,##0)");
		putFormat(m, 7, "$#,##0.00_);($#,##0.00)");
		putFormat(m, 8, "$#,##0.00_);[Red]($#,##0.00)");
		putFormat(m, 9, "0%");
		putFormat(m, 0xa, "0.00%");
		putFormat(m, 0xb, "0.00E+00");
		putFormat(m, 0xc, "# ?/?");
		putFormat(m, 0xd, "# ??/??");
		putFormat(m, 0xe, "m/d/yy");
		putFormat(m, 0xf, "d-mmm-yy");
		putFormat(m, 0x10, "d-mmm");
		putFormat(m, 0x11, "mmm-yy");
		putFormat(m, 0x12, "h:mm AM/PM");
		putFormat(m, 0x13, "h:mm:ss AM/PM");
		putFormat(m, 0x14, "h:mm");
		putFormat(m, 0x15, "h:mm:ss");
		putFormat(m, 0x16, "m/d/yy h:mm");

		// 0x17 - 0x24 reserved for international and undocumented
		for (int i=0x17; i<=0x24; i++) {
			// TODO - one junit relies on these values which seems incorrect
			putFormat(m, i, "reserved-0x" + Integer.toHexString(i));
		}

		putFormat(m, 0x25, "#,##0_);(#,##0)");
		putFormat(m, 0x26, "#,##0_);[Red](#,##0)");
		putFormat(m, 0x27, "#,##0.00_);(#,##0.00)");
		putFormat(m, 0x28, "#,##0.00_);[Red](#,##0.00)");
		putFormat(m, 0x29, "_(*#,##0_);_(*(#,##0);_(* \"-\"_);_(@_)");
		putFormat(m, 0x2a, "_($*#,##0_);_($*(#,##0);_($* \"-\"_);_(@_)");
		putFormat(m, 0x2b, "_(*#,##0.00_);_(*(#,##0.00);_(*\"-\"??_);_(@_)");
		putFormat(m, 0x2c, "_($*#,##0.00_);_($*(#,##0.00);_($*\"-\"??_);_(@_)");
		putFormat(m, 0x2d, "mm:ss");
		putFormat(m, 0x2e, "[h]:mm:ss");
		putFormat(m, 0x2f, "mm:ss.0");
		putFormat(m, 0x30, "##0.0E+0");
		putFormat(m, 0x31, "@");
		String[] ss = new String[m.size()];
		m.toArray(ss);
		_formats = ss;
	}
	private static void putFormat(List<String> m, int index, String value) {
		if (m.size() != index) {
			throw new IllegalStateException("index " + index  + " is wrong");
		}
		m.add(value);
	}


	/**
	 * @deprecated (May 2009) use {@link #getAll()}
	 */
	public static Map<Integer, String> getBuiltinFormats() {
		Map<Integer, String> result = new LinkedHashMap<Integer, String>();
		for (int i=0; i<_formats.length; i++) {
			result.put(Integer.valueOf(i), _formats[i]);
		}
		return result;
	}

	/**
	 * @return array of built-in data formats
	 */
	public static String[] getAll() {
		return _formats.clone();
	}

	/**
	 * Get the format string that matches the given format index
	 *
	 * @param index of a built in format
	 * @return string represented at index of format or <code>null</code> if there is not a built-in format at that index
	 */
	public static String getBuiltinFormat(int index) {
		if (index < 0 || index >=_formats.length) {
			return null;
		}
		return _formats[index];
	}

	/**
	 * Get the format index that matches the given format string
	 * <p/>
	 * Automatically converts "text" to excel's format string to represent text.
	 * </p>
	 * @param fmt string matching a built-in format
	 * @return index of format or -1 if undefined.
	 */
	public static int getBuiltinFormat(String pFmt) {
		String fmt;
		if (pFmt.equalsIgnoreCase("TEXT")) {
			fmt = "@";
		} else {
			fmt = pFmt;
		}

		for(int i =0; i< _formats.length; i++) {
			if(fmt.equals(_formats[i])) {
				return i;
			}
		}
		return -1;
	}
}
