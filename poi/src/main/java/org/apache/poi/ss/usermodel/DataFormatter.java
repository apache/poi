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

import java.beans.PropertyChangeSupport;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.poi.logging.PoiLogManager;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.formula.ConditionalFormattingEvaluator;
import org.apache.poi.ss.util.DateFormatConverter;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.StringUtil;


/**
 * DataFormatter contains methods for formatting the value stored in a
 * Cell. This can be useful for reports and GUI presentations when you
 * need to display data exactly as it appears in Excel. Supported formats
 * include currency, SSN, percentages, decimals, dates, phone numbers, zip
 * codes, etc.
 * <p>
 * Internally, formats will be implemented using subclasses of {@link Format}
 * such as {@link DecimalFormat} and {@link SimpleDateFormat}. Therefore the
 * formats used by this class must obey the same pattern rules as these Format
 * subclasses. This means that only legal number pattern characters ("0", "#",
 * ".", "," etc.) may appear in number formats. Other characters can be
 * inserted <em>before</em> or <em> after</em> the number pattern to form a
 * prefix or suffix.
 * </p>
 * <p>
 * For example the Excel pattern {@code "$#,##0.00 "USD"_);($#,##0.00 "USD")"
 * } will be correctly formatted as "$1,000.00 USD" or "($1,000.00 USD)".
 * However the pattern {@code "00-00-00"} is incorrectly formatted by
 * DecimalFormat as "000000--". For Excel formats that are not compatible with
 * DecimalFormat, you can provide your own custom {@link Format} implementation
 * via {@code DataFormatter.addFormat(String,Format)}. The following
 * custom formats are already provided by this class:
 * </p>
 * <pre>{@code
 * SSN "000-00-0000"
 * Phone Number "(###) ###-####"
 * Zip plus 4 "00000-0000"
 * }</pre>
 * <p>
 * If the Excel format pattern cannot be parsed successfully, then a default
 * format will be used. The default number format will mimic the Excel General
 * format: "#" for whole numbers and "#.##########" for decimal numbers. You
 * can override the default format pattern with {@code
 * DataFormatter.setDefaultNumberFormat(Format)}. <b>Note:</b> the
 * default format will only be used when a Format cannot be created from the
 * cell's data format string.
 *
 * <p>
 * Note that by default formatted numeric values are trimmed.
 * Excel formats can contain spacers and padding and the default behavior is to strip them off.
 * </p>
 * <p>Example:</p>
 * <p>
 * Consider a numeric cell with a value {@code 12.343} and format {@code "##.##_ "}.
 *  The trailing underscore and space ("_ ") in the format adds a space to the end and Excel formats this cell as {@code "12.34 "},
 *  but {@code DataFormatter} trims the formatted value and returns {@code "12.34"}.
 * </p>
 * You can enable spaces by passing the {@code emulateCSV=true} flag in the {@code DateFormatter} constructor.
 * If set to true, then the output tries to conform to what you get when you take an xls or xlsx in Excel and Save As CSV file:
 * <ul>
 *  <li>returned values are not trimmed</li>
 *  <li>Invalid dates are formatted as  255 pound signs ("#")</li>
 *  <li>simulate Excel's handling of a format string of all # when the value is 0.
 *   Excel will output "", {@code DataFormatter} will output "0".
 * </ul>
 * <p>
 *  Some formats are automatically "localized" by Excel, eg show as mm/dd/yyyy when
 *   loaded in Excel in some Locales but as dd/mm/yyyy in others. These are always
 *   returned in the "default" (US) format, as stored in the file.
 *  Some format strings request an alternate locale, eg
 *   {@code [$-809]d/m/yy h:mm AM/PM} which explicitly requests UK locale.
 *   These locale directives are (currently) ignored.
 *  You can use {@link DateFormatConverter} to do some of this localisation if
 *   you need it.
 */
@SuppressWarnings("unused")
public class DataFormatter {
    private static final String defaultFractionWholePartFormat = "#";
    private static final String defaultFractionFractionPartFormat = "#/##";
    /** Pattern to find a number format: "0" or  "#" */
    private static final Pattern numPattern = Pattern.compile("[0#]+");

    /** Pattern to find days of week as text "ddd...." */
    private static final Pattern daysAsText = Pattern.compile("([d]{3,})", Pattern.CASE_INSENSITIVE);

    /** Pattern to find "AM/PM" marker */
    private static final Pattern amPmPattern = Pattern.compile("(([AP])[M/P]*)", Pattern.CASE_INSENSITIVE);

    /** Stands for the AM/PM marker while a date format is converted */
    private static final String AM_PM_PLACEHOLDER = "\ue000";

    /** Excel's Chinese AM/PM marker, which shows 上午 (morning) or 下午 (afternoon) */
    private static final String CHINESE_AM_PM = "上午/下午";

    /** Pattern to find the [DBNum1], [DBNum2] or [DBNum3] Chinese/Japanese numeral styles */
    private static final Pattern dbNumPattern = Pattern.compile("\\[DBNum[123]]", Pattern.CASE_INSENSITIVE);

    /** Pattern to find formats with condition ranges e.g. [>=100] */
    private static final Pattern rangeConditionalPattern = Pattern.compile("\\[\\s*(<>|<=|>=|<|>|=)\\s*-?[0-9]*\\.?[0-9]+\\s*]");

    /**
     * A regex to find locale patterns like [$$-1009] and [$?-452], or a currency string without a locale like [$USD].
     * Note that we don't currently process these into locales
     */
    private static final Pattern localePatternGroup = Pattern.compile("(\\[\\$[^-\\]]*(?:-[0-9A-Za-z]+)?])");

    /**
     * A regex to match the colour formatting's rules.
     * Allowed colours are: Black, Blue, Cyan, Green,
     *  Magenta, Red, White, Yellow, "Color n" (1<=n<=56)
     */
    private static final Pattern colorPattern =
       Pattern.compile("(\\[BLACK])|(\\[BLUE])|(\\[CYAN])|(\\[GREEN])|" +
            "(\\[MAGENTA])|(\\[RED])|(\\[WHITE])|(\\[YELLOW])|" +
            "(\\[COLOR\\s*\\d])|(\\[COLOR\\s*[0-5]\\d])", Pattern.CASE_INSENSITIVE);

    /**
     * A regex to identify a fraction pattern.
     * This requires that replaceAll("\\?", "#") has already been called
     */
    private static final Pattern fractionPattern = Pattern.compile("(?:([#\\d]+)\\s+)?(#+)\\s*/\\s*([#\\d]+)");

    /**
     * A regex to strip junk out of fraction formats
     */
    private static final Pattern fractionStripper = Pattern.compile("(\"[^\"]*\")|([^ ?#\\d/]+)");

    /**
     * A regex to detect if an alternate grouping character is used
     *  in a numeric format
     */
    private static final Pattern alternateGrouping = Pattern.compile("([#0]([^.#0])[#0]{3})");

    /**
     * For handling '0#' properly
     */
    private static final Pattern decimalFormatFix = Pattern.compile("0+#");

    private static final Pattern multipleSpaces = Pattern.compile(" +");

    /** A positive exponent, which Excel shows with an explicit '+' */
    private static final Pattern exponentWithoutSign = Pattern.compile("E(\\d)");

    private static final Pattern exponent = Pattern.compile("E", Pattern.LITERAL);

    /**
      * Cells formatted with a date or time format and which contain invalid date or time values
     *  show 255 pound signs ("#").
      */
     private static final String invalidDateTimeString;
     static {
         StringBuilder buf = new StringBuilder();
         for(int i = 0; i < 255; i++) buf.append('#');
         invalidDateTimeString = buf.toString();
     }

    /**
     * The decimal symbols of the locale used for formatting values.
     */
    private DecimalFormatSymbols decimalSymbols;

    /**
     * The date symbols of the locale used for formatting values.
     */
    private DateFormatSymbols dateSymbols;

    /**
     * A default date format, if no date format was given
     */
    private DateFormat defaultDateformat;

    /** <em>General</em> format for numbers. */
    private Format generalNumberFormat;

    /** A default format to use when a number pattern cannot be parsed. */
    private Format defaultNumFormat;

    /**
     * A map to cache formats.
     *  Map<String,Format> formats
     */
    private final Map<String,Format> formats = new HashMap<>();

    /** format strings to use in place of the default (en-US) strings of built-in formats, keyed by format index */
    private final Map<Integer,String> builtinFormatOverrides = new HashMap<>();

    /** whether CSV friendly adjustments should be made to the formatted text **/
    private boolean emulateCSV = false;

    /** whether years in dates should be displayed with 4 digits even if the formatString specifies only 2 **/
    private boolean use4DigitYearsInAllDateFormats = false;

    /**
     * if set to true, avoid recalculating the values if there is a cached value available
     * (default is true since POI 6.0.0, was false in earlier releases)
     */
    private boolean useCachedValuesForFormulaCells = true;

    /** stores the locale set by updateLocale method */
    private Locale locale;

    /** stores if the locale should change according to {@link LocaleUtil#getUserLocale()} */
    private boolean localeIsAdapting;

    // contain a support object instead of extending the support class
    private final PropertyChangeSupport pcs;

    /** For logging any problems we find */
    private static final Logger LOG = PoiLogManager.getLogger(DataFormatter.class);

    /**
     * Creates a formatter using the {@link Locale#getDefault() default locale}.
     */
    public DataFormatter() {
        this(false);
    }

    /**
     * Creates a formatter using the {@link Locale#getDefault() default locale}.
     *
     * @param  emulateCSV whether to emulate CSV output.
     */
    public DataFormatter(boolean emulateCSV) {
        this(LocaleUtil.getUserLocale(), true, emulateCSV);
    }

