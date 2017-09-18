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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JLabel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.ConditionalFormatting;
import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.Removal;

/**
 * Format a value according to the standard Excel behavior.  This "standard" is
 * not explicitly documented by Microsoft, so the behavior is determined by
 * experimentation; see the tests.
 * <p>
 * An Excel format has up to four parts, separated by semicolons.  Each part
 * specifies what to do with particular kinds of values, depending on the number
 * of parts given:
 * <dl>
 * <dt>One part (example: <tt>[Green]#.##</tt>)</dt>
 * <dd>If the value is a number, display according to this one part (example: green text,
 * with up to two decimal points). If the value is text, display it as is.</dd>
 * 
 * <dt>Two parts (example: <tt>[Green]#.##;[Red]#.##</tt>)</dt>
 * <dd>If the value is a positive number or zero, display according to the first part (example: green
 * text, with up to two decimal points); if it is a negative number, display
 * according to the second part (example: red text, with up to two decimal
 * points). If the value is text, display it as is.</dd>
 * 
 * <dt>Three parts (example: <tt>[Green]#.##;[Black]#.##;[Red]#.##</tt>)</dt>
 * <dd>If the value is a positive
 * number, display according to the first part (example: green text, with up to
 * two decimal points); if it is zero, display according to the second part
 * (example: black text, with up to two decimal points); if it is a negative
 * number, display according to the third part (example: red text, with up to
 * two decimal points). If the value is text, display it as is.</dd>
 * 
 * <dt>Four parts (example: <tt>[Green]#.##;[Black]#.##;[Red]#.##;[@]</tt>)</dt>
 * <dd>If the value is a positive number, display according to the first part (example: green text,
 * with up to two decimal points); if it is zero, display according to the
 * second part (example: black text, with up to two decimal points); if it is a
 * negative number, display according to the third part (example: red text, with
 * up to two decimal points). If the value is text, display according to the
 * fourth part (example: text in the cell's usual color, with the text value
 * surround by brackets).</dd>
 * </dl>
 * <p>
 * A given format part may specify a given Locale, by including something
 *  like <tt>[$$-409]</tt> or <tt>[$&pound;-809]</tt> or <tt>[$-40C]</tt>. These
 *  are (currently) largely ignored. You can use {@link DateFormatConverter}
 *  to look these up into Java Locales if desired.
 * <p>
 * In addition to these, there is a general format that is used when no format
 * is specified.
 * 
 * TODO Merge this with {@link DataFormatter} so we only have one set of
 *  code for formatting numbers.
 * TODO Re-use parts of this logic with {@link ConditionalFormatting} /
 *  {@link ConditionalFormattingRule} for reporting stylings which do/don't apply
 * TODO Support the full set of modifiers, including alternate calendars and
 *  native character numbers, as documented at https://help.libreoffice.org/Common/Number_Format_Codes
 */
public class CellFormat {
    private final Locale locale;
    private final String format;
    private final CellFormatPart posNumFmt;
    private final CellFormatPart zeroNumFmt;
    private final CellFormatPart negNumFmt;
    private final CellFormatPart textFmt;
    private final int formatPartCount;

    private static final Pattern ONE_PART = Pattern.compile(
            CellFormatPart.FORMAT_PAT.pattern() + "(;|$)",
            Pattern.COMMENTS | Pattern.CASE_INSENSITIVE);

    /*
     * Cells that cannot be formatted, e.g. cells that have a date or time
     * format and have an invalid date or time value, are displayed as 255
     * pound signs ("#").
     */
    private static final String INVALID_VALUE_FOR_FORMAT =
            "###################################################" +
            "###################################################" +
            "###################################################" +
            "###################################################" +
            "###################################################";

    private static String QUOTE = "\"";
            
    private static CellFormat createGeneralFormat(final Locale locale) {
        return new CellFormat(locale, "General") {
            @Override
            public CellFormatResult apply(Object value) {
                String text = (new CellGeneralFormatter(locale)).format(value);
                return new CellFormatResult(true, text, null);
            }
        };
    }

    /** Maps a format string to its parsed version for efficiencies sake. */
    private static final Map<Locale, Map<String, CellFormat>> formatCache =
            new WeakHashMap<>();

