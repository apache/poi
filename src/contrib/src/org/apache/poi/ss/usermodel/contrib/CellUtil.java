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

package org.apache.poi.ss.usermodel.contrib;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * Various utility functions that make working with a cells and rows easier. The various methods
 * that deal with style's allow you to create your CellStyles as you need them. When you apply a
 * style change to a cell, the code will attempt to see if a style already exists that meets your
 * needs. If not, then it will create a new style. This is to prevent creating too many styles.
 * there is an upper limit in Excel on the number of styles that can be supported.
 *
 *@author Eric Pugh epugh@upstate.com
 *@author (secondary) Avinash Kewalramani akewalramani@accelrys.com
 */
public final class CellUtil {

	public static final String ALIGNMENT = "alignment";
	public static final String BORDER_BOTTOM = "borderBottom";
	public static final String BORDER_LEFT = "borderLeft";
	public static final String BORDER_RIGHT = "borderRight";
	public static final String BORDER_TOP = "borderTop";
	public static final String BOTTOM_BORDER_COLOR = "bottomBorderColor";
	public static final String DATA_FORMAT = "dataFormat";
	public static final String FILL_BACKGROUND_COLOR = "fillBackgroundColor";
	public static final String FILL_FOREGROUND_COLOR = "fillForegroundColor";
	public static final String FILL_PATTERN = "fillPattern";
	public static final String FONT = "font";
	public static final String HIDDEN = "hidden";
	public static final String INDENTION = "indention";
	public static final String LEFT_BORDER_COLOR = "leftBorderColor";
	public static final String LOCKED = "locked";
	public static final String RIGHT_BORDER_COLOR = "rightBorderColor";
	public static final String ROTATION = "rotation";
	public static final String TOP_BORDER_COLOR = "topBorderColor";
	public static final String VERTICAL_ALIGNMENT = "verticalAlignment";
	public static final String WRAP_TEXT = "wrapText";

	private static UnicodeMapping unicodeMappings[];

	private static final class UnicodeMapping {

		public final String entityName;
		public final String resolvedValue;

		public UnicodeMapping(String pEntityName, String pResolvedValue) {
			entityName = "&" + pEntityName + ";";
			resolvedValue = pResolvedValue;
		}
	}

	private CellUtil() {
		// no instances of this class
	}

	/**
	 * Get a row from the spreadsheet, and create it if it doesn't exist.
	 *
	 *@param rowIndex The 0 based row number
	 *@param sheet The sheet that the row is part of.
	 *@return The row indicated by the rowCounter
	 */
	public static Row getRow(int rowIndex, Sheet sheet) {
		Row row = sheet.getRow(rowIndex);
		if (row == null) {
			row = sheet.createRow(rowIndex);
		}
		return row;
	}


	/**
	 * Get a specific cell from a row. If the cell doesn't exist, then create it.
	 *
	 *@param row The row that the cell is part of
	 *@param columnIndex The column index that the cell is in.
	 *@return The cell indicated by the column.
	 */
	public static Cell getCell(Row row, int columnIndex) {
		Cell cell = row.getCell(columnIndex);

		if (cell == null) {
			cell = row.createCell(columnIndex);
		}
		return cell;
	}


	/**
	 * Creates a cell, gives it a value, and applies a style if provided
	 *
	 * @param  row     the row to create the cell in
	 * @param  column  the column index to create the cell in
	 * @param  value   The value of the cell
	 * @param  style   If the style is not null, then set
	 * @return         A new Cell
	 */
	public static Cell createCell(Row row, int column, String value, CellStyle style) {
		Cell cell = getCell(row, column);

		cell.setCellValue(cell.getRow().getSheet().getWorkbook().getCreationHelper()
				.createRichTextString(value));
		if (style != null) {
			cell.setCellStyle(style);
		}
		return cell;
	}


	/**
	 * Create a cell, and give it a value.
	 *
	 *@param  row     the row to create the cell in
	 *@param  column  the column index to create the cell in
	 *@param  value   The value of the cell
	 *@return         A new Cell.
	 */
	public static Cell createCell(Row row, int column, String value) {
		return createCell(row, column, value, null);
	}


	/**
	 * Take a cell, and align it.
	 *
	 *@param cell the cell to set the alignment for
	 *@param workbook The workbook that is being worked with.
	 *@param align the column alignment to use.
	 *
	 * @see CellStyle for alignment options
	 */
	public static void setAlignment(Cell cell, Workbook workbook, short align) {
		setCellStyleProperty(cell, workbook, ALIGNMENT, new Short(align));
	}

