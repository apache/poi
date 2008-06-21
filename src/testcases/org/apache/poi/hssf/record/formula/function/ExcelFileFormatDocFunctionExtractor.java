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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class is not used during normal POI run-time but is used at development time to generate
 * the file 'functionMetadata.txt'.   There are more than 300 built-in functions in Excel and the
 * intention of this class is to make it easier to maintain the metadata, by extracting it from
 * a reliable source.
 * 
 * @author Josh Micich
 */
public final class ExcelFileFormatDocFunctionExtractor {

	private static final String SOURCE_DOC_FILE_NAME = "excelfileformat.odt";

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
			StringBuffer sb = new StringBuffer(ss.length() + 4);
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

		private final Map _allFunctionsByIndex;
		private final Map _allFunctionsByName;
		private final Set _groupFunctionIndexes;
		private final Set _groupFunctionNames;
		private final PrintStream _ps;
		
		public FunctionDataCollector(PrintStream ps) {
			_ps = ps;
			_allFunctionsByIndex = new HashMap();
			_allFunctionsByName = new HashMap();
			_groupFunctionIndexes = new HashSet();
			_groupFunctionNames = new HashSet();
		}

		public void addFuntion(int funcIx, boolean hasFootnote, String funcName, int minParams, int maxParams,
				String returnClass, String paramClasses, String volatileFlagStr) {
			boolean isVolatile = volatileFlagStr.length() > 0;
			
			Integer funcIxKey = new Integer(funcIx);
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
			fdPrev = (FunctionData) _allFunctionsByIndex.get(funcIxKey);
			if(fdPrev != null) {
				if(!fdPrev.hasFootnote() || !hasNote) {
					throw new RuntimeException("changing function [" 
							+ funcIxKey + "] definition without foot-note");
				}
				_allFunctionsByName.remove(fdPrev.getName());
			}
			// check by name
			fdPrev = (FunctionData) _allFunctionsByName.get(funcName);
			if(fdPrev != null) {
				if(!fdPrev.hasFootnote() || !hasNote) {
					throw new RuntimeException("changing function '" 
							+ funcName + "' definition without foot-note");
				}
				_allFunctionsByIndex.remove(new Integer(fdPrev.getIndex()));
			}
		}

