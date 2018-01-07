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

package org.apache.poi.hssf.usermodel;


/**
 * Common class for {@link HSSFHeader} and {@link HSSFFooter}.
 */
public abstract class HeaderFooter implements org.apache.poi.ss.usermodel.HeaderFooter {

	protected HeaderFooter() {
		//
	}
	
	/**
	 * @return the internal text representation (combining center, left and right parts).
	 * Possibly empty string if no header or footer is set.  Never <code>null</code>.
	 */
	protected abstract String getRawText(); 
	
	private String[] splitParts() {
		String text = getRawText();
		// default values
		String _left = "";
		String _center = "";
		String _right = "";

// FIXME: replace outer goto. just eww.
outer:
		while (text.length() > 1) {
			if (text.charAt(0) != '&') {
				// Mimics the behaviour of Excel, which would put it in the center.
				_center = text;
				break;
			}
			int pos = text.length();
			switch (text.charAt(1)) {
			case 'L':
				if (text.contains("&C")) {
					pos = Math.min(pos, text.indexOf("&C"));
				}
				if (text.contains("&R")) {
					pos = Math.min(pos, text.indexOf("&R"));
				}
				_left = text.substring(2, pos);
				text = text.substring(pos);
				break;
			case 'C':
				if (text.contains("&L")) {
					pos = Math.min(pos, text.indexOf("&L"));
				}
				if (text.contains("&R")) {
					pos = Math.min(pos, text.indexOf("&R"));
				}
				_center = text.substring(2, pos);
				text = text.substring(pos);
				break;
			case 'R':
				if (text.contains("&C")) {
					pos = Math.min(pos, text.indexOf("&C"));
				}
				if (text.contains("&L")) {
					pos = Math.min(pos, text.indexOf("&L"));
				}
				_right = text.substring(2, pos);
				text = text.substring(pos);
				break;
			default:
				// Mimics the behaviour of Excel, which would put it in the center.
				_center = text;
				break outer;
			}
		}
		return new String[] { _left, _center, _right, };
	}

	/**
	 * @return the left side of the header or footer.
	 */
	public final String getLeft() {
		return splitParts()[0];
	}

	/**
	 * @param newLeft The string to set as the left side.
	 */
	public final void setLeft(String newLeft) {
		updatePart(0, newLeft); 
	}

	/**
	 * @return the center of the header or footer.
	 */
	public final String getCenter() {
		return splitParts()[1];
	}

	/**
	 * @param newCenter The string to set as the center.
	 */
	public final void setCenter(String newCenter) {
		updatePart(1, newCenter); 
	}

	/**
	 * @return The right side of the header or footer.
	 */
	public final String getRight() {
		return splitParts()[2];
	}

	/**
	 * @param newRight The string to set as the right side.
	 */
	public final void setRight(String newRight) {
		updatePart(2, newRight); 
	}
	
	private void updatePart(int partIndex, String newValue) {
		String[] parts = splitParts();
		parts[partIndex] = newValue == null ? "" : newValue;
		updateHeaderFooterText(parts);
	}
	/**
	 * Creates the complete footer string based on the left, center, and middle
	 * strings.
	 */
	private void updateHeaderFooterText(String[] parts) {
		String _left = parts[0];
		String _center = parts[1];
		String _right = parts[2];
		
		if (_center.length() < 1 && _left.length() < 1 && _right.length() < 1) {
			setHeaderFooterText("");
			return;
		}
		StringBuilder sb = new StringBuilder(64);
		sb.append("&C");
		sb.append(_center);
		sb.append("&L");
		sb.append(_left);
		sb.append("&R");
		sb.append(_right);
		String text = sb.toString();
		setHeaderFooterText(text);
	}

	/**
	 * @param text the new header footer text (contains mark-up tags). Possibly
	 *            empty string never <code>null</code>
	 */
	protected abstract void setHeaderFooterText(String text);

	/**
	 * @param size
	 *            the new font size
	 * @return The mark-up tag representing a new font size
	 */
	public static String fontSize(short size) {
		return "&" + size;
	}