	/**
	 * Take a cell, and apply a font to it
	 *
	 *@param cell the cell to set the alignment for
	 *@param workbook The workbook that is being worked with.
	 *@param font The Font that you want to set...
	 */
	public static void setFont(Cell cell, Workbook workbook, Font font) {
		setCellStyleProperty(cell, workbook, FONT, font);
	}

	/**
	 * This method attempt to find an already existing CellStyle that matches what you want the
	 * style to be. If it does not find the style, then it creates a new one. If it does create a
	 * new one, then it applies the propertyName and propertyValue to the style. This is necessary
	 * because Excel has an upper limit on the number of Styles that it supports.
	 *
	 *@param workbook The workbook that is being worked with.
	 *@param propertyName The name of the property that is to be changed.
	 *@param propertyValue The value of the property that is to be changed.
	 *@param cell The cell that needs it's style changes
	 */
	public static void setCellStyleProperty(Cell cell, Workbook workbook, String propertyName,
			Object propertyValue) {
		CellStyle originalStyle = cell.getCellStyle();
		CellStyle newStyle = null;
		Map<String, Object> values = getFormatProperties(originalStyle);
		values.put(propertyName, propertyValue);

		// index seems like what index the cellstyle is in the list of styles for a workbook.
		// not good to compare on!
		short numberCellStyles = workbook.getNumCellStyles();

		for (short i = 0; i < numberCellStyles; i++) {
			CellStyle wbStyle = workbook.getCellStyleAt(i);
			Map<String, Object> wbStyleMap = getFormatProperties(wbStyle);

			if (wbStyleMap.equals(values)) {
				newStyle = wbStyle;
				break;
			}
		}

		if (newStyle == null) {
			newStyle = workbook.createCellStyle();
			setFormatProperties(newStyle, workbook, values);
		}

		cell.setCellStyle(newStyle);
	}

	/**
	 * Returns a map containing the format properties of the given cell style.
	 *
	 * @param style cell style
	 * @return map of format properties (String -> Object)
	 * @see #setFormatProperties(CellStyle, Map)
	 */
	private static Map<String, Object> getFormatProperties(CellStyle style) {
		Map<String, Object> properties = new HashMap<String, Object>();
		putShort(properties, ALIGNMENT, style.getAlignment());
		putShort(properties, BORDER_BOTTOM, style.getBorderBottom());
		putShort(properties, BORDER_LEFT, style.getBorderLeft());
		putShort(properties, BORDER_RIGHT, style.getBorderRight());
		putShort(properties, BORDER_TOP, style.getBorderTop());
		putShort(properties, BOTTOM_BORDER_COLOR, style.getBottomBorderColor());
		putShort(properties, DATA_FORMAT, style.getDataFormat());
		putShort(properties, FILL_BACKGROUND_COLOR, style.getFillBackgroundColor());
		putShort(properties, FILL_FOREGROUND_COLOR, style.getFillForegroundColor());
		putShort(properties, FILL_PATTERN, style.getFillPattern());
		putShort(properties, FONT, style.getFontIndex());
		putBoolean(properties, HIDDEN, style.getHidden());
		putShort(properties, INDENTION, style.getIndention());
		putShort(properties, LEFT_BORDER_COLOR, style.getLeftBorderColor());
		putBoolean(properties, LOCKED, style.getLocked());
		putShort(properties, RIGHT_BORDER_COLOR, style.getRightBorderColor());
		putShort(properties, ROTATION, style.getRotation());
		putShort(properties, TOP_BORDER_COLOR, style.getTopBorderColor());
		putShort(properties, VERTICAL_ALIGNMENT, style.getVerticalAlignment());
		putBoolean(properties, WRAP_TEXT, style.getWrapText());
		return properties;
	}

