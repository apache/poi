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


/*
 * HSSFDataFormat.java
 *
 * Created on December 18, 2001, 12:42 PM
 */
package org.apache.poi.hssf.usermodel;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormat;

/**
 * Identifies both built-in and user defined formats within a workbook.<p/>
 * See {@link BuiltinFormats} for a list of supported built-in formats.<p/>
 *
 * <b>International Formats</b><br/>
 * Since version 2003 Excel has supported international formats.  These are denoted
 * with a prefix "[$-xxx]" (where xxx is a 1-7 digit hexadecimal number).
 * See the Microsoft article
 * <a href="http://office.microsoft.com/assistance/hfws.aspx?AssetID=HA010346351033&CTT=6&Origin=EC010272491033">
 *   Creating international number formats
 * </a> for more details on these codes.
 *
 * @author  Andrew C. Oliver (acoliver at apache dot org)
 * @author  Shawn M. Laubach (slaubach at apache dot org)
 */
public final class HSSFDataFormat implements DataFormat {
	private static final String[] _builtinFormats = BuiltinFormats.getAll();

	private final Vector<String> _formats = new Vector<String>();
	private final InternalWorkbook _workbook;
	private boolean _movedBuiltins = false;  // Flag to see if need to
	// check the built in list
	// or if the regular list
	// has all entries.

	/**
	 * Constructs a new data formatter.  It takes a workbook to have
	 * access to the workbooks format records.
	 * @param workbook the workbook the formats are tied to.
	 */
	HSSFDataFormat(InternalWorkbook workbook) {
		_workbook = workbook;

		@SuppressWarnings("unchecked")
		Iterator<FormatRecord> i = workbook.getFormats().iterator();
		while (i.hasNext()) {
			FormatRecord r = i.next();
			if (_formats.size() < r.getIndexCode() + 1) {
				_formats.setSize(r.getIndexCode() + 1);
			}
			_formats.set(r.getIndexCode(), r.getFormatString());
		}
	}

	public static List<String> getBuiltinFormats() {
		return Arrays.asList(_builtinFormats);
	}

	/**
	 * get the format index that matches the given format string<p>
	 * Automatically converts "text" to excel's format string to represent text.
	 * @param format string matching a built in format
	 * @return index of format or -1 if undefined.
	 */
	public static short getBuiltinFormat(String format) {
		return (short) BuiltinFormats.getBuiltinFormat(format);
	}

	/**
	 * Get the format index that matches the given format
	 *  string, creating a new format entry if required.
	 * Aliases text to the proper format as required.
	 * @param pFormat string matching a built in format
	 * @return index of format.
	 */
	public short getFormat(String pFormat) {

		String format;
		if (pFormat.toUpperCase().equals("TEXT")) {
			format = "@";
		} else {
			format = pFormat;
		}

		if (!_movedBuiltins) {
			for (int i=0; i<_builtinFormats.length; i++) {
				if (_formats.size() < i + 1) {
					_formats.setSize(i + 1);
				}
				_formats.set(i, _builtinFormats[i]);
			}
			_movedBuiltins = true;
		}
		ListIterator<String> i;
		i = _formats.listIterator();
		int ind;
		while (i.hasNext()) {
			ind = i.nextIndex();
			if (format.equals(i.next())) {
				return (short) ind;
			}
		}

		ind = _workbook.getFormat(format, true);
		if (_formats.size() <= ind)
			_formats.setSize(ind + 1);
		_formats.set(ind, format);

		return (short) ind;
	}

	/**
	 * get the format string that matches the given format index
	 * @param index of a format
	 * @return string represented at index of format or null if there is not a  format at that index
	 */
	public String getFormat(short index) {
		if (_movedBuiltins) {
			return _formats.get(index);
		}
		if (_builtinFormats.length > index && _builtinFormats[index] != null) {
			return _builtinFormats[index];
		}
		return _formats.get(index);
	}

	/**
	 * get the format string that matches the given format index
	 * @param index of a built in format
	 * @return string represented at index of format or null if there is not a builtin format at that index
	 */
	public static String getBuiltinFormat(short index) {
		return BuiltinFormats.getBuiltinFormat(index);
	}

	/**
	 * get the number of built-in and reserved builtinFormats
	 * @return number of built-in and reserved builtinFormats
	 */
	public static int getNumberOfBuiltinBuiltinFormats() {
		return _builtinFormats.length;
	}
}