    /**
     * Creates a formatter using the given locale.
     */
    public DataFormatter(Locale locale) {
        this(locale, false);
    }

    /**
     * Creates a formatter using the given locale.
     *
     * @param  emulateCSV whether to emulate CSV output.
     */
    public DataFormatter(Locale locale, boolean emulateCSV) {
        this(locale, false, emulateCSV);
    }

    /**
     * Creates a formatter using the given locale.
     * @param  localeIsAdapting (true only if locale is not user-specified)
     * @param  emulateCSV whether to emulate CSV output.
     */
    public DataFormatter(Locale locale, boolean localeIsAdapting, boolean emulateCSV) {
        this.localeIsAdapting = true;
        pcs = new PropertyChangeSupport(this);
        // localeIsAdapting must be true prior to this first checkForLocaleChange call.
        checkForLocaleChange(locale);
        // set localeIsAdapting so subsequent checks perform correctly
        // (whether a specific locale was provided to this DataFormatter or DataFormatter should
        // adapt to the current user locale as the locale changes)
        this.localeIsAdapting = localeIsAdapting;
        this.emulateCSV = emulateCSV;
    }

    /**
     * @param emulateCSV whether to emulate CSV output (default false).
     * @since 5.2.0
     */
    public void setEmulateCSV(boolean emulateCSV) {
        this.emulateCSV = emulateCSV;
    }

    /**
     * @return whether to emulate CSV output (default false).
     * @since 5.2.0
     */
    public boolean isEmulateCSV() {
        return emulateCSV;
    }

    /**
     * @param useCachedValuesForFormulaCells if set to true, when you do not provide a {@link FormulaEvaluator},
     *                                       for cells with formulas, we will return the cached value for the cell (if available),
     *                                       otherwise - we return the formula itself.
     *                                       Since POI 6.0.0, the default is true, so the cached value is used when it is
     *                                       available. In POI 5.x, the default was false and the formula itself was returned.
     *                                       Set this to false to restore the POI 5.x behaviour.
     * @since 5.2.0
     */
    public void setUseCachedValuesForFormulaCells(boolean useCachedValuesForFormulaCells) {
        this.useCachedValuesForFormulaCells = useCachedValuesForFormulaCells;
    }

    /**
     * @return useCachedValuesForFormulaCells if set to true, when you do not provide a {@link FormulaEvaluator},
     *                                        for cells with formulas, we will return the cached value for the cell (if available),
     *                                        otherwise - we return the formula itself.
     *                                        Since POI 6.0.0, the default is true, so the cached value is used when it is
     *                                        available. In POI 5.x, the default was false and the formula itself was returned.
     * @since 5.2.0
     */
    public boolean useCachedValuesForFormulaCells() {
        return useCachedValuesForFormulaCells;
    }

    /**
     * @param use4DigitYearsInAllDateFormats set to true if you want to have all dates formatted with 4 digit
     *                                       years (even if the format associated with the cell specifies just 2)
     * @since 5.2.0
     */
    public void setUse4DigitYearsInAllDateFormats(boolean use4DigitYearsInAllDateFormats) {
        this.use4DigitYearsInAllDateFormats = use4DigitYearsInAllDateFormats;
    }

    /**
     * @return use4DigitYearsInAllDateFormats set to true if you want to have all dates formatted with 4 digit
     *                                        years (even if the format associated with the cell specifies just 2)
     * @since 5.2.0
     */
    public boolean use4DigitYearsInAllDateFormats() {
        return use4DigitYearsInAllDateFormats;
    }

    /**
     * Return a Format for the given cell if one exists, otherwise try to
     * create one. This method will return {@code null} if any of the
     * following is true:
     * <ul>
     * <li>the cell's style is null</li>
     * <li>the style's data format string is null or empty</li>
     * <li>the format string cannot be recognized as either a number or date</li>
     * </ul>
     *
     * @param cell The cell to retrieve a Format for
     * @return A Format for the format String
     */
    private Format getFormat(Cell cell, ConditionalFormattingEvaluator cfEvaluator) {
        if (cell == null) return null;

        ExcelNumberFormat numFmt = ExcelNumberFormat.from(cell, cfEvaluator);

        if ( numFmt == null) {
            return null;
        }

        int formatIndex = numFmt.getIdx();
        String formatStr = applyBuiltinFormatOverride(formatIndex, numFmt.getFormat());
        if(StringUtil.isBlank(formatStr)) {
            return null;
        }
        return getFormat(cell.getNumericCellValue(), formatIndex, formatStr, isDate1904(cell));
    }

    private boolean isDate1904(Cell cell) {
        if ( cell != null && cell.getSheet().getWorkbook() instanceof Date1904Support date1904Support) {
            return date1904Support.isDate1904();

        }
        return false;
    }

    private Format getFormat(double cellValue, int formatIndex, String formatStrIn, boolean use1904Windowing) {
        if (formatStrIn == null) {
            throw new IllegalArgumentException("Missing input format for value " + cellValue + " and index " + formatIndex);
        }

        checkForLocaleChange();

        // Might be better to separate out the n p and z formats, falling back to p when n and z are not set.
        // That however would require other code to be re factored.
        // String[] formatBits = formatStrIn.split(";");
        // int i = cellValue > 0.0 ? 0 : cellValue < 0.0 ? 1 : 2;
        // String formatStr = (i < formatBits.length) ? formatBits[i] : formatBits[0];

        // this replace is done to fix https://bz.apache.org/bugzilla/show_bug.cgi?id=63211
        String formatStr = convertExclamationEscapes(formatStrIn).replace("\\%", "'%'");

        // Excel supports 2+ part conditional data formats, eg positive/negative/zero,
        //  or (>1000),(>0),(0),(negative). As Java doesn't handle these kinds
        //  of different formats for different ranges, just +ve/-ve, we need to
        //  handle these ourselves in a special way.
        // For now, if we detect 2+ parts, we call out to CellFormat to handle it
        // TODO Going forward, we should really merge the logic between the two classes
        if ((formatStr.contains(";") && formatStr.indexOf(';') != formatStr.lastIndexOf(';'))
                || rangeConditionalPattern.matcher(formatStr).find()) {
            try {
                // Ask CellFormat to get a formatter for it
                CellFormat cfmt = CellFormat.getInstance(locale, formatStr);
                // CellFormat requires callers to identify date vs not, so do so
                // don't try to handle Date value 0, let a 3 or 4-part format take care of it
                Object cellValueO = (cellValue != 0.0 && DateUtil.isADateFormat(formatIndex, formatStr)
                        && DateUtil.isValidExcelDate(cellValue))
                    ? DateUtil.getJavaDate(cellValue, use1904Windowing)
                    : cellValue;
                // Wrap and return (non-cacheable - CellFormat does that)
                return new CellFormatResultWrapper( cfmt.apply(cellValueO) );
            } catch (Exception e) {
                LOG.atWarn().withThrowable(e).log("Formatting failed for format {}, falling back", formatStr);
            }
        }

       // Excel's # with value 0 will output empty where Java will output 0. This hack removes the # from the format.
       if (emulateCSV && cellValue == 0.0 && formatStr.contains("#") && !formatStr.contains("0")) {
           formatStr = formatStr.replace("#", "");
       }

        // A value a date format cannot show (negative, or past 9999-12-31) is displayed by Excel as
        // ########; show it as a plain number. Checked before the cache, so that neither the date
        // format is applied to such a value nor a number format built from a date pattern gets cached.
        // Decided on the format string alone: a conditional format can carry a built-in date index
        // with a number pattern such as "0.00E+00", which must keep formatting as a number.
        if (!DateUtil.isValidExcelDate(cellValue) && DateUtil.isADateFormat(-1, formatStr)) {
            return getDefaultFormat(cellValue);
        }

        // See if we already have it cached
        Format format = formats.get(formatStr);
        if (format != null) {
            return format;
        }

        // Is it one of the special built in types, General or @?
        if (StringUtil.equalsIgnoreCase("General", formatStr) || "@".equals(formatStr)) {
            return generalNumberFormat;
        }

        // Build a formatter, and cache it
        format = createFormat(cellValue, formatIndex, formatStr);
        formats.put(formatStr, format);
        return format;
    }

    /**
     * Create and return a Format based on the format string from a  cell's
     * style. If the pattern cannot be parsed, return a default pattern.
     *
     * @param cell The Excel cell
     * @return A Format representing the excel format. May return null.
     */
    public Format createFormat(Cell cell) {

        int formatIndex = cell.getCellStyle().getDataFormat();
        String formatStr = applyBuiltinFormatOverride(formatIndex, cell.getCellStyle().getDataFormatString());
        return createFormat(cell.getNumericCellValue(), formatIndex, formatStr);
    }

