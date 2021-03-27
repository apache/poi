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

package org.apache.poi.ss.formula.function;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is not used during normal POI run-time but is used at development time to generate
 * the file 'functionMetadataCetab.txt'.  There are more than 300 built-in functions in Excel in
 * the Cetab and the intention of this class is to make it easier to maintain the metadata, by extracting
 * it from a reliable source.
 */
public final class ExcelCetabFunctionExtractor {

	private static final String SOURCE_DOC_FILE_NAME = "functionMetadataCetab-PDF.txt";

	/**
	 * For simplicity, the output file is strictly simple ASCII.
	 * This method detects any unexpected characters.
	 */
	/* package */ static boolean isSimpleAscii(char c) {

		if (c>=0x21 && c<=0x7E) {
			// everything from '!' to '~' (includes letters, digits, punctuation
			return true;
		}
		// some specific whitespace chars below 0x21:
		switch(c) {
			case ' ':
			case '\t':
			case '\r':
			case '\n':
				return true;
		}
		return false;
	}


	private static final class FunctionData {
		// special characters from the ooo document
		private static final int CHAR_ELLIPSIS_8230 = 8230;
		private static final int CHAR_NDASH_8211 = 8211;

		private final int _index;
		private final boolean _hasFootnote;
		private final String _name;
		private final int _minParams;
		private final int _maxParams;
		private final String _returnClass;
		private final String _paramClasses;
		private final boolean _isVolatile;

		public FunctionData(int funcIx, boolean hasFootnote, String funcName, int minParams, int maxParams,
					String returnClass, String paramClasses, boolean isVolatile) {
			_index = funcIx;
			_hasFootnote = hasFootnote;
			_name = funcName;
			_minParams = minParams;
			_maxParams = maxParams;
			_returnClass = convertSpecialChars(returnClass);
			_paramClasses = convertSpecialChars(paramClasses);
			_isVolatile = isVolatile;
		}
		private static String convertSpecialChars(String ss) {
			StringBuilder sb = new StringBuilder(ss.length() + 4);
			for(int i=0; i<ss.length(); i++) {
				char c = ss.charAt(i);
				if (isSimpleAscii(c)) {
					sb.append(c);
					continue;
				}
				switch (c) {
					case CHAR_NDASH_8211:
						sb.append('-');
						continue;
					case CHAR_ELLIPSIS_8230:
						sb.append("...");
						continue;
				}
				throw new RuntimeException("bad char (" + ((int)c) + ") in string '" + ss + "'");
			}
			return sb.toString();
		}
		public int getIndex() {
			return _index;
		}
		public String getName() {
			return _name;
		}
		public boolean hasFootnote() {
			return _hasFootnote;
		}
		public String formatAsDataLine() {
			return _index + "\t" + _name + "\t" + _minParams + "\t"
					+ _maxParams + "\t" + _returnClass + "\t" + _paramClasses
					+ "\t" + checkMark(_isVolatile) + "\t" + checkMark(_hasFootnote);
		}
		private static String checkMark(boolean b) {
			return b ? "x" : "";
		}
	}

	private static final class FunctionDataCollector {

		private final Map<Integer, FunctionData> _allFunctionsByIndex;
		private final Map<String, FunctionData> _allFunctionsByName;
		private final Set<Integer> _groupFunctionIndexes;
		private final Set<String> _groupFunctionNames;
		private final PrintStream _ps;

		public FunctionDataCollector(PrintStream ps) {
			_ps = ps;
			_allFunctionsByIndex = new HashMap<>();
			_allFunctionsByName = new HashMap<>();
			_groupFunctionIndexes = new HashSet<>();
			_groupFunctionNames = new HashSet<>();
		}