		public void endTableGroup(String headingText) {
			Integer[] keys = new Integer[_groupFunctionIndexes.size()];
			_groupFunctionIndexes.toArray(keys);
			_groupFunctionIndexes.clear();
			_groupFunctionNames.clear();
			Arrays.sort(keys);
			
			_ps.println("# " + headingText);
			for (int i = 0; i < keys.length; i++) {
				FunctionData fd = (FunctionData) _allFunctionsByIndex.get(keys[i]);
				_ps.println(fd.formatAsDataLine());
			}
		}
	}
	
	/**
	 * To avoid drag-in - parse XML using only JDK. 
	 */
	private static class EFFDocHandler implements ContentHandler {
		private static final String[] HEADING_PATH_NAMES = {
			"office:document-content", "office:body", "office:text", "text:h",	
		};
		private static final String[] TABLE_BASE_PATH_NAMES = {
			"office:document-content", "office:body", "office:text", "table:table",	
		};
		private static final String[] TABLE_ROW_RELPATH_NAMES = {
			"table:table-row",	
		};
		private static final String[] TABLE_CELL_RELPATH_NAMES = {
			"table:table-row", "table:table-cell", "text:p",	
		};
		// after May 2008 there was one more style applied to the footnotes
		private static final String[] NOTE_REF_RELPATH_NAMES_OLD = { 
			"table:table-row", "table:table-cell", "text:p", "text:span", "text:note-ref",
		};
		private static final String[] NOTE_REF_RELPATH_NAMES = {
			"table:table-row", "table:table-cell", "text:p", "text:span", "text:span", "text:note-ref",
		};


		private final Stack _elemNameStack;
		/** <code>true</code> only when parsing the target tables */
		private boolean _isInsideTable;

		private final List _rowData; 
		private final StringBuffer _textNodeBuffer;
		private final List _rowNoteFlags;
		private boolean _cellHasNote;

		private final FunctionDataCollector _fdc;
		private String _lastHeadingText;

		public EFFDocHandler(FunctionDataCollector fdc) {
			_fdc = fdc;
			_elemNameStack = new Stack();
			_isInsideTable = false;
			_rowData = new ArrayList();
			_textNodeBuffer = new StringBuffer();
			_rowNoteFlags = new ArrayList();
		}

		private boolean matchesTargetPath() {
			return matchesPath(0, TABLE_BASE_PATH_NAMES);
		}
		private boolean matchesRelPath(String[] pathNames) {
			return matchesPath(TABLE_BASE_PATH_NAMES.length, pathNames);
		}
		private boolean matchesPath(int baseStackIndex, String[] pathNames) {
			if(_elemNameStack.size() != baseStackIndex + pathNames.length) {
				return false;
			}
			for (int i = 0; i < pathNames.length; i++) {
				if(!_elemNameStack.get(baseStackIndex + i).equals(pathNames[i])) {
					return false;
				}
			}
			return true;
		}
		public void characters(char[] ch, int start, int length) {
			// only 2 text nodes where text is collected:
			if(matchesRelPath(TABLE_CELL_RELPATH_NAMES) || matchesPath(0, HEADING_PATH_NAMES)) {
				_textNodeBuffer.append(ch, start, length);
			}
		}

		public void endElement(String namespaceURI, String localName, String name) {
			String expectedName = (String) _elemNameStack.peek();
			if(expectedName != name) {
				throw new RuntimeException("close tag mismatch");
			}
			if(matchesPath(0, HEADING_PATH_NAMES)) {
				_lastHeadingText = _textNodeBuffer.toString().trim();
				_textNodeBuffer.setLength(0);
			}			
			if(_isInsideTable) {
				if(matchesTargetPath()) {
					_fdc.endTableGroup(_lastHeadingText);
					_isInsideTable = false;
				} else if(matchesRelPath(TABLE_ROW_RELPATH_NAMES)) {
					String[] cellData = new String[_rowData.size()];
					_rowData.toArray(cellData);
					_rowData.clear();
					Boolean[] noteFlags = new Boolean[_rowNoteFlags.size()];
					_rowNoteFlags.toArray(noteFlags);
					_rowNoteFlags.clear();
					processTableRow(cellData, noteFlags);
				} else if(matchesRelPath(TABLE_CELL_RELPATH_NAMES)) {
					_rowData.add(_textNodeBuffer.toString().trim());
					_rowNoteFlags.add(Boolean.valueOf(_cellHasNote));
					_textNodeBuffer.setLength(0);
				}
			}
			_elemNameStack.pop();
		}

		private void processTableRow(String[] cellData, Boolean[] noteFlags) {
			// each table row of the document contains data for two functions
			if(cellData.length != 15) {
				throw new RuntimeException("Bad table row size");
			}
			processFunction(cellData, noteFlags, 0);
			processFunction(cellData, noteFlags, 8);
		}
		public void processFunction(String[] cellData, Boolean[] noteFlags, int i) {
			String funcIxStr = cellData[i + 0];
			if (funcIxStr.length() < 1) {
				// empty (happens on the right hand side when there is an odd number of functions)
				return;
			}
			int funcIx = parseInt(funcIxStr);

			boolean hasFootnote = noteFlags[i + 1].booleanValue();
			String funcName = cellData[i + 1];
			int minParams = parseInt(cellData[i + 2]);
			int maxParams = parseInt(cellData[i + 3]);

			String returnClass = cellData[i + 4];
			String paramClasses = cellData[i + 5];
			String volatileFlagStr = cellData[i + 6];
			
			_fdc.addFuntion(funcIx, hasFootnote, funcName, minParams, maxParams, returnClass, paramClasses, volatileFlagStr);
		}
		private static int parseInt(String valStr) {
			try {
				return Integer.parseInt(valStr);
			} catch (NumberFormatException e) {
				throw new RuntimeException("Value '" + valStr + "' could not be parsed as an integer");
			}
		}
		public void startElement(String namespaceURI, String localName, String name, Attributes atts) {
			_elemNameStack.add(name);
			if(matchesTargetPath()) {
				String tableName = atts.getValue("table:name");
				if(tableName.startsWith("tab_fml_func") && !tableName.equals("tab_fml_func0")) {
					_isInsideTable = true;
				}
				return;
			}
			if(matchesPath(0, HEADING_PATH_NAMES)) {
				_textNodeBuffer.setLength(0);
			} else if(matchesRelPath(TABLE_ROW_RELPATH_NAMES)) {
				_rowData.clear();
				_rowNoteFlags.clear();
			} else if(matchesRelPath(TABLE_CELL_RELPATH_NAMES)) {
				_textNodeBuffer.setLength(0);
				_cellHasNote = false;
			} else if(matchesRelPath(NOTE_REF_RELPATH_NAMES_OLD)) {
				_cellHasNote = true;
			} else if(matchesRelPath(NOTE_REF_RELPATH_NAMES)) {
				_cellHasNote = true;
			}
		}

		public void endDocument() {
			// do nothing
		}
		public void endPrefixMapping(String prefix) {
			// do nothing
		}
		public void ignorableWhitespace(char[] ch, int start, int length) {
			// do nothing
		}
		public void processingInstruction(String target, String data) {
			// do nothing
		}
		public void setDocumentLocator(Locator locator) {
			// do nothing
		}
		public void skippedEntity(String name) {
			// do nothing
		}
		public void startDocument() {
			// do nothing
		}
		public void startPrefixMapping(String prefix, String uri) {
			// do nothing
		}
	}

	private static void extractFunctionData(FunctionDataCollector fdc, InputStream is) {
		XMLReader xr;
		
		try {
			// First up, try the default one
			xr = XMLReaderFactory.createXMLReader();
		} catch (SAXException e) {
			// Try one for java 1.4
			System.setProperty("org.xml.sax.driver", "org.apache.crimson.parser.XMLReaderImpl");
			try {
				xr = XMLReaderFactory.createXMLReader();
			} catch (SAXException e2) {
				throw new RuntimeException(e2);
			}
		}
		xr.setContentHandler(new EFFDocHandler(fdc));

		InputSource inSrc = new InputSource(is);

		try {
			xr.parse(inSrc);
			is.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * To be sure that no tricky unicode chars make it through to the output file.
	 */
	private static final class SimpleAsciiOutputStream extends OutputStream {

		private final OutputStream _os;

		public SimpleAsciiOutputStream(OutputStream os) {
			_os = os;
		}
		public void write(int b) throws IOException {
			checkByte(b);
			_os.write(b);
		}
		private static void checkByte(int b) {
			if (!isSimpleAscii((char)b)) {
				throw new RuntimeException("Encountered char (" + b + ") which was not simple ascii as expected");
			}
		}
		public void write(byte[] b, int off, int len) throws IOException {
			for (int i = 0; i < len; i++) {
				checkByte(b[i + off]);
				
			}
			_os.write(b, off, len);
		}
	}

	private static void processFile(File effDocFile, File outFile) {
		if(!effDocFile.exists()) {
			throw new RuntimeException("file '" + effDocFile.getAbsolutePath() + "' does not exist");
		}
		OutputStream os;
		try {
			os = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		os = new SimpleAsciiOutputStream(os);
		PrintStream ps;
		try {
			ps = new PrintStream(os, true, "UTF-8");
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		outputLicenseHeader(ps);
		Class genClass = ExcelFileFormatDocFunctionExtractor.class;
		ps.println("# Created by (" + genClass.getName() + ")");
		// identify the source file
		ps.print("# from source file '" + SOURCE_DOC_FILE_NAME + "'");
		ps.println(" (size=" + effDocFile.length() + ", md5=" + getFileMD5(effDocFile) + ")");
		ps.println("#");
		ps.println("#Columns: (index, name, minParams, maxParams, returnClass, paramClasses, isVolatile, hasFootnote )");
		ps.println("");
		try {
			ZipFile zf = new ZipFile(effDocFile);
			InputStream is = zf.getInputStream(zf.getEntry("content.xml"));
			extractFunctionData(new FunctionDataCollector(ps), is);
			zf.close();
		} catch (ZipException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ps.close();
		
		String canonicalOutputFileName;
		try {
			canonicalOutputFileName = outFile.getCanonicalPath();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("Successfully output to '" + canonicalOutputFileName + "'");
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
		for (int i = 0; i < lines.length; i++) {
			ps.print("# ");
			ps.println(lines[i]);
		}
		ps.println();
	}

	/**
	 * Helps identify the source file
	 */
	private static String getFileMD5(File f) {
		MessageDigest m;
		try {
			m = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		byte[]buf = new byte[2048];
		try {
			InputStream is = new FileInputStream(f);
			while(true) {
				int bytesRead = is.read(buf);
				if(bytesRead<1) { 
					break;
				}
				m.update(buf, 0, bytesRead);
			}
			is.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		return "0x" + new BigInteger(1, m.digest()).toString(16);
	}

	private static File downloadSourceFile() {
		URL url;
		try {
			url = new URL("http://sc.openoffice.org/" + SOURCE_DOC_FILE_NAME);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}

		File result;
		byte[]buf = new byte[2048];
		try {
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			System.out.println("downloading " + url.toExternalForm());
			result = File.createTempFile("excelfileformat", ".odt");
			OutputStream os = new FileOutputStream(result);
			while(true) {
				int bytesRead = is.read(buf);
				if(bytesRead<1) { 
					break;
				}
				os.write(buf, 0, bytesRead);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("file downloaded ok");
		return result;
	}

	public static void main(String[] args) {

		File outFile = new File("functionMetadata-asGenerated.txt");

		if (false) { // set true to use local file
			File dir = new File("c:/temp");
			File effDocFile = new File(dir, SOURCE_DOC_FILE_NAME);
			processFile(effDocFile, outFile);
			return;
		}
		
		File tempEFFDocFile = downloadSourceFile();
		processFile(tempEFFDocFile, outFile);
		tempEFFDocFile.delete();
	}
}