    private Format createFormat(double cellValue, int formatIndex, String sFormat) {
        checkForLocaleChange();

        String formatStr = convertExclamationEscapes(sFormat);

        // Remove colour formatting if present
        if (formatStr != null) {
            Matcher colourM = colorPattern.matcher(formatStr);
            while (colourM.find()) {
                String colour = colourM.group();

                // Paranoid replacement...
                int at = formatStr.indexOf(colour);
                if (at == -1) break;
                String nFormatStr = formatStr.substring(0, at) +
                        formatStr.substring(at + colour.length());
                if (nFormatStr.equals(formatStr)) break;

                // Try again in case there's multiple
                formatStr = nFormatStr;
                colourM = colorPattern.matcher(formatStr);
            }
        }

        // Strip off the numeral style: Arabic digits are shown instead of Chinese/Japanese numerals
        if (formatStr != null) {
            formatStr = dbNumPattern.matcher(formatStr).replaceAll("");
        }

        // Strip off the locale information, we use an instance-wide locale for everything.
        // The locale is optional, e.g. "[$USD]" is just the currency string
        if (formatStr != null) {
            Matcher m = localePatternGroup.matcher(formatStr);
            StringBuilder sb = new StringBuilder(formatStr.length());
            while (m.find()) {
                String match = m.group();
                int dash = match.indexOf('-');
                String symbol = match.substring(2, dash < 0 ? match.length() - 1 : dash);
                // escaped, so that letters and dots in it, e.g. "EUR" or "Bs.", are not taken as part of the pattern
                // (quoted text is not used, as date letters in it still make it count as a date format)
                StringBuilder escaped = new StringBuilder(symbol.length() * 2);
                for (int i = 0; i < symbol.length(); i++) {
                    escaped.append('\\').append(symbol.charAt(i));
                }
                m.appendReplacement(sb, Matcher.quoteReplacement(escaped.toString()));
            }
            m.appendTail(sb);
            formatStr = sb.toString();
        }

        // Check for special cases
        if(StringUtil.isBlank(formatStr)) {
            return getDefaultFormat(cellValue);
        }

        if (StringUtil.equalsIgnoreCase("General", formatStr) || "@".equals(formatStr)) {
           return generalNumberFormat;
        }

        if (formatStr == null) {
            return null;
        }

        if(DateUtil.isADateFormat(formatIndex, formatStr) &&
                DateUtil.isValidExcelDate(cellValue)) {
            return createDateFormat(formatStr, cellValue);
        }
        // Excel supports fractions in format strings, which Java doesn't
        boolean plainFraction = formatStr.contains("#/") || formatStr.contains("?/");
        // FractionFormat strips any text in a fraction, and does not recognise one with text around
        // the '/', but Excel shows the text, e.g. "#\\:#/#" shows 23.75 as "23:3/4", and "#_#=/=#"
        // shows 3.75 as "15 =/=4": let CellFormat handle any fraction with text or spacing in it
        boolean fractionWithText = formatStr.indexOf('/') >= 0 && hasTextInFraction(formatStr);
        if (plainFraction || fractionWithText) {
            if (fractionWithText) {
                try {
                    return new CellFormatWrapper(CellFormat.getInstance(locale, formatStr));
                } catch (Exception e) {
                    LOG.atWarn().withThrowable(e).log("Formatting failed for format {}, falling back", formatStr);
                }
            }
            String[] chunks = formatStr.split(";");
            for (String chunk1 : chunks) {
                String chunk = chunk1.replace("?", "#");
                Matcher matcher = fractionStripper.matcher(chunk);
                chunk = matcher.replaceAll(" ");
                chunk = multipleSpaces.matcher(chunk).replaceAll(" ");
                Matcher fractionMatcher = fractionPattern.matcher(chunk);
                //take the first match
                if (fractionMatcher.find()) {
                    String wholePart = (fractionMatcher.group(1) == null) ? "" : defaultFractionWholePartFormat;
                    return new FractionFormat(wholePart, fractionMatcher.group(3));
                }
            }

            return new FractionFormat(defaultFractionWholePartFormat, defaultFractionFractionPartFormat);
        }

        // DecimalFormat cannot show literal text between digit placeholders, e.g. the
        // escaped dot in "0\\.0," which displays 12345 as "1.2", or after trailing scaling
        // commas, e.g. "#,##0,,\"M\"", so let CellFormat handle it
        // Excel shows a '0' placeholder before '#' or '?' placeholders only in its own position,
        // e.g. "60#########" shows 12345678 as "6012345678", but DecimalFormat does not accept
        // the pattern, so let CellFormat handle it
        if (hasLiteralDecimalFormatCannotShow(formatStr) || hasZeroBeforeOptionalDigit(formatStr)) {
            try {
                CellFormat cellFormat = CellFormat.getInstance(locale, formatStr.replace("'%'", "\\%"));
                // a format CellFormat cannot make sense of, e.g. "#''0#0'1#10L16EE", fails here, not when formatting
                cellFormat.apply(cellValue);
                return new CellFormatWrapper(cellFormat);
            } catch (Exception e) {
                LOG.atWarn().withThrowable(e).log("Formatting failed for format {}, falling back", formatStr);
            }
        }

        if (numPattern.matcher(formatStr).find() || hasUnquotedQuestionMark(formatStr)) {
            return createNumberFormat(formatStr, cellValue);
        }

        if (emulateCSV) {
            return new ConstantStringFormat(cleanFormatForNumber(formatStr));
        }
        // TODO - when does this occur?
        return null;
    }

    String adjustTo4DigitYearsIfConfigured(String format) {
        if (use4DigitYearsInAllDateFormats) {
            int ypos2 = format.indexOf("yy");
            if (ypos2 < 0) {
                return format;
            } else {
                int ypos3 = format.indexOf("yyy");
                int ypos4 = format.indexOf("yyyy");
                if (ypos4 == ypos2) {
                    String part1 = format.substring(0, ypos2 + 4);
                    String part2 = format.substring(ypos2 + 4);
                    return part1 + adjustTo4DigitYearsIfConfigured(part2);
                } else if (ypos3 == ypos2) {
                    return format;
                } else {
                    String part1 = format.substring(0, ypos2 + 2);
                    String part2 = format.substring(ypos2 + 2);
                    return part1 + "yy" + adjustTo4DigitYearsIfConfigured(part2);
                }
            }
        }
        return format;
    }

    private static char toLowerAscii(char c) {
        return c >= 'A' && c <= 'Z' ? (char) (c + ('a' - 'A')) : c;
    }

    /** Pattern to find an elapsed time unit: [h], [mm], [ss] etc. */
    private static final Pattern elapsedTimePattern = Pattern.compile("\\[([hH]+|[mM]+|[sS]+)]");

    private static boolean isElapsedTimeFormat(int formatIndex, String formatString) {
        // only a single section format: the other sections of a format say how to show negative values
        return formatString != null && formatString.indexOf(';') < 0
                && elapsedTimePattern.matcher(formatString).find()
                && DateUtil.isADateFormat(formatIndex, formatString);
    }