    /**
     * Returns a {@link CellFormat} that applies the given format.  Two calls
     * with the same format may or may not return the same object.
     *
     * @param format The format.
     *
     * @return A {@link CellFormat} that applies the given format.
     */
    public static CellFormat getInstance(String format) {
        return getInstance(LocaleUtil.getUserLocale(), format);
    }

    /**
     * Returns a {@link CellFormat} that applies the given format.  Two calls
     * with the same format may or may not return the same object.
     *
     * @param locale The locale.
     * @param format The format.
     *
     * @return A {@link CellFormat} that applies the given format.
     */
    public static synchronized CellFormat getInstance(Locale locale, String format) {
        Map<String, CellFormat> formatMap = formatCache.get(locale);
        if (formatMap == null) {
            formatMap = new WeakHashMap<>();
            formatCache.put(locale, formatMap);
        }
        CellFormat fmt = formatMap.get(format);
        if (fmt == null) {
            if (format.equals("General") || format.equals("@"))
                fmt = createGeneralFormat(locale);
            else
                fmt = new CellFormat(locale, format);
            formatMap.put(format, fmt);
        }
        return fmt;
    }

    /**
     * Creates a new object.
     *
     * @param format The format.
     */
    private CellFormat(Locale locale, String format) {
        this.locale = locale;
        this.format = format;
        CellFormatPart defaultTextFormat = new CellFormatPart(locale, "@");
        Matcher m = ONE_PART.matcher(format);
        List<CellFormatPart> parts = new ArrayList<>();

        while (m.find()) {
            try {
                String valueDesc = m.group();

                // Strip out the semicolon if it's there
                if (valueDesc.endsWith(";"))
                    valueDesc = valueDesc.substring(0, valueDesc.length() - 1);

                parts.add(new CellFormatPart(locale, valueDesc));
            } catch (RuntimeException e) {
                CellFormatter.logger.log(Level.WARNING,
                        "Invalid format: " + CellFormatter.quote(m.group()), e);
                parts.add(null);
            }
        }
        
        formatPartCount = parts.size();
        
        switch (formatPartCount) {
        case 1:
            posNumFmt = parts.get(0);
            negNumFmt = null;
            zeroNumFmt = null;
            textFmt = defaultTextFormat;
            break;
        case 2:
            posNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            zeroNumFmt = null;
            textFmt = defaultTextFormat;
            break;
        case 3:
            posNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            zeroNumFmt = parts.get(2);
            textFmt = defaultTextFormat;
            break;
        case 4:
        default:
            posNumFmt = parts.get(0);
            negNumFmt = parts.get(1);
            zeroNumFmt = parts.get(2);
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
            if (val < 0 &&
                    ((formatPartCount == 2
                            && !posNumFmt.hasCondition() && !negNumFmt.hasCondition())
                    || (formatPartCount == 3 && !negNumFmt.hasCondition())
                    || (formatPartCount == 4 && !negNumFmt.hasCondition()))) {
                // The negative number format has the negative formatting required,
                // e.g. minus sign or brackets, so pass a positive value so that
                // the default leading minus sign is not also output
                return negNumFmt.apply(-val);
            } else {
                return getApplicableFormatPart(val).apply(val);
            }
        } else if (value instanceof java.util.Date) {
            // Don't know (and can't get) the workbook date windowing (1900 or 1904)
            // so assume 1900 date windowing
            Double numericValue = DateUtil.getExcelDate((Date) value);
            if (DateUtil.isValidExcelDate(numericValue)) {
                return getApplicableFormatPart(numericValue).apply(value);
            } else {
                throw new IllegalArgumentException("value " + numericValue + " of date " + value + " is not a valid Excel date");
            }
        } else {
            return textFmt.apply(value);
        }
    }