	/**
	 * Sets the format properties of the given style based on the given map.
	 *
	 * @param style cell style
	 * @param workbook parent workbook
	 * @param properties map of format properties (String -> Object)
	 * @see #getFormatProperties(CellStyle)
	 */
	private static void setFormatProperties(CellStyle style, Workbook workbook, Map<String, Object> properties) {
		style.setAlignment(getShort(properties, ALIGNMENT));
		style.setBorderBottom(getShort(properties, BORDER_BOTTOM));
		style.setBorderLeft(getShort(properties, BORDER_LEFT));
		style.setBorderRight(getShort(properties, BORDER_RIGHT));
		style.setBorderTop(getShort(properties, BORDER_TOP));
		style.setBottomBorderColor(getShort(properties, BOTTOM_BORDER_COLOR));
		style.setDataFormat(getShort(properties, DATA_FORMAT));
		style.setFillBackgroundColor(getShort(properties, FILL_BACKGROUND_COLOR));
		style.setFillForegroundColor(getShort(properties, FILL_FOREGROUND_COLOR));
		style.setFillPattern(getShort(properties, FILL_PATTERN));
		style.setFont(workbook.getFontAt(getShort(properties, FONT)));
		style.setHidden(getBoolean(properties, HIDDEN));
		style.setIndention(getShort(properties, INDENTION));
		style.setLeftBorderColor(getShort(properties, LEFT_BORDER_COLOR));
		style.setLocked(getBoolean(properties, LOCKED));
		style.setRightBorderColor(getShort(properties, RIGHT_BORDER_COLOR));
		style.setRotation(getShort(properties, ROTATION));
		style.setTopBorderColor(getShort(properties, TOP_BORDER_COLOR));
		style.setVerticalAlignment(getShort(properties, VERTICAL_ALIGNMENT));
		style.setWrapText(getBoolean(properties, WRAP_TEXT));
	}

	/**
	 * Utility method that returns the named short value form the given map.
	 * @return zero if the property does not exist, or is not a {@link Short}.
	 *
	 * @param properties map of named properties (String -> Object)
	 * @param name property name
	 * @return property value, or zero
	 */
	private static short getShort(Map<String, Object> properties, String name) {
		Object value = properties.get(name);
		if (value instanceof Short) {
			return ((Short) value).shortValue();
		}
		return 0;
	}

	/**
	 * Utility method that returns the named boolean value form the given map.
	 * @return false if the property does not exist, or is not a {@link Boolean}.
	 *
	 * @param properties map of properties (String -> Object)
	 * @param name property name
	 * @return property value, or false
	 */
	private static boolean getBoolean(Map<String, Object> properties, String name) {
		Object value = properties.get(name);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		return false;
	}

	/**
	 * Utility method that puts the named short value to the given map.
	 *
	 * @param properties map of properties (String -> Object)
	 * @param name property name
	 * @param value property value
	 */
	private static void putShort(Map<String, Object> properties, String name, short value) {
		properties.put(name, new Short(value));
	}

	/**
	 * Utility method that puts the named boolean value to the given map.
	 *
	 * @param properties map of properties (String -> Object)
	 * @param name property name
	 * @param value property value
	 */
	private static void putBoolean(Map<String, Object> properties, String name, boolean value) {
		properties.put(name, new Boolean(value));
	}

	/**
	 *  Looks for text in the cell that should be unicode, like &alpha; and provides the
	 *  unicode version of it.
	 *
	 *@param  cell  The cell to check for unicode values
	 *@return       translated to unicode
	 */
	public static Cell translateUnicodeValues(Cell cell) {

		String s = cell.getRichStringCellValue().getString();
		boolean foundUnicode = false;
		String lowerCaseStr = s.toLowerCase();

		for (int i = 0; i < unicodeMappings.length; i++) {
			UnicodeMapping entry = unicodeMappings[i];
			String key = entry.entityName;
			if (lowerCaseStr.indexOf(key) != -1) {
				s = s.replaceAll(key, entry.resolvedValue);
				foundUnicode = true;
			}
		}
		if (foundUnicode) {
			cell.setCellValue(cell.getRow().getSheet().getWorkbook().getCreationHelper()
					.createRichTextString(s));
		}
		return cell;
	}

	static {
		unicodeMappings = new UnicodeMapping[] {
			um("alpha",   "\u03B1" ),
			um("beta",    "\u03B2" ),
			um("gamma",   "\u03B3" ),
			um("delta",   "\u03B4" ),
			um("epsilon", "\u03B5" ),
			um("zeta",    "\u03B6" ),
			um("eta",     "\u03B7" ),
			um("theta",   "\u03B8" ),
			um("iota",    "\u03B9" ),
			um("kappa",   "\u03BA" ),
			um("lambda",  "\u03BB" ),
			um("mu",      "\u03BC" ),
			um("nu",      "\u03BD" ),
			um("xi",      "\u03BE" ),
			um("omicron", "\u03BF" ),
		};
	}

	private static UnicodeMapping um(String entityName, String resolvedValue) {
		return new UnicodeMapping(entityName, resolvedValue);
	}
}