    /**
     * Converts Excel's {@code "quoted text"} into SimpleDateFormat's {@code 'quoted text'}, so that
     * the letters in it are not taken as pattern letters and the quote marks are not shown.
     * A {@code "} without a closing {@code "} is left as it is, as is text already in single quotes.
     */
    private static String quoteLiteralText(String formatStr) {
        if (formatStr.indexOf('"') < 0 && formatStr.indexOf('\\') < 0) {
            return formatStr;
        }
        StringBuilder sb = new StringBuilder(formatStr.length() + 2);
        // whether sb ends with a literal that this method quoted, which the next literal can be added to
        boolean inLiteral = false;
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            String literal = null;
            if (c == '"') {
                int end = formatStr.indexOf('"', i + 1);
                if (end > 0) {
                    literal = formatStr.substring(i + 1, end);
                    i = end;
                }
            } else if (c == '\\' && i < formatStr.length() - 1) {
                // an escaped character, e.g. \d
                literal = String.valueOf(formatStr.charAt(++i));
            } else if (c == '\'') {
                int end = formatStr.indexOf('\'', i + 1);
                if (end > 0) {
                    sb.append(formatStr, i, end + 1);
                    i = end;
                    inLiteral = false;
                    continue;
                }
            }
            if (literal == null) {
                sb.append(c);
                inLiteral = false;
            } else if (!literal.isEmpty()) {
                if (inLiteral) {
                    // continue the literal, instead of '' which is a quote character
                    sb.setLength(sb.length() - 1);
                } else {
                    sb.append('\'');
                }
                sb.append(literal.replace("'", "''")).append('\'');
                inLiteral = true;
            }
        }
        return sb.toString();
    }

    /**
     * Applies {@code op} to the parts of a SimpleDateFormat pattern that are not quoted text
     */
    private static String mapUnquoted(String pattern, UnaryOperator<String> op) {
        if (pattern.indexOf('\'') < 0) {
            return op.apply(pattern);
        }
        StringBuilder sb = new StringBuilder(pattern.length());
        int start = 0;
        while (start < pattern.length()) {
            int quote = pattern.indexOf('\'', start);
            if (quote < 0) {
                sb.append(op.apply(pattern.substring(start)));
                break;
            }
            sb.append(op.apply(pattern.substring(start, quote)));
            int end = pattern.indexOf('\'', quote + 1);
            // an unterminated quote is left to SimpleDateFormat to complain about
            end = end < 0 ? pattern.length() - 1 : end;
            sb.append(pattern, quote, end + 1);
            start = end + 1;
        }
        return sb.toString();
    }

    private Format createDateFormat(String pFormatStr, double cellValue) {
        String formatStr = adjustTo4DigitYearsIfConfigured(pFormatStr);
        formatStr = formatStr.replace("\\-","-");
        formatStr = formatStr.replace("\\,",",");
        formatStr = formatStr.replace("\\.","."); // . is a special regexp char
        formatStr = formatStr.replace("\\ "," ");
        formatStr = formatStr.replace("\\/","/"); // weird: m\\/d\\/yyyy
        formatStr = formatStr.replace(";@", "");
        formatStr = formatStr.replace("\"/\"", "/"); // "/" is escaped for no reason in: mm"/"dd"/"yyyy
        formatStr = formatStr.replace("\"\"", "'"); // replace Excel quoting with Java style quoting
        formatStr = formatStr.replace("\\T","'T'"); // Quote the T is iso8601 style dates
        formatStr = quoteLiteralText(formatStr);

        // the AM/PM marker found, if any
        String[] amPm = new String[1];
        formatStr = mapUnquoted(formatStr, part -> {
            if (part.contains(CHINESE_AM_PM)) {
                amPm[0] = CHINESE_AM_PM;
                part = part.replace(CHINESE_AM_PM, AM_PM_PLACEHOLDER);
            }
            Matcher amPmMatcher = amPmPattern.matcher(part);
            while (amPmMatcher.find()) {
                if (amPm[0] == null || amPmMatcher.group().equalsIgnoreCase("A/P")) {
                    amPm[0] = amPmMatcher.group();
                }
            }
            part = amPmPattern.matcher(part).replaceAll(AM_PM_PLACEHOLDER);
            Matcher dateMatcher = daysAsText.matcher(part);
            if (dateMatcher.find()) {
                String match = dateMatcher.group(0).toUpperCase(Locale.ROOT).replace('D', 'E');
                part = dateMatcher.replaceAll(match);
            }
            return part;
        });
        formatStr = formatStr.replace(AM_PM_PLACEHOLDER, "a");
        boolean hasAmPm = amPm[0] != null;
        DateFormatSymbols symbols = dateSymbols;
        if (CHINESE_AM_PM.equals(amPm[0])) {
            // Excel shows the Chinese marker whatever the locale
            symbols = (DateFormatSymbols) dateSymbols.clone();
            symbols.setAmPmStrings(new String[] {"上午", "下午"});
        } else if (hasAmPm && amPm[0].equalsIgnoreCase("A/P")) {
            // A/P shows a single letter, in the case it is written in
            boolean lowerCase = amPm[0].charAt(0) == 'a';
            symbols = (DateFormatSymbols) dateSymbols.clone();
            symbols.setAmPmStrings(lowerCase ? new String[] {"a", "p"} : new String[] {"A", "P"});
        }

        // Convert excel date format to SimpleDateFormat.
        // Excel uses lower and upper case 'm' for both minutes and months.
        // From Excel help:
        /*
            The "m" or "mm" code must appear immediately after the "h" or"hh"
            code or immediately before the "ss" code; otherwise, Microsoft
            Excel displays the month instead of minutes."
          */

        StringBuilder sb = new StringBuilder();
        char[] chars = formatStr.toCharArray();
        boolean mIsMonth = true;
        List<Integer> ms = new ArrayList<>();
        boolean isElapsed = false;
        // the units shown as elapsed time, e.g. 'h' for [h]
        StringBuilder elapsedUnits = new StringBuilder();
        boolean afterSeconds = false;
        for(int j=0; j<chars.length; j++) {
            char c = chars[j];
            if (afterSeconds && c != 's' && c != 'S') {
                afterSeconds = false;
                if (Character.isWhitespace(c)) {
                    mIsMonth = true;
                }
            }
            if (c == '\'') {
                sb.append(c);
                j++;

                // skip until the next quote
                while(j<chars.length) {
                    c = chars[j];
                    sb.append(c);
                    if(c == '\'') {
                        break;
                    }
                    j++;
                }
            }
            else if (c == '[' && !isElapsed) {
                isElapsed = true;
                mIsMonth = false;
                sb.append(c);
            }
            else if (c == ']' && isElapsed) {
                isElapsed = false;
                sb.append(c);
            }
            else if (isElapsed) {
                if (c == 'h' || c == 'H') {
                    sb.append('H');
                    elapsedUnits.append('h');
                }
                else if (c == 'm' || c == 'M') {
                    sb.append('m');
                    elapsedUnits.append('m');
                }
                else if (c == 's' || c == 'S') {
                    sb.append('s');
                    elapsedUnits.append('s');
                }
                else {
                    sb.append(c);
                }
            }
            else if (elapsedUnits.indexOf(String.valueOf(toLowerAscii(c))) >= 0) {
                // once a unit is shown as elapsed time, e.g. [h], Excel shows it that way everywhere
                char unit = toLowerAscii(c);
                int count = 1;
                while (j + 1 < chars.length && toLowerAscii(chars[j + 1]) == unit) {
                    j++;
                    count++;
                }
                String letters = String.valueOf(unit == 'h' ? 'H' : unit).repeat(Math.min(count, 2));
                sb.append('[').append(letters).append(']');
            }
            else if (c == 'h' || c == 'H') {
                mIsMonth = false;
                if (hasAmPm) {
                    sb.append('h');
                } else {
                    sb.append('H');
                }
            }
            else if (c == 'm' || c == 'M') {
                if(mIsMonth) {
                    sb.append('M');
                    ms.add(sb.length() - 1);
                } else {
                    sb.append('m');
                }
            }
            else if (c == 's' || c == 'S') {
                sb.append('s');
                // if 'M' precedes 's' it should be minutes ('m')
                for (int index : ms) {
                    if (sb.charAt(index) == 'M') {
                        sb.replace(index, index + 1, "m");
                    }
                }
                // an 'm' straight after the seconds is minutes too, e.g. "s:m", but not after a space
                mIsMonth = false;
                afterSeconds = true;
                ms.clear();
            }
            // only ASCII letters are pattern letters: others, like the Chinese 时 (hour), are
            // literal text, which does not stop an 'm' after 'h' being minutes
            else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                mIsMonth = true;
                ms.clear();
                if (c == 'y' || c == 'Y') {
                    // y and yy are a 2 digit year, yyy and longer a 4 digit year
                    int count = 1;
                    while (j + 1 < chars.length && (chars[j + 1] == 'y' || chars[j + 1] == 'Y')) {
                        j++;
                        count++;
                    }
                    sb.append(count <= 2 ? "yy" : "yyyy");
                }
                else if (c == 'd' || c == 'D') {
                    sb.append('d');
                }
                else {
                    sb.append(c);
                }
            }
            else {
                if (Character.isWhitespace(c)){
                    ms.clear();
                }
                sb.append(c);
            }
        }
        formatStr = sb.toString();

        try {
            return new ExcelStyleDateFormatter(formatStr, symbols);
        } catch(IllegalArgumentException iae) {
            LOG.atDebug().withThrowable(iae).log("Formatting failed for format {}, falling back", formatStr);
            // the pattern could not be parsed correctly,
            // so fall back to the default number format
            return getDefaultFormat(cellValue);
        }

    }

    /**
     * Excel treats {@code !} like {@code \}: it displays the next character literally.
     * Rewrite such escapes as {@code \}, which the rest of the formatting code understands.
     * Quoted text, bracketed sections and the operands of {@code _} and {@code *} are left alone.
     */
    private static String convertExclamationEscapes(String formatStr) {
        if (formatStr == null || formatStr.indexOf('!') < 0) {
            return formatStr;
        }
        StringBuilder sb = new StringBuilder(formatStr.length());
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            if (c == '"' || c == '[') {
                int end = formatStr.indexOf(c == '"' ? '"' : ']', i + 1);
                end = end < 0 ? formatStr.length() - 1 : end;
                sb.append(formatStr, i, end + 1);
                i = end;
            } else if ((c == '\\' || c == '_' || c == '*') && i < formatStr.length() - 1) {
                sb.append(c).append(formatStr.charAt(++i));
            } else if (c == '!' && i < formatStr.length() - 1) {
                sb.append('\\').append(formatStr.charAt(++i));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Whether any section of the number format has quoted or escaped text with a digit
     * placeholder both before and after it, e.g. {@code 0\.0} or {@code 0"-"00}, or
     * after trailing scaling commas, e.g. {@code 0.0,"K"} or {@code #,##0.0,","}.
     */
    private static boolean hasLiteralDecimalFormatCannotShow(String formatStr) {
        boolean seenDigit = false;
        boolean literalAfterDigit = false;
        boolean commaAfterDigit = false;
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            switch (c) {
                case '"' -> {
                    int end = formatStr.indexOf('"', i + 1);
                    i = end < 0 ? formatStr.length() : end;
                    if (commaAfterDigit) {
                        return true;
                    }
                    literalAfterDigit |= seenDigit;
                }
                case '[' -> {
                    int end = formatStr.indexOf(']', i + 1);
                    i = end < 0 ? formatStr.length() : end;
                }
                case '\\' -> {
                    i++;
                    if (commaAfterDigit) {
                        return true;
                    }
                    literalAfterDigit |= seenDigit;
                }
                case ',' -> commaAfterDigit |= seenDigit;
                case '_', '*' -> i++;
                case ';' -> {
                    seenDigit = false;
                    literalAfterDigit = false;
                    commaAfterDigit = false;
                }
                case '0', '#', '?' -> {
                    if (literalAfterDigit) {
                        return true;
                    }
                    seenDigit = true;
                    // a comma followed by a digit placeholder is a grouping separator, not a scaler
                    commaAfterDigit = false;
                }
                default -> {
                    // other characters don't affect the result
                }
            }
        }
        return false;
    }

    /**
     * Whether the integer part of any section of the number format has a {@code 0} placeholder
     * followed by a {@code #} or {@code ?} placeholder, e.g. {@code 0##} or {@code 60#########}.
     */
    private static boolean hasZeroBeforeOptionalDigit(String formatStr) {
        boolean seenZero = false;
        boolean inInteger = true;
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            switch (c) {
                case '"' -> {
                    int end = formatStr.indexOf('"', i + 1);
                    i = end < 0 ? formatStr.length() : end;
                }
                case '[' -> {
                    int end = formatStr.indexOf(']', i + 1);
                    i = end < 0 ? formatStr.length() : end;
                }
                case '\\', '_', '*' -> i++;
                case ';' -> {
                    seenZero = false;
                    inInteger = true;
                }
                case '.', 'E', 'e' -> inInteger = false;
                case '0' -> seenZero |= inInteger;
                case '#', '?' -> {
                    if (seenZero && inInteger) {
                        return true;
                    }
                }
                default -> {
                    // other characters don't affect the result
                }
            }
        }
        return false;
    }

    private String cleanFormatForNumber(String formatStrIn) {
        // this replace is done to fix https://bz.apache.org/bugzilla/show_bug.cgi?id=63211
        String formatStr = formatStrIn.replace("\\%", "'%'");

        StringBuilder sb = new StringBuilder(formatStr);

        if (emulateCSV) {
            // Requested spacers with "_" are replaced by a single space.
            // Full-column-width padding "*" are removed.
            // Not processing fractions at this time. Replace ? with space.
            // This matches CSV output.
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '_' || c == '*' || c == '?') {
                    if (i > 0 && sb.charAt((i - 1)) == '\\') {
                        // It's escaped, don't worry
                        continue;
                    }
                    if (c == '?') {
                        sb.setCharAt(i, ' ');
                    } else if (i < sb.length() - 1) {
                        // Remove the character we're supposed
                        //  to match the space of / pad to the
                        //  column width with
                        if (c == '_') {
                            sb.setCharAt(i + 1, ' ');
                        } else {
                            sb.deleteCharAt(i + 1);
                        }
                        // Remove the character too
                        sb.deleteCharAt(i);
                        i--;
                    }
                }
            }
        } else {
            // If they requested spacers, with "_",
            //  remove those as we don't do spacing
            // If they requested full-column-width
            //  padding, with "*", remove those too
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '_' || c == '*') {
                    if (i > 0 && sb.charAt((i - 1)) == '\\') {
                        // It's escaped, don't worry
                        continue;
                    }
                    if (i < sb.length() - 1) {
                        // Remove the character we're supposed
                        //  to match the space of / pad to the
                        //  column width with
                        sb.deleteCharAt(i + 1);
                    }
                    // Remove the _ too
                    sb.deleteCharAt(i);
                    i--;
                }
            }
        }

        // Now, handle the other aspects like
        //  quoting and scientific notation
        for(int i = 0; i < sb.length(); i++) {
           char c = sb.charAt(i);
            // remove quotes and back slashes
            if (c == '\\' || c == '"') {
                sb.deleteCharAt(i);
                i--;

            // for scientific/engineering notation
            } else if ((c == '+' || c == '-') && i > 0 && sb.charAt(i - 1) == 'E') {
                sb.deleteCharAt(i);
                i--;
            }
        }

        return sb.toString();
    }

    /** characters that DecimalFormat does not show as they are, unless they are quoted */
    private static final String DECIMAL_FORMAT_SPECIALS = "0#.,;%‰E¤";

    /**
     * Quotes the characters in Excel's {@code "quoted text"} and {@code \}-escaped characters
     * that DecimalFormat would otherwise treat as part of the pattern, e.g. the dot in {@code 0.00"x."}:
     * a second decimal point makes the pattern invalid, or is shown as the locale's decimal separator.
     */
    private static String quoteSpecialLiterals(String formatStr) {
        if (formatStr.indexOf('"') < 0 && formatStr.indexOf('\\') < 0) {
            return formatStr;
        }
        StringBuilder sb = new StringBuilder(formatStr.length() + 8);
        boolean inExcelQuotes = false;
        boolean inPatternQuotes = false;
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            boolean literal = inExcelQuotes;
            if (c == '"') {
                inExcelQuotes = !inExcelQuotes;
                sb.append(c);
                continue;
            } else if (!inExcelQuotes && (c == '_' || c == '*') && i < formatStr.length() - 1) {
                // spacing and padding: the operand is removed later
                if (inPatternQuotes) {
                    sb.append('\'');
                    inPatternQuotes = false;
                }
                sb.append(c).append(formatStr.charAt(++i));
                continue;
            } else if (!inExcelQuotes && c == '\\' && i < formatStr.length() - 1) {
                sb.append(c);
                c = formatStr.charAt(++i);
                literal = true;
            }
            boolean special = literal && DECIMAL_FORMAT_SPECIALS.indexOf(c) >= 0;
            if (special != inPatternQuotes) {
                sb.append('\'');
                inPatternQuotes = special;
            }
            // a quote character is written as two, in and out of DecimalFormat's quoted text
            sb.append(literal && c == '\'' ? "''" : String.valueOf(c));
        }
        if (inPatternQuotes) {
            sb.append('\'');
        }
        return sb.toString();
    }

    private static class InternalDecimalFormatWithScale extends Format {

        private static final Pattern endsWithCommas = Pattern.compile("(,+)$");
        private final BigDecimal divider;
        private static final BigDecimal ONE_THOUSAND = BigDecimal.valueOf(1000);
        private final DecimalFormat df;
        /** the integer and fraction digit placeholders of the positive section, e.g. "??0" and "0?" */
        private final String integerPlaceholders;
        private final String fractionPlaceholders;
        private final boolean padsWithSpaces;
        private static String trimTrailingCommas(String s) {
            return endsWithCommas.matcher(s).replaceAll("");
        }

        public InternalDecimalFormatWithScale(String pattern, DecimalFormatSymbols symbols) {
            // '?' is a digit placeholder that shows a space for a missing digit:
            // DecimalFormat shows nothing for it, as for '#', and format() adds the spaces
            StringBuilder integers = new StringBuilder();
            StringBuilder fractions = new StringBuilder();
            boolean quoted = false;
            boolean inFraction = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                } else if (!quoted) {
                    if (c == ';' || c == 'E') {
                        break;
                    } else if (c == '.') {
                        inFraction = true;
                    } else if (c == '0' || c == '#' || c == '?') {
                        (inFraction ? fractions : integers).append(c);
                    }
                }
            }
            integerPlaceholders = integers.toString();
            fractionPlaceholders = fractions.toString();
            padsWithSpaces = pattern.indexOf('?') >= 0;
            if (padsWithSpaces) {
                pattern = pattern.replace('?', '#');
            }
            // each '%' multiplies by 100, but DecimalFormat only allows one
            int percents = countUnquoted(pattern, '%');
            if (percents > 1) {
                pattern = quoteUnquoted(pattern, '%');
            }
            df = new DecimalFormat(trimTrailingCommas(pattern), symbols);
            if (percents > 1) {
                df.setMultiplier((int) Math.pow(100, Math.min(percents, 4)));
            }
            setExcelStyleRoundingMode(df);
            // Excel shows the decimal point even when there are no digits after it, e.g. "#.#" shows 1 as "1."
            if (hasDecimalPoint(pattern)) {
                df.setDecimalSeparatorAlwaysShown(true);
            }
            Matcher endsWithCommasMatcher = endsWithCommas.matcher(pattern);
            if (endsWithCommasMatcher.find()) {
                String commas = (endsWithCommasMatcher.group(1));
                BigDecimal temp = BigDecimal.ONE;
                for (int i = 0; i < commas.length(); ++i) {
                    temp = temp.multiply(ONE_THOUSAND);
                }
                divider = temp;
            } else {
                divider = null;
            }
        }

        private static int countUnquoted(String pattern, char ch) {
            int count = 0;
            boolean quoted = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                } else if (!quoted && c == ch) {
                    count++;
                }
            }
            return count;
        }

        /** quotes each run of unquoted {@code ch}, so that DecimalFormat shows them as text */
        private static String quoteUnquoted(String pattern, char ch) {
            StringBuilder sb = new StringBuilder(pattern.length() + 8);
            boolean quoted = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                    sb.append(c);
                } else if (!quoted && c == ch) {
                    int end = i;
                    while (end < pattern.length() && pattern.charAt(end) == ch) {
                        end++;
                    }
                    sb.append('\'').append(pattern, i, end).append('\'');
                    i = end - 1;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        /** whether the positive section of the DecimalFormat pattern has a decimal point, outside quoted text */
        private static boolean hasDecimalPoint(String pattern) {
            boolean quoted = false;
            for (int i = 0; i < pattern.length(); i++) {
                char c = pattern.charAt(i);
                if (c == '\'') {
                    quoted = !quoted;
                } else if (!quoted) {
                    if (c == '.') {
                        return true;
                    } else if (c == ';' || c == 'E') {
                        return false;
                    }
                }
            }
            return false;
        }

        boolean requiresScaling() {
            return divider != null;
        }

        private Object scaleInput(Object obj) {
            if (divider != null) {
                if (obj instanceof BigDecimal bd) {
                    // dividing by a power of ten is exact, so keep all the digits until formatting
                    obj = bd.divide(divider);
                } else if (obj instanceof Double d) {
                    obj = d / divider.doubleValue();
                } else {
                    throw new UnsupportedOperationException("cannot scaleInput of type " + obj.getClass());
                }
            }
            return obj;
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            obj = scaleInput(obj);
            if (!padsWithSpaces) {
                return df.format(obj, toAppendTo, pos);
            }
            return toAppendTo.append(padWithSpaces(df.format(obj)));
        }

        /**
         * Adds a space for each '?' placeholder that has no digit: before the integer digits
         * (along with the grouping separators that the missing digits would have had) and after the fraction digits
         */
        private String padWithSpaces(String formatted) {
            DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
            char decimalSeparator = symbols.getDecimalSeparator();
            char groupingSeparator = symbols.getGroupingSeparator();
            int start = 0;
            while (start < formatted.length() && !Character.isDigit(formatted.charAt(start))
                    && formatted.charAt(start) != decimalSeparator) {
                start++;
            }
            int end = start;
            int integerDigits = 0;
            while (end < formatted.length()
                    && (Character.isDigit(formatted.charAt(end)) || formatted.charAt(end) == groupingSeparator)) {
                if (Character.isDigit(formatted.charAt(end))) {
                    integerDigits++;
                }
                end++;
            }
            int fractionEnd = end;
            if (fractionEnd < formatted.length() && formatted.charAt(fractionEnd) == decimalSeparator) {
                fractionEnd++;
                while (fractionEnd < formatted.length() && Character.isDigit(formatted.charAt(fractionEnd))) {
                    fractionEnd++;
                }
            }
            int fractionDigits = Math.max(0, fractionEnd - end - 1);

            int missingIntegers = integerPlaceholders.length() - integerDigits;
            int leadingSpaces = 0;
            if (missingIntegers > 0) {
                String missing = integerPlaceholders.substring(0, missingIntegers);
                if (missing.chars().allMatch(c -> c == '?')) {
                    leadingSpaces = groupedWidth(integerPlaceholders.length()) - groupedWidth(integerDigits);
                } else {
                    leadingSpaces = (int) missing.chars().filter(c -> c == '?').count();
                }
            }
            int missingFractions = fractionPlaceholders.length() - fractionDigits;
            int trailingSpaces = missingFractions <= 0 ? 0 : (int) fractionPlaceholders
                    .substring(fractionDigits).chars().filter(c -> c == '?').count();

            return formatted.substring(0, start) + " ".repeat(leadingSpaces) + formatted.substring(start, fractionEnd)
                    + " ".repeat(trailingSpaces) + formatted.substring(fractionEnd);
        }

        private int groupedWidth(int digits) {
            return digits <= 0 ? 0 : digits + (df.isGroupingUsed() && df.getGroupingSize() > 0
                    ? (digits - 1) / df.getGroupingSize() : 0);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Whether a single section fraction format has literal text or spacing (escaped, quoted, or {@code _x})
     * between its first and last digit placeholders, e.g. {@code #\\:#/#}
     */
    private static boolean hasTextInFraction(String formatStr) {
        if (formatStr.indexOf(';') >= 0) {
            return false;
        }
        int first = -1;
        int last = -1;
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            if (c == '0' || c == '#' || c == '?') {
                first = first < 0 ? i : first;
                last = i;
            }
        }
        for (int i = first + 1; i < last; i++) {
            char c = formatStr.charAt(i);
            if (c == '\\' || c == '"' || c == '_' || c == '*') {
                return true;
            }
        }
        return false;
    }

    /** whether the format has a '?' digit placeholder, i.e. a '?' that is not quoted or escaped */
    private static boolean hasUnquotedQuestionMark(String formatStr) {
        boolean quoted = false;
        for (int i = 0; i < formatStr.length(); i++) {
            char c = formatStr.charAt(i);
            if (c == '"') {
                quoted = !quoted;
            } else if (!quoted && c == '\\') {
                i++;
            } else if (!quoted && c == '?') {
                return true;
            }
        }
        return false;
    }

    /**
     * Rewrites the commas of a number format the way DecimalFormat needs them. In Excel, a comma between
     * digit placeholders turns on grouping by thousands, wherever it is: "0,00" shows 1234567 as "1,234,567".
     * Commas straight after the integer digits scale the value by 1000, before a decimal point
     * as well as at the end ("#,.#," shows 1234567 as "1.2"), and a comma before the digits is shown as it is.
     */
    private static String normalizeGrouping(String format) {
        if (format.indexOf(',') < 0) {
            return format;
        }
        String[] sections = format.split(";", -1);
        for (int i = 0; i < sections.length; i++) {
            sections[i] = normalizeSectionGrouping(sections[i]);
        }
        return String.join(";", sections);
    }

    private static String normalizeSectionGrouping(String section) {
        int start = -1;
        for (int i = 0; i < section.length() && start < 0; i++) {
            char c = section.charAt(i);
            if (c == '0' || c == '#' || c == '?') {
                start = i;
            } else if (c == '\'') {
                // quoted text: leave it to DecimalFormat as before
                return section;
            }
        }
        if (start < 0) {
            return section;
        }
        int end = start;
        while (end < section.length() && "0#?,".indexOf(section.charAt(end)) >= 0) {
            end++;
        }
        String integerPart = section.substring(start, end);
        int scalingCommas = 0;
        while (integerPart.endsWith(",")) {
            integerPart = integerPart.substring(0, integerPart.length() - 1);
            scalingCommas++;
        }
        String suffix = section.substring(end);
        boolean beforeDecimalPoint = suffix.startsWith(".");
        if (!beforeDecimalPoint && suffix.indexOf(',') >= 0) {
            // commas after the digits, e.g. in a literal: not something we can handle here
            return section;
        }
        String digits = integerPart.replace(",", "");
        if (digits.length() < integerPart.length()) {
            // a comma between digit placeholders means grouping by thousands
            StringBuilder sb = new StringBuilder(digits);
            while (sb.length() < 4) {
                sb.insert(0, '#');
            }
            integerPart = sb.insert(sb.length() - 3, ',').toString();
        }
        StringBuilder sb = new StringBuilder();
        // a comma before the digits is a literal
        sb.append(section.substring(0, start).replace(",", "','"));
        sb.append(integerPart);
        if (beforeDecimalPoint) {
            // move the scaling commas to the end, where InternalDecimalFormatWithScale looks for them
            sb.append(suffix);
            sb.append(",".repeat(scalingCommas));
        } else {
            sb.append(",".repeat(scalingCommas));
            sb.append(suffix);
        }
        return sb.toString();
    }

    private Format createNumberFormat(String formatStr, double cellValue) {
        String format = cleanFormatForNumber(quoteSpecialLiterals(formatStr));
        DecimalFormatSymbols symbols = decimalSymbols;

        // Do we need to change the grouping character?
        // eg for a format like #'##0 which wants 12'345 not 12,345
        Matcher agm = alternateGrouping.matcher(format);
        if (agm.find()) {
            char grouping = agm.group(2).charAt(0);
            // Only replace the grouping character if it is not the default
            // grouping character for the US locale (',') in order to enable
            // correct grouping for non-US locales.
            if (grouping!=',') {
                symbols = DecimalFormatSymbols.getInstance(locale);

                symbols.setGroupingSeparator(grouping);
                String oldPart = agm.group(1);
                String newPart = oldPart.replace(grouping, ',');
                format = format.replace(oldPart, newPart);
            }
        }

        format = normalizeGrouping(format);

        // Excel ignores leading zeros, but Java fails with an exception below
        if (decimalFormatFix.matcher(format).matches()) {
            format = "#";
        }

        try {
            return new InternalDecimalFormatWithScale(format, symbols);
        } catch(IllegalArgumentException iae) {
            LOG.atDebug().withThrowable(iae).log("Formatting failed for format {}, falling back", formatStr);
            // the pattern could not be parsed correctly,
            // so fall back to the default number format
            return getDefaultFormat(cellValue);
        }
    }

    /**
     * Returns a default format for a cell.
     * @param cell The cell
     * @return a default format
     */
    public Format getDefaultFormat(Cell cell) {
        return getDefaultFormat(cell.getNumericCellValue());
    }
    private Format getDefaultFormat(double cellValue) {
        checkForLocaleChange();

        // for numeric cells try user supplied default
        if (defaultNumFormat != null) {
            return defaultNumFormat;

          // otherwise use general format
        }
        return generalNumberFormat;
    }

    /**
     * Performs Excel-style date formatting, using the
     *  supplied Date and format
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private String performDateFormatting(Date d, double excelValue, Format dateFormat) {
        Format df = dateFormat != null ? dateFormat : defaultDateformat;
        synchronized (df) {
            try {
                return df.format(d);
            } catch (IllegalArgumentException e) {
                if (df instanceof DateFormat) {
                    throw e;
                }
                // a date format that could not be parsed falls back to a number format
                return df.format(excelValue);
            }
        }
    }

    /**
     * Returns the formatted value of an Excel date as a {@code String} based
     * on the cell's {@code DataFormat}. i.e. "Thursday, January 02, 2003"
     * , "01/02/2003" , "02-Jan" , etc.
     * <p>
     * If any conditional format rules apply, the highest priority with a number format is used.
     * If no rules contain a number format, or no rules apply, the cell's style format is used.
     * If the style does not have a format, the default date format is applied.
     *
     * @param cell to format
     * @param cfEvaluator ConditionalFormattingEvaluator (if available)
     * @return Formatted value
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private String getFormattedDateString(Cell cell, ConditionalFormattingEvaluator cfEvaluator) {
        if (cell == null) {
            return null;
        }
        Format dateFormat = getFormat(cell, cfEvaluator);
        if (dateFormat == null) {
            if (defaultDateformat == null) {
                DateFormatSymbols sym = DateFormatSymbols.getInstance(LocaleUtil.getUserLocale());
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", sym);
                sdf.setTimeZone(LocaleUtil.getUserTimeZone());
                dateFormat = sdf;
            } else {
                dateFormat = defaultDateformat;
            }
        }
        synchronized (dateFormat) {
            if(dateFormat instanceof ExcelStyleDateFormatter formatter) {
                // Hint about the raw excel value
                formatter.setDateToBeFormatted(cell.getNumericCellValue());
            }
            Date d = cell.getDateCellValue();
            return performDateFormatting(d, cell.getNumericCellValue(), dateFormat);
        }
    }

    /**
     * Returns the formatted value of an Excel number as a {@code String}
     * based on the cell's {@code DataFormat}. Supported formats include
     * currency, percents, decimals, phone number, SSN, etc.:
     * "61.54%", "$100.00", "(800) 555-1234".
     * <p>
     * Format comes from either the highest priority conditional format rule with a
     * specified format, or from the cell style.
     *
     * @param cell The cell
     * @param cfEvaluator if available, or null
     * @return a formatted number string
     */
    private String getFormattedNumberString(Cell cell, ConditionalFormattingEvaluator cfEvaluator) {
        if (cell == null) {
            return null;
        }
        Format numberFormat = getFormat(cell, cfEvaluator);
        double d = cell.getNumericCellValue();
        if (numberFormat == null) {
            return Double.toString(d);
        }
        String formatted = null;
        if (numberFormat instanceof InternalDecimalFormatWithScale idfws) {
            if (idfws.requiresScaling()) {
                // hack for https://bz.apache.org/bugzilla/show_bug.cgi?id=69812
                // the https://github.com/apache/poi/pull/321 hack causes problems here
                formatted = idfws.format(d);
            }
        }
        if (formatted == null) {
            try {
                //see https://github.com/apache/poi/pull/321 -- but this sometimes fails, thus the catch and retry
                formatted = numberFormat.format(BigDecimal.valueOf(d));
            } catch (NumberFormatException nfe) {
                formatted = numberFormat.format(d);
            }
        }
        return exponentWithoutSign.matcher(formatted).replaceFirst("E+$1"); // to match Excel's E-notation
    }

    /**
     * Formats the given raw cell value, based on the supplied
     *  format index and string, according to excel style rules.
     * @see #formatCellValue(Cell)
     */
    public String formatRawCellContents(double value, int formatIndex, String formatString) {
        return formatRawCellContents(value, formatIndex, formatString, false);
    }
    /**
     * Formats the given raw cell value, based on the supplied
     *  format index and string, according to excel style rules.
     * @see #formatCellValue(Cell)
     */
    public String formatRawCellContents(double value, int formatIndex, String formatStringIn, boolean use1904Windowing) {
        checkForLocaleChange();

        String formatString = applyBuiltinFormatOverride(formatIndex, formatStringIn);

        // Excel shows a negative elapsed time, e.g. for [h]:mm, with a minus sign
        if (value < 0 && isElapsedTimeFormat(formatIndex, formatString)) {
            return "-" + formatRawCellContents(-value, formatIndex, formatString, use1904Windowing);
        }

        // Is it a date?
        if(DateUtil.isADateFormat(formatIndex,formatString)) {
            if(DateUtil.isValidExcelDate(value)) {
                Format dateFormat = getFormat(value, formatIndex, formatString, use1904Windowing);
                if(dateFormat instanceof ExcelStyleDateFormatter formatter) {
                    // Hint about the raw excel value
                    formatter.setDateToBeFormatted(value);
                }
                Date d = DateUtil.getJavaDate(value, use1904Windowing);
                return performDateFormatting(d, value, dateFormat);
            }
            // RK: Invalid dates are 255 #s.
            if (emulateCSV) {
                return invalidDateTimeString;
            }
        }

        // else Number
        Format numberFormat = getFormat(value, formatIndex, formatString, use1904Windowing);
        if (numberFormat == null) {
            return String.valueOf(value);
        }

        // When formatting 'value', double to text to BigDecimal produces more
        // accurate results than double to Double in JDK8 (as compared to
        // previous versions). However, if the value contains E notation, this
        // would expand the values, which we do not want, so revert to
        // original method.
        String result;
        final String textValue = NumberToTextConverter.toText(value);
        if (textValue.indexOf('E') > -1) {
            result = numberFormat.format(value);
        }
        else {
            result = numberFormat.format(new BigDecimal(textValue));
        }

        // If they requested a non-abbreviated Scientific format,
        //  and there's an E## (but not E-##), add the missing '+' for E+##
        String fslc = formatString.toLowerCase(Locale.ROOT);
        if ((fslc.contains("general") || fslc.contains("e+0") || numberFormat == generalNumberFormat)
                && result.contains("E") && !result.contains("E-")) {
            result = exponent.matcher(result).replaceFirst("E+");
        }
        return result;
    }

    /**
     * <p>
     * Returns the formatted value of a cell as a {@code String} regardless
     * of the cell type. If the Excel format pattern cannot be parsed then the
     * cell value will be formatted using a default format.
     * </p>
     * <p>When passed a null or blank cell, this method will return an empty
     * String (""). Formulas in formula type cells will not be evaluated.
     * {@link #setUseCachedValuesForFormulaCells} controls how these cells are evaluated.
     * Since POI 6.0.0, that setting defaults to true, so the cached value of a formula cell is
     * returned when one is available (POI 5.x returned the formula itself by default).
     * </p>
     *
     * @param cell The cell
     * @return the formatted cell value as a String
     * @see #setUseCachedValuesForFormulaCells(boolean)
     * @see #formatCellValue(Cell, FormulaEvaluator)
     * @see #formatCellValue(Cell, FormulaEvaluator, ConditionalFormattingEvaluator)
     */
    public String formatCellValue(Cell cell) {
        return formatCellValue(cell, null);
    }

    /**
     * <p>
     * Returns the formatted value of a cell as a {@code String} regardless
     * of the cell type. If the Excel number format pattern cannot be parsed then the
     * cell value will be formatted using a default format.
     * </p>
     * <p>When passed a null or blank cell, this method will return an empty
     * String (""). Formula cells will be evaluated using the given
     * {@link FormulaEvaluator} if the evaluator is non-null. If the
     * evaluator is null, then the formula String will be returned. The caller
     * is responsible for setting the currentRow on the evaluator.
     *</p>
     *
     * @param cell The cell (can be null)
     * @param evaluator The FormulaEvaluator (can be null)
     * @return a string value of the cell
     * @see #formatCellValue(Cell)
     * @see #formatCellValue(Cell, FormulaEvaluator, ConditionalFormattingEvaluator)
     */
    public String formatCellValue(Cell cell, FormulaEvaluator evaluator) {
        return formatCellValue(cell, evaluator, null);
    }

    /**
     * <p>
     * Returns the formatted value of a cell as a {@code String} regardless
     * of the cell type. If the Excel number format pattern cannot be parsed then the
     * cell value will be formatted using a default format.
     * </p>
     * <p>When passed a null or blank cell, this method will return an empty
     * String (""). Formula cells will be evaluated using the given
     * {@link FormulaEvaluator} if the evaluator is non-null. If the
     * evaluator is null, then the formula String will be returned. The caller
     * is responsible for setting the currentRow on the evaluator
     *</p>
     * <p>
     * When a ConditionalFormattingEvaluator is present, it is checked first to see
     * if there is a number format to apply.  If multiple rules apply, the last one is used.
     * If no ConditionalFormattingEvaluator is present, no rules apply, or the applied
     * rules do not define a format, the cell's style format is used.
     * </p>
     * <p>
     * The two evaluators should be from the same context, to avoid inconsistencies in cached values.
     *</p>
     *
     * @param cell The cell (can be null)
     * @param evaluator The FormulaEvaluator (can be null)
     * @param cfEvaluator ConditionalFormattingEvaluator (can be null)
     * @return a string value of the cell
     * @see #formatCellValue(Cell)
     * @see #formatCellValue(Cell, FormulaEvaluator)
     */
    public String formatCellValue(Cell cell, FormulaEvaluator evaluator, ConditionalFormattingEvaluator cfEvaluator) {
        checkForLocaleChange();

        if (cell == null) {
            return "";
        }

        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            if (evaluator == null) {
                if (useCachedValuesForFormulaCells) {
                    try {
                        cellType = cell.getCachedFormulaResultType();
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                } else {
                    return cell.getCellFormula();
                }
            } else {
                cellType = evaluator.evaluateFormulaCell(cell);
            }
        }
        switch (cellType) {
            case NUMERIC :

                if (cfEvaluator == null && cell.getNumericCellValue() < 0 && cell.getCellStyle() != null
                        && isElapsedTimeFormat(cell.getCellStyle().getDataFormat(), cell.getCellStyle().getDataFormatString())) {
                    return formatRawCellContents(cell.getNumericCellValue(), cell.getCellStyle().getDataFormat(),
                            cell.getCellStyle().getDataFormatString(), isDate1904(cell));
                }
                if (DateUtil.isCellDateFormatted(cell, cfEvaluator)) {
                    return getFormattedDateString(cell, cfEvaluator);
                }
                return getFormattedNumberString(cell, cfEvaluator);

            case STRING :
                return cell.getRichStringCellValue().getString();

            case BOOLEAN :
                return cell.getBooleanCellValue() ? "TRUE" : "FALSE";
            case BLANK :
                return "";
            case ERROR:
                return FormulaError.forInt(cell.getErrorCellValue()).getString();
            default:
                throw new IllegalStateException("Unexpected celltype (" + cellType + ")");
        }
    }


    /**
     * <p>
     * Sets a default number format to be used when the Excel format cannot be
     * parsed successfully. <b>Note:</b> This is a fall back for when an error
     * occurs while parsing an Excel number format pattern. This will not
     * affect cells with the <em>General</em> format.
     * </p>
     * <p>
     * The value that will be passed to the Format's format method (specified
     * by {@code java.text.Format#format}) will be a double value from a
     * numeric cell. Therefore the code in the format method should expect a
     * {@code Number} value.
     * </p>
     *
     * @param format A Format instance to be used as a default
     * @see Format#format
     */
    public void setDefaultNumberFormat(Format format) {
        for (Map.Entry<String, Format> entry : formats.entrySet()) {
            if (entry.getValue() == generalNumberFormat) {
                entry.setValue(format);
            }
        }
        defaultNumFormat = format;
    }

    /**
     * Adds a new format to the available formats.
     * <p>
     * The value that will be passed to the Format's format method (specified
     * by {@code java.text.Format#format}) will be a double value from a
     * numeric cell. Therefore the code in the format method should expect a
     * {@code Number} value.
     * </p>
     * @param excelFormatStr The data format string
     * @param format A Format instance
     */
    public void addFormat(String excelFormatStr, Format format) {
        formats.put(excelFormatStr, format);
    }

    /**
     * Sets the format string to use for a built-in format, in place of the default (en-US) one
     * from {@link BuiltinFormats}.
     * <p>
     * Excel does not store the format string of most built-in formats in a workbook, just the
     * format index, and it displays some of them (for example 37-40) differently depending on
     * the locale of the Excel installation. POI only knows the en-US versions, so for example
     * index 40 is formatted as {@code #,##0.00_);[Red](#,##0.00)}, whereas German Excel shows it
     * as <code>#,##0.00 _&#8364;;[Red]-#,##0.00 _&#8364;</code>. Use this method to supply the format string
     * that matches the locale the workbook was made for.
     * </p>
     * <p>
     * The override only applies when the format string of the cell is the default one for that
     * index, so a format string that the workbook explicitly stores for the index is still used.
     * </p>
     *
     * @param formatIndex index of a built-in format, see {@link BuiltinFormats}
     * @param formatString the format string to use for the index, or {@code null} to remove an override
     * @throws IllegalArgumentException if {@code formatIndex} is not the index of a built-in format
     * @since 6.0.0
     */
    public void setBuiltinFormatOverride(int formatIndex, String formatString) {
        if (BuiltinFormats.getBuiltinFormat(formatIndex) == null) {
            throw new IllegalArgumentException("Not a built-in format index: " + formatIndex);
        }
        if (formatString == null) {
            builtinFormatOverrides.remove(formatIndex);
        } else {
            builtinFormatOverrides.put(formatIndex, formatString);
        }
    }

    /**
     * @param formatIndex index of a built-in format, see {@link BuiltinFormats}
     * @return the format string set with {@link #setBuiltinFormatOverride(int, String)} for the index,
     * or {@code null} if there is none
     * @since 6.0.0
     */
    public String getBuiltinFormatOverride(int formatIndex) {
        return builtinFormatOverrides.get(formatIndex);
    }

    private String applyBuiltinFormatOverride(int formatIndex, String formatString) {
        if (builtinFormatOverrides.isEmpty()) {
            return formatString;
        }
        String override = builtinFormatOverrides.get(formatIndex);
        if (override != null && formatString != null
                && formatString.equals(BuiltinFormats.getBuiltinFormat(formatIndex))) {
            return override;
        }
        return formatString;
    }

    // Some custom formats

    /**
     * @return a {@code DecimalFormat} with parseIntegerOnly set {@code true}
     */
    private static DecimalFormat createIntegerOnlyFormat(String fmt) {
        DecimalFormatSymbols dsf = DecimalFormatSymbols.getInstance(Locale.ROOT);
        DecimalFormat result = new DecimalFormat(fmt, dsf);
        result.setParseIntegerOnly(true);
        return result;
    }

    /**
     * Enables excel style rounding mode (round half up) on the
     *  Decimal Format given.
     */
    public static void setExcelStyleRoundingMode(DecimalFormat format) {
        setExcelStyleRoundingMode(format, RoundingMode.HALF_UP);
    }

    /**
     * Enables custom rounding mode on the given Decimal Format.
     * @param format DecimalFormat
     * @param roundingMode RoundingMode
     */
    public static void setExcelStyleRoundingMode(DecimalFormat format, RoundingMode roundingMode) {
       format.setRoundingMode(roundingMode);
    }

    /**
     * If the Locale has been changed via {@link LocaleUtil#setUserLocale(Locale)} the stored
     * formats need to be refreshed. All formats which aren't originated from DataFormatter
     * itself, i.e. all Formats added via {@link DataFormatter#addFormat(String, Format)} and
     * {@link DataFormatter#setDefaultNumberFormat(Format)}, need to be added again.
     * To notify callers, the returned {@link PropertyChangeSupport} should be used.
     * The Locale in {@link #updateLocale(Locale)} is the new Locale.
     *
     * @return the listener object, where callers can register themselves
     */
    public PropertyChangeSupport getLocaleChangedObservable() {
        return pcs;
    }

    private void checkForLocaleChange() {
        checkForLocaleChange(LocaleUtil.getUserLocale());
    }

    private void checkForLocaleChange(Locale newLocale) {
        if (!localeIsAdapting) return;
        if (newLocale.equals(locale)) return;
        updateLocale(newLocale);
        pcs.firePropertyChange("locale", locale, newLocale);
    }

    /**
     * Update formats when locale has been changed
     *
     * @param newLocale the new locale
     */
    public void updateLocale(Locale newLocale) {
        if (!localeIsAdapting || newLocale.equals(locale)) return;

        locale = newLocale;

        dateSymbols = DateFormatSymbols.getInstance(locale);
        decimalSymbols = DecimalFormatSymbols.getInstance(locale);
        generalNumberFormat = new ExcelGeneralNumberFormat(locale);

        // taken from Date.toString()
        defaultDateformat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", dateSymbols);
        defaultDateformat.setTimeZone(LocaleUtil.getUserTimeZone());

        // init built-in formats

        formats.clear();
        Format zipFormat = ZipPlusFourFormat.instance;
        addFormat("00000\\-0000", zipFormat);
        addFormat("00000-0000", zipFormat);

        Format phoneFormat = PhoneFormat.instance;
        // allow for format string variations
        addFormat("[<=9999999]###\\-####;\\(###\\)\\ ###\\-####", phoneFormat);
        addFormat("[<=9999999]###-####;(###) ###-####", phoneFormat);
        addFormat("###\\-####;\\(###\\)\\ ###\\-####", phoneFormat);
        addFormat("###-####;(###) ###-####", phoneFormat);

        Format ssnFormat = SSNFormat.instance;
        addFormat("000\\-00\\-0000", ssnFormat);
        addFormat("000-00-0000", ssnFormat);
    }

    /**
     * Format class for Excel's SSN format. This class mimics Excel's built-in
     * SSN formatting.
     */
    @SuppressWarnings("serial")
   private static final class SSNFormat extends Format {
        public static final Format instance = new SSNFormat();
        private static final DecimalFormat df = createIntegerOnlyFormat("000000000");
        private SSNFormat() {
            // enforce singleton
        }

        /** Format a number as an SSN */
        public static String format(Number num) {
            String result = df.format(num);
            return result.substring(0, 3) + '-' +
                    result.substring(3, 5) + '-' +
                    result.substring(5, 9);
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(format((Number)obj));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }

    /**
     * Format class for Excel Zip + 4 format. This class mimics Excel's
     * built-in formatting for Zip + 4.
     */
    @SuppressWarnings("serial")
   private static final class ZipPlusFourFormat extends Format {
        public static final Format instance = new ZipPlusFourFormat();
        private static final DecimalFormat df = createIntegerOnlyFormat("000000000");
        private ZipPlusFourFormat() {
            // enforce singleton
        }

        /** Format a number as Zip + 4 */
        public static String format(Number num) {
            String result = df.format(num);
            return result.substring(0, 5) + '-' +
                    result.substring(5, 9);
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(format((Number)obj));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }

    /**
     * Format class for Excel phone number format. This class mimics Excel's
     * built-in phone number formatting.
     */
    @SuppressWarnings("serial")
   private static final class PhoneFormat extends Format {
        public static final Format instance = new PhoneFormat();
        private static final DecimalFormat df = createIntegerOnlyFormat("##########");
        private PhoneFormat() {
            // enforce singleton
        }

        /** Format a number as a phone number */
        public static String format(Number num) {
            String result = df.format(num);
            StringBuilder sb = new StringBuilder();
            String seg1, seg2, seg3;
            int len = result.length();
            if (len <= 4) {
                return result;
            }

            seg3 = result.substring(len - 4, len);
            seg2 = result.substring(Math.max(0, len - 7), len - 4);
            seg1 = result.substring(Math.max(0, len - 10), Math.max(0, len - 7));

            if(StringUtil.isNotBlank(seg1)) {
                sb.append('(').append(seg1).append(") ");
            }
            if(StringUtil.isNotBlank(seg2)) {
                sb.append(seg2).append('-');
            }
            sb.append(seg3);
            return sb.toString();
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(format((Number)obj));
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }




    /**
     * Format class that does nothing and always returns a constant string.
     *
     * This format is used to simulate Excel's handling of a format string
     * of all # when the value is 0. Excel will output "", Java will output "0".
     *
     * @see DataFormatter#createFormat(double, int, String)
     */
    @SuppressWarnings("serial")
   private static final class ConstantStringFormat extends Format {
        private static final DecimalFormat df = createIntegerOnlyFormat("##########");
        private final String str;
        public ConstantStringFormat(String s) {
            str = s;
        }

        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            return toAppendTo.append(str);
        }

        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return df.parseObject(source, pos);
        }
    }
    /**
     * Cachable wrapper around a {@link CellFormat}, used for number formats that DecimalFormat cannot handle
     */
    @SuppressWarnings("serial")
    private final class CellFormatWrapper extends Format {
        private final CellFormat cellFormat;
        private CellFormatWrapper(CellFormat cellFormat) {
            this.cellFormat = cellFormat;
        }
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            String text = cellFormat.apply(obj).text;
            return toAppendTo.append(emulateCSV ? text : text.trim());
        }
        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null; // Not supported
        }
    }

    /**
     * Workaround until we merge {@link DataFormatter} with {@link CellFormat}.
     * Constant, non-cachable wrapper around a {@link CellFormatResult}
     */
    @SuppressWarnings("serial")
    private final class CellFormatResultWrapper extends Format {
        private final CellFormatResult result;
        private CellFormatResultWrapper(CellFormatResult result) {
            this.result = result;
        }
        @Override
        public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
            if (emulateCSV) {
                return toAppendTo.append(result.text);
            } else {
                return toAppendTo.append(result.text.trim());
            }
        }
        @Override
        public Object parseObject(String source, ParsePosition pos) {
            return null; // Not supported
        }
    }
}