	/**
	 * @param font
	 *            the new font
	 * @param style
	 *            the fonts style, one of regular, italic, bold, italic bold or
	 *            bold italic
	 * @return The mark-up tag representing a new font size
	 */
	public static String font(String font, String style) {
		return "&\"" + font + "," + style + "\"";
	}

	/**
	 * @return The mark-up tag representing the current page number
	 */
	public static String page() {
		return MarkupTag.PAGE_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag representing the number of pages
	 */
	public static String numPages() {
		return MarkupTag.NUM_PAGES_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag representing the current date date
	 */
	public static String date() {
		return MarkupTag.DATE_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag representing current time
	 */
	public static String time() {
		return MarkupTag.TIME_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag representing the current file name
	 */
	public static String file() {
		return MarkupTag.FILE_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag representing the current tab (sheet) name
	 */
	public static String tab() {
		return MarkupTag.SHEET_NAME_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag for start bold
	 */
	public static String startBold() {
		return MarkupTag.BOLD_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag for end bold
	 */
	public static String endBold() {
		return MarkupTag.BOLD_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag for start underline
	 */
	public static String startUnderline() {
		return MarkupTag.UNDERLINE_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag for end underline
	 */
	public static String endUnderline() {
		return MarkupTag.UNDERLINE_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag for start double underline
	 */
	public static String startDoubleUnderline() {
		return MarkupTag.DOUBLE_UNDERLINE_FIELD.getRepresentation();
	}

	/**
	 * @return The mark-up tag for end double underline
	 */
	public static String endDoubleUnderline() {
		return MarkupTag.DOUBLE_UNDERLINE_FIELD.getRepresentation();
	}

	/**
	 * Removes any fields (eg macros, page markers etc) from the string.
	 * Normally used to make some text suitable for showing to humans, and the
	 * resultant text should not normally be saved back into the document!
	 */
	public static String stripFields(String pText) {
		int pos;

		// Check we really got something to work on
		if (pText == null || pText.length() == 0) {
			return pText;
		}

		String text = pText;

		// Firstly, do the easy ones which are static
		for (MarkupTag mt : MarkupTag.values()) {
			String seq = mt.getRepresentation();
			while ((pos = text.indexOf(seq)) >= 0) {
				text = text.substring(0, pos) + text.substring(pos + seq.length());
			}
		}

		// Now do the tricky, dynamic ones
		// These are things like font sizes and font names
		text = text.replaceAll("\\&\\d+", "");
		text = text.replaceAll("\\&\".*?,.*?\"", "");

		// All done
		return text;
	}

	private enum MarkupTag {
		SHEET_NAME_FIELD ("&A", false),
		DATE_FIELD       ("&D", false),
		FILE_FIELD       ("&F", false),
		FULL_FILE_FIELD  ("&Z", false),
		PAGE_FIELD       ("&P", false),
		TIME_FIELD       ("&T", false),
		NUM_PAGES_FIELD  ("&N", false),

		PICTURE_FIELD    ("&G", false),

		BOLD_FIELD             ("&B", true),
		ITALIC_FIELD           ("&I", true),
		STRIKETHROUGH_FIELD    ("&S", true),
		SUBSCRIPT_FIELD        ("&Y", true),
		SUPERSCRIPT_FIELD      ("&X", true),
		UNDERLINE_FIELD        ("&U", true),
		DOUBLE_UNDERLINE_FIELD ("&E", true),
		;
		
		private final String _representation;
		private final boolean _occursInPairs;
		private MarkupTag(String sequence, boolean occursInPairs) {
			_representation = sequence;
			_occursInPairs = occursInPairs;
		}
		/**
		 * @return The character sequence that marks this field
		 */
		public String getRepresentation() {
			return _representation;
		}

		/**
		 * @return true if this markup tag normally comes in a pair, eg turn on
		 *         underline / turn off underline
		 */
		public boolean occursPairs() {
			return _occursInPairs;
		}
	}
}