    /**
     * Returns the result of applying the format to the given date.
     *
     * @param date         The date.
     * @param numericValue The numeric value for the date.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    private CellFormatResult apply(Date date, double numericValue) {
        return getApplicableFormatPart(numericValue).apply(date);
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
        case BLANK:
            return apply("");
        case BOOLEAN:
            return apply(c.getBooleanCellValue());
        case NUMERIC:
            Double value = c.getNumericCellValue();
            if (getApplicableFormatPart(value).getCellFormatType() == CellFormatType.DATE) {
                if (DateUtil.isValidExcelDate(value)) {
                    return apply(c.getDateCellValue(), value);
                } else {
                    return apply(INVALID_VALUE_FOR_FORMAT);
                }
            } else {
                return apply(value);
            }
        case STRING:
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
     * Uses the result of applying this format to the given date, setting the text
     * and color of a label before returning the result.
     *
     * @param label        The label to apply to.
     * @param date         The date.
     * @param numericValue The numeric value for the date.
     *
     * @return The result, in a {@link CellFormatResult}.
     */
    private CellFormatResult apply(JLabel label, Date date, double numericValue) {
        CellFormatResult result = apply(date, numericValue);
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
            case BLANK:
                return apply(label, "");
            case BOOLEAN:
                return apply(label, c.getBooleanCellValue());
            case NUMERIC:
                Double value = c.getNumericCellValue();
                if (getApplicableFormatPart(value).getCellFormatType() == CellFormatType.DATE) {
                    if (DateUtil.isValidExcelDate(value)) {
                        return apply(label, c.getDateCellValue(), value);
                    } else {
                        return apply(label, INVALID_VALUE_FOR_FORMAT);
                    }
                } else {
                    return apply(label, value);
                }
            case STRING:
                return apply(label, c.getStringCellValue());
            default:
                return apply(label, "?");
            }
    }

    /**
     * Returns the {@link CellFormatPart} that applies to the value.  Result
     * depends on how many parts the cell format has, the cell value and any
     * conditions.  The value must be a {@link Number}.
     * 
     * @param value The value.
     * @return The {@link CellFormatPart} that applies to the value.
     */
    private CellFormatPart getApplicableFormatPart(Object value) {
        
        if (value instanceof Number) {
            
            double val = ((Number) value).doubleValue();
            
            if (formatPartCount == 1) {
                if (!posNumFmt.hasCondition()
                        || (posNumFmt.hasCondition() && posNumFmt.applies(val))) {
                    return posNumFmt;
                } else {
                    return new CellFormatPart(locale, "General");
                }
            } else if (formatPartCount == 2) {
                if ((!posNumFmt.hasCondition() && val >= 0)
                        || (posNumFmt.hasCondition() && posNumFmt.applies(val))) {
                    return posNumFmt;
                } else if (!negNumFmt.hasCondition()
                        || (negNumFmt.hasCondition() && negNumFmt.applies(val))) {
                    return negNumFmt;
                } else {
                    // Return ###...### (255 #s) to match Excel 2007 behaviour
                    return new CellFormatPart(QUOTE + INVALID_VALUE_FOR_FORMAT + QUOTE);
                }
            } else {
                if ((!posNumFmt.hasCondition() && val > 0)
                        || (posNumFmt.hasCondition() && posNumFmt.applies(val))) {
                    return posNumFmt;
                } else if ((!negNumFmt.hasCondition() && val < 0)
                        || (negNumFmt.hasCondition() && negNumFmt.applies(val))) {
                    return negNumFmt;
                // Only the first two format parts can have conditions
                } else {
                    return zeroNumFmt;
                }
            }
        } else {
            throw new IllegalArgumentException("value must be a Number");
        }
        
    }
    
    /**
     * Returns the ultimate cell type, following the results of formulas.  If
     * the cell is a {@link CellType#FORMULA}, this returns the result of
     * {@link Cell#getCachedFormulaResultType()}.  Otherwise this returns the
     * result of {@link Cell#getCellType()}.
     * 
     * @param cell The cell.
     *
     * @return The ultimate type of this cell.
     */
    public static CellType ultimateType(Cell cell) {
        CellType type = cell.getCellType();
        if (type == CellType.FORMULA)
            return cell.getCachedFormulaResultType();
        else
            return type;
    }

    /**
     * Returns the ultimate cell type, following the results of formulas.  If
     * the cell is a {@link CellType#FORMULA}, this returns the result of
     * {@link Cell#getCachedFormulaResultType()}.  Otherwise this returns the
     * result of {@link Cell#getCellType()}.
     *
     * @param cell The cell.
     *
     * @return The ultimate type of this cell.
     * @since POI 3.15 beta 3
     * @deprecated use <code>ultimateType</code> instead
     */
    @Deprecated
    @Removal(version = "4.2")
    public static CellType ultimateTypeEnum(Cell cell) {
        return ultimateType(cell);
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
