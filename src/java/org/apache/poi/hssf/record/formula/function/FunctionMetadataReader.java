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

package org.apache.poi.hssf.record.formula.function;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.poi.hssf.record.formula.Ptg;

/**
 * Converts the text meta-data file into a <tt>FunctionMetadataRegistry</tt>
 * 
 * @author Josh Micich
 */
final class FunctionMetadataReader {

	private static final String METADATA_FILE_NAME = "functionMetadata.txt";
	
	/** plain ASCII text metadata file uses three dots for ellipsis */
	private static final String ELLIPSIS = "...";

	private static final Pattern TAB_DELIM_PATTERN = Pattern.compile("\t");
	private static final Pattern SPACE_DELIM_PATTERN = Pattern.compile(" ");
	private static final byte[] EMPTY_BYTE_ARRAY = { };

	private static final String[] DIGIT_ENDING_FUNCTION_NAMES = {
		// Digits at the end of a function might be due to a left-over footnote marker.
		// except in these cases
		"LOG10", "ATAN2", "DAYS360", "SUMXMY2", "SUMX2MY2", "SUMX2PY2",
	};
	private static final Set DIGIT_ENDING_FUNCTION_NAMES_SET = new HashSet(Arrays.asList(DIGIT_ENDING_FUNCTION_NAMES));

	public static FunctionMetadataRegistry createRegistry() {
		InputStream is = FunctionMetadataReader.class.getResourceAsStream(METADATA_FILE_NAME);
		if (is == null) {
			throw new RuntimeException("resource '" + METADATA_FILE_NAME + "' not found");
		}

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		FunctionDataBuilder fdb = new FunctionDataBuilder(400);

		try {
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				if (line.length() < 1 || line.charAt(0) == '#') {
					continue;
				}
				String trimLine = line.trim();
				if (trimLine.length() < 1) {
					continue;
				}
				processLine(fdb, line);
			}
			br.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return fdb.build();
	}

	private static void processLine(FunctionDataBuilder fdb, String line) {

		String[] parts = TAB_DELIM_PATTERN.split(line, -2);
		if(parts.length != 8) {
			throw new RuntimeException("Bad line format '" + line + "' - expected 8 data fields");
		}
		int functionIndex = parseInt(parts[0]);
		String functionName = parts[1];
		int minParams = parseInt(parts[2]);
		int maxParams = parseInt(parts[3]);
		byte returnClassCode = parseReturnTypeCode(parts[4]);
		byte[] parameterClassCodes = parseOperandTypeCodes(parts[5]);
		// 6 isVolatile
		boolean hasNote = parts[7].length() > 0;

		validateFunctionName(functionName);
		// TODO - make POI use isVolatile
		fdb.add(functionIndex, functionName, minParams, maxParams, 
				returnClassCode, parameterClassCodes, hasNote);
	}
	

	private static byte parseReturnTypeCode(String code) {
		if(code.length() == 0) {
			return Ptg.CLASS_REF; // happens for GETPIVOTDATA
		}
		return parseOperandTypeCode(code);
	}

	private static byte[] parseOperandTypeCodes(String codes) {
		if(codes.length() < 1) {
			return EMPTY_BYTE_ARRAY; // happens for GETPIVOTDATA
		}
		if(isDash(codes)) {
			// '-' means empty:
			return EMPTY_BYTE_ARRAY;
		}
		String[] array = SPACE_DELIM_PATTERN.split(codes);
		int nItems = array.length;
		if(ELLIPSIS.equals(array[nItems-1])) {
			// final ellipsis is optional, and ignored
			// (all unspecified params are assumed to be the same as the last)
			nItems --;
		}
		byte[] result = new byte[nItems];
		for (int i = 0; i < nItems; i++) {
			result[i] = parseOperandTypeCode(array[i]);
		}
		return result;
	}

	private static boolean isDash(String codes) {
		if(codes.length() == 1) {
			switch (codes.charAt(0)) {
				case '-':
					return true;
			}
		}
		return false;
	}

	private static byte parseOperandTypeCode(String code) {
		if(code.length() != 1) {
			throw new RuntimeException("Bad operand type code format '" + code  + "' expected single char");
		}
		switch(code.charAt(0)) {
			case 'V': return Ptg.CLASS_VALUE;
			case 'R': return Ptg.CLASS_REF;
			case 'A': return Ptg.CLASS_ARRAY;
		}
		throw new IllegalArgumentException("Unexpected operand type code '" + code + "' (" + (int)code.charAt(0) + ")");
	}

	/**
	 * Makes sure that footnote digits from the original OOO document have not been accidentally 
	 * left behind
	 */
	private static void validateFunctionName(String functionName) {
		int len = functionName.length();
		int ix = len - 1;
		if (!Character.isDigit(functionName.charAt(ix))) {
			return;
		}
		while(ix >= 0) {
			if (!Character.isDigit(functionName.charAt(ix))) {
				break;
			}
			ix--;
		}
		if(DIGIT_ENDING_FUNCTION_NAMES_SET.contains(functionName)) {
			return;
		}
		throw new RuntimeException("Invalid function name '" + functionName 
				+ "' (is footnote number incorrectly appended)");
	}

	private static int parseInt(String valStr) {
		try {
			return Integer.parseInt(valStr);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Value '" + valStr + "' could not be parsed as an integer");
		}
	}
}