		public void addFunction(int funcIx, boolean hasFootnote, String funcName, int minParams, int maxParams,
								String returnClass, String paramClasses, String volatileFlagStr) {
			boolean isVolatile = volatileFlagStr.length() > 0;

			Integer funcIxKey = Integer.valueOf(funcIx);
			if(!_groupFunctionIndexes.add(funcIxKey)) {
				throw new RuntimeException("Duplicate function index (" + funcIx + ")");
			}
			if(!_groupFunctionNames.add(funcName)) {
				throw new RuntimeException("Duplicate function name '" + funcName + "'");
			}

			checkRedefinedFunction(hasFootnote, funcName, funcIxKey);
			FunctionData fd = new FunctionData(funcIx, hasFootnote, funcName,
					minParams, maxParams, returnClass, paramClasses, isVolatile);

			_allFunctionsByIndex.put(funcIxKey, fd);
			_allFunctionsByName.put(funcName, fd);
		}

		/**
		 * Some extra validation here.
		 * Any function which changes definition will have a footnote in the source document
		 */
		private void checkRedefinedFunction(boolean hasNote, String funcName, Integer funcIxKey) {
			FunctionData fdPrev;
			// check by index
			fdPrev = _allFunctionsByIndex.get(funcIxKey);
			if(fdPrev != null) {
				if(!fdPrev.hasFootnote() || !hasNote) {
					throw new RuntimeException("changing function ["
							+ funcIxKey + "] definition without foot-note");
				}
				_allFunctionsByName.remove(fdPrev.getName());
			}
			// check by name
			fdPrev = _allFunctionsByName.get(funcName);
			if(fdPrev != null) {
				if(!fdPrev.hasFootnote() || !hasNote) {
					throw new RuntimeException("changing function '"
							+ funcName + "' definition without foot-note");
				}
				_allFunctionsByIndex.remove(Integer.valueOf(fdPrev.getIndex()));
			}
		}

		public void endTableGroup(String headingText) {
			Integer[] keys = new Integer[_groupFunctionIndexes.size()];
			_groupFunctionIndexes.toArray(keys);
			_groupFunctionIndexes.clear();
			_groupFunctionNames.clear();
			Arrays.sort(keys);

			_ps.println("# " + headingText);
			for (Integer key : keys) {
				FunctionData fd = _allFunctionsByIndex.get(key);
				_ps.println(fd.formatAsDataLine());
			}
		}
	}

	private static final Pattern ID_MATCH = Pattern.compile("0x([\\dA-F]+)");
	private static final Pattern NAME_MATCH = Pattern.compile("([0-9A-Z.]+)");
	private static final Pattern ID_NAME_MATCH = Pattern.compile("0x([\\dA-F]+)\\s+([0-9A-Z.]+)");

	private static final Set<String> IGNORED_LINES = new HashSet<>();
	static {
		IGNORED_LINES.add("[MS-XLS] — v20141018");
		IGNORED_LINES.add("Excel Binary File Format (.xls) Structure");
		IGNORED_LINES.add("Copyright © 2014 Microsoft Corporation.");
		IGNORED_LINES.add("Release: October 30, 2014Value Meaning");
		IGNORED_LINES.add("Release: October 30, 2014Value");
		IGNORED_LINES.add("Meaning");
	}

	private static void extractFunctionData(FunctionDataCollector fdc, InputStream is) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

