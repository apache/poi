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

package org.apache.poi.ss.format;

import org.apache.poi.ss.usermodel.Cell;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Format a value according to the standard Excel behavior.  This "standard" is
 * not explicitly documented by Microsoft, so the behavior is determined by
 * experimentation; see the tests.
 * <p/>
 * An Excel format has up to four parts, separated by semicolons.  Each part
 * specifies what to do with particular kinds of values, depending on the number
 * of parts given: <dl> <dt>One part (example: <tt>[Green]#.##</tt>) <dd>If the
 * value is a number, display according to this one part (example: green text,
 * with up to two decimal points). If the value is text, display it as is.
 * <dt>Two parts (example: <tt>[Green]#.##;[Red]#.##</tt>) <dd>If the value is a
 * positive number or zero, display according to the first part (example: green
 * text, with up to two decimal points); if it is a negative number, display
 * according to the second part (example: red text, with up to two decimal
 * points). If the value is text, display it as is. <dt>Three parts (example:
 * <tt>[Green]#.##;[Black]#.##;[Red]#.##</tt>) <dd>If the value is a positive
 * number, display according to the first part (example: green text, with up to
 * two decimal points); if it is zero, display according to the second part
 * (example: black text, with up to two decimal points); if it is a negative
 * number, display according to the third part (example: red text, with up to
 * two decimal points). If the value is text, display it as is. <dt>Four parts
 * (example: <tt>[Green]#.##;[Black]#.##;[Red]#.##;[@]</tt>) <dd>If the value is
 * a positive number, display according to the first part (example: green text,
 * with up to two decimal points); if it is zero, display according to the
 * second part (example: black text, with up to two decimal points); if it is a
 * negative number, display according to the third part (example: red text, with
 * up to two decimal points). If the value is text, display according to the
 * fourth part (example: text in the cell's usual color, with the text value
 * surround by brackets). </dl>
 * <p/>
 * In addition to these, there is a general format that is used when no format
 * is specified.  This formatting is presented by the {@link #GENERAL_FORMAT}
 * object.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
@SuppressWarnings({"Singleton"})
public class CellFormat {
    private final String format;
    private final CellFormatPart posNumFmt;
    private final CellFormatPart zeroNumFmt;
    private final CellFormatPart negNumFmt;
    private final CellFormatPart textFmt;

    private static final Pattern ONE_PART = Pattern.compile(
            CellFormatPart.FORMAT_PAT.pattern() + "(;|$)",
            Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);

    private static final CellFormatPart DEFAULT_TEXT_FORMAT =
            new CellFormatPart("@");

    /**
     * Format a value as it would be were no format specified.  This is also
     * used when the format specified is <tt>General</tt>.
     */
    public static final CellFormat GENERAL_FORMAT = new CellFormat("General") {
        @Override
        public CellFormatResult apply(Object value) {
            String text;
            if (value == null) {
                text = "";
            } else if (value instanceof Number) {
                text = CellNumberFormatter.SIMPLE_NUMBER.format(value);
            } else {
                text = value.toString();
            }
            return new CellFormatResult(true, text, null);
        }
    };

    /** Maps a format string to its parsed version for efficiencies sake. */
    private static final Map<String, CellFormat> formatCache =
            new WeakHashMap<String, CellFormat>();

    /**
     * Returns a {@link CellFormat} that applies the given format.  Two calls
     * with the same format may or may not return the same object.
     *
     * @param format The format.
     *
     * @return A {@link CellFormat} that applies the given format.
     */
    public static CellFormat getInstance(String format) {
        CellFormat fmt = formatCache.get(format);
        if (fmt == null) {
            if (format.equals("General"))
                fmt = GENERAL_FORMAT;
            else
                fmt = new CellFormat(format);
            formatCache.put(format, fmt);
        }
        return fmt;
    }

    /**
     * Creates a new object.
     *
     * @param format The format.
     */
    private CellFormat(String format) {
        this.format = format;
        Matcher m = ONE_PART.matcher(format);
        List<CellFormatPart> parts = new ArrayList<CellFormatPart>();

        while (m.find()) {
            try {
                String valueDesc = m.group();

                // Strip out the semicolon if it's there
                if (valueDesc.endsWith(";"))
                    valueDesc = valueDesc.substring(0, valueDesc.length() - 1);

                parts.add(new CellFormatPart(valueDesc));
            } catch (RuntimeException e) {
                CellFormatter.logger.log(Level.WARNING,
                        "Invalid format: " + CellFormatter.quote(m.group()), e);
                parts.add(null);
            }
        }

        switch (parts.size()) {
        case 1:
            posNumFmt = zeroNumFmt = negNumFmt = parts.get(0);
            textFmt = DEFAULT_TEXT_FORMAT;
            break;
        case 2:
            posNumFmt = zeroNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            textFmt = DEFAULT_TEXT_FORMAT;
            break;
        case 3:
            posNumFmt = parts.get(0);
            zeroNumFmt = parts.get(1);
            negNumFmt = parts.get(2);
            textFmt = DEFAULT_TEXT_FORMAT;
            break;
        case 4:
        default:
            posNumFmt = parts.get(0);
            zeroNumFmt = parts.get(1);
            negNumFmt = parts.get(2);
            textFmt = parts.get(3);
            break;
        }
    }

    /**
     * Returns the result of applying the format to the given value.  If the
     * value is a number (a type of {@link Number} object), the correct number
     * format type is chosen; otherwise it is considered a text object.
     *
     * @param value The value
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(Object value) {
        if (value instanceof Number) {
            Number num = (Number) value;
            double val = num.doubleValue();
            if (val > 0)
                return posNumFmt.apply(value);
            else if (val < 0)
                return negNumFmt.apply(-val);
            else
                return zeroNumFmt.apply(value);
        } else {
            return textFmt.apply(value);
        }
    }

    /**
     * Fetches the appropriate value from the cell, and returns the result of
     * applying it to the appropriate format.  For formula cells, the computed
     * value is what is used.
     *
     * @param c The cell.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(Cell c) {
        switch (ultimateType(c)) {
        case Cell.CELL_TYPE_BLANK:
            return apply("");
        case Cell.CELL_TYPE_BOOLEAN:
            return apply(c.getStringCellValue());
        case Cell.CELL_TYPE_NUMERIC:
            return apply(c.getNumericCellValue());
        case Cell.CELL_TYPE_STRING:
            return apply(c.getStringCellValue());
        default:
            return apply("?");
        }
    }

    /**
     * Uses the result of applying this format to the value, setting the text
     * and color of a label before returning the result.
     *
     * @param label The label to apply to.
     * @param value The value to process.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(JLabel label, Object value) {
        CellFormatResult result = apply(value);
        label.setText(result.text);
        if (result.textColor != null) {
            label.setForeground(result.textColor);
        }
        return result;
    }

    /**
     * Fetches the appropriate value from the cell, and uses the result, setting
     * the text and color of a label before returning the result.
     *
     * @param label The label to apply to.
     * @param c     The cell.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    public CellFormatResult apply(JLabel label, Cell c) {
        switch (ultimateType(c)) {
        case Cell.CELL_TYPE_BLANK:
            return apply(label, "");
        case Cell.CELL_TYPE_BOOLEAN:
            return apply(label, c.getStringCellValue());
        case Cell.CELL_TYPE_NUMERIC:
            return apply(label, c.getNumericCellValue());
        case Cell.CELL_TYPE_STRING:
            return apply(label, c.getStringCellValue());
        default:
            return apply(label, "?");
        }
    }

    /**
     * Returns the ultimate cell type, following the results of formulas.  If
     * the cell is a {@link Cell#CELL_TYPE_FORMULA}, this returns the result of
     * {@link Cell#getCachedFormulaResultType()}.  Otherwise this returns the
     * result of {@link Cell#getCellType()}.
     *
     * @param cell The cell.
     *
     * @return The ultimate type of this cell.
     */
    public static int ultimateType(Cell cell) {
        int type = cell.getCellType();
        if (type == Cell.CELL_TYPE_FORMULA)
            return cell.getCachedFormulaResultType();
        else
            return type;
    }

    /**
     * Returns <tt>true</tt> if the other object is a {@link CellFormat} object
     * with the same format.
     *
     * @param obj The other object.
     *
     * @return <tt>true</tt> if the two objects are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof CellFormat) {
            CellFormat that = (CellFormat) obj;
            return format.equals(that.format);
        }
        return false;
    }

    /**
     * Returns a hash code for the format.
     *
     * @return A hash code for the format.
     */
    @Override
    public int hashCode() {
        return format.hashCode();
    }
}