			String id = null;
			String name = null;
			while (true) {
				String line = reader.readLine();
				if(line == null) {
					break;
				}

				if(IGNORED_LINES.contains(line) || line.matches("\\d+ / \\d+")) {
					continue;
				}

				Matcher idMatcher = ID_MATCH.matcher(line);
				boolean foundID = idMatcher.matches();
				Matcher nameMatcher = NAME_MATCH.matcher(line);
				boolean foundName = nameMatcher.matches();
				Matcher idAndNameMatcher = ID_NAME_MATCH.matcher(line);
				boolean foundIDandName = idAndNameMatcher.matches();
				if(foundID && foundName ||
						foundName && foundIDandName ||
						foundID && foundIDandName) {
					throw new IllegalStateException("Should not find id and name: " +
							foundID + "/" + foundName + "/" + foundIDandName +
							", line: " + line);
				}

				if(foundID && id != null) {
					throw new IllegalStateException("Found ID, but already had one: " + id + ", line: " + line);
				}
				if(foundName && name != null) {
					throw new IllegalStateException("Found name, but already had one: " + name + ", line: " + line);
				}
				if(foundIDandName && (name != null || id != null)) {
					throw new IllegalStateException("Found name and id, but already had one: id: " + id + ", name: " + name + ", line: " + line);
				}

				if(foundID) {
					id = idMatcher.group(1);
				} else if (foundName) {
					name = nameMatcher.group(1);
				} else if (foundIDandName) {
					id = idAndNameMatcher.group(1);
					name = idAndNameMatcher.group(2);
					// manual override for one function name which contains lowercase characters
				} else if(line.equals("VBAActivate")) {
					name = line;
				} else if (id == null || name == null) {
					throw new IllegalStateException("Found params, but had empty id or name, id: " + id +
							", name: " + name + ", line: " + line);
				} else {
					System.out.println("Found function " + id + " " + name + " " + line);
					fdc.addFunction(Integer.parseInt(id, 16), false, name, 0, 0,
							"", "", "");

					id = null;
					name = null;
				}
			}
		}

		fdc.endTableGroup("");
	}
	/**
	 * To be sure that no tricky unicode chars make it through to the output file.
	 */
	private static final class SimpleAsciiOutputStream extends OutputStream {

		private final OutputStream _os;

		public SimpleAsciiOutputStream(OutputStream os) {
			_os = os;
		}
		
		@Override
        public void write(int b) throws IOException {
			checkByte(b);
			_os.write(b);
		}

		private static void checkByte(int b) {
			if (!isSimpleAscii((char)b)) {
				throw new RuntimeException("Encountered char (" + b + ") which was not simple ascii as expected");
			}
		}

		@Override
        public void write(byte[] b, int off, int len) throws IOException {
			for (int i = 0; i < len; i++) {
				checkByte(b[i + off]);

			}
			_os.write(b, off, len);
		}
	}

	private static void processFile(InputStream input, File outFile) throws IOException {
		try (OutputStream os = new SimpleAsciiOutputStream(new FileOutputStream(outFile));
		PrintStream ps = new PrintStream(os, true, "UTF-8")) {

			outputLicenseHeader(ps);
			Class<?> genClass = ExcelCetabFunctionExtractor.class;
			ps.println("# Created by (" + genClass.getName() + ")");
			// identify the source file
			ps.println("# from source file '" + SOURCE_DOC_FILE_NAME + "'");
			ps.println("#");
			ps.println("#Columns: (index, name, minParams, maxParams, returnClass, paramClasses, isVolatile, hasFootnote )");
			ps.println();
			extractFunctionData(new FunctionDataCollector(ps), input);
			ps.close();

			String canonicalOutputFileName = outFile.getCanonicalPath();
			System.out.println("Successfully output to '" + canonicalOutputFileName + "'");
		}
	}

	private static void outputLicenseHeader(PrintStream ps) {
		String[] lines= {
			"Licensed to the Apache Software Foundation (ASF) under one or more",
			"contributor license agreements.  See the NOTICE file distributed with",
			"this work for additional information regarding copyright ownership.",
			"The ASF licenses this file to You under the Apache License, Version 2.0",
			"(the \"License\"); you may not use this file except in compliance with",
			"the License.  You may obtain a copy of the License at",
			"",
			"    http://www.apache.org/licenses/LICENSE-2.0",
			"",
			"Unless required by applicable law or agreed to in writing, software",
			"distributed under the License is distributed on an \"AS IS\" BASIS,",
			"WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
			"See the License for the specific language governing permissions and",
			"limitations under the License.",
		};
		for (String line : lines) {
			ps.print("# ");
			ps.println(line);
		}
		ps.println();
	}

	public static void main(String[] args) throws IOException {
		if(!new File(SOURCE_DOC_FILE_NAME).exists()) {
			throw new IllegalStateException("Did not find file " + SOURCE_DOC_FILE_NAME + " in the resources");
		}

		try (InputStream stream = new FileInputStream(SOURCE_DOC_FILE_NAME)) {
			File outFile = new File("functionMetadataCetab.txt");

			processFile(stream, outFile);
		}
	}
}
