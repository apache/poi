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

import java.text.AttributedCharacterIterator;
import java.text.CharacterIterator;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Matcher;

import org.apache.poi.util.LocaleUtil;
import org.apache.poi.util.StringUtil;

/**
 * Formats a date value.
 */
public class CellDateFormatter extends CellFormatter {
    private boolean amPmUpper;
    private boolean showM;
    private boolean showAmPm;
    private final DateFormat dateFmt;
    private String sFmt;

    private static final Calendar EXCEL_EPOCH_CAL =
            LocaleUtil.getLocaleCalendar(1904, 0, 1);

    private static final int NUM_MILLISECONDS_IN_DAY = 1000 * 60 * 60 * 24;

    private static CellDateFormatter SIMPLE_DATE_FORMATTER;

    class DatePartHandler implements CellFormatPart.PartHandler {
        private int mStart = -1;
        private int mLen;
        private int hStart = -1;
        private int hLen;

        @Override
        public String handlePart(Matcher m, String part, CellFormatType type,
                                 StringBuffer desc) {

            int pos = desc.length();
            char firstCh = part.charAt(0);
            switch (firstCh) {
                case 's':
                case 'S':
                    if (mStart >= 0) {
                        for (int i = 0; i < mLen; i++)
                            desc.setCharAt(mStart + i, 'm');
                        mStart = -1;
                    }
                    return part.toLowerCase(Locale.ROOT);

                case 'h':
                case 'H':
                    mStart = -1;
                    hStart = pos;
                    hLen = part.length();
                    return part.toLowerCase(Locale.ROOT);

                case 'd':
                case 'D':
                    mStart = -1;
                    if (part.length() <= 2)
                        return part.toLowerCase(Locale.ROOT);
                    else
                        return part.toLowerCase(Locale.ROOT).replace('d', 'E');

                case 'm':
                case 'M':
                    mStart = pos;
                    mLen = part.length();
                    // For 'm' after 'h', output minutes ('m') not month ('M')
                    if (hStart >= 0)
                        return part.toLowerCase(Locale.ROOT);
                    else
                        return part.toUpperCase(Locale.ROOT);

                case 'y':
                case 'Y':
                    mStart = -1;
                    // See https://issues.apache.org/bugzilla/show_bug.cgi?id=53369
                    if (part.length() == 1)
                        part = "yy";
                    else if (part.length() == 3)
                        part = "yyyy";
                    return part.toLowerCase(Locale.ROOT);

                case '0':
                    mStart = -1;
                    int sLen = part.length();
                    sFmt = "%0" + (sLen + 2) + "." + sLen + "f";
                    return part.replace('0', 'S');

                case 'a':
                case 'A':
                case 'p':
                case 'P':
                    if (part.length() > 1) {
                        // am/pm marker
                        mStart = -1;
                        showAmPm = true;
                        showM = StringUtil.toLowerCase(part.charAt(1)).equals("m");
                        // For some reason "am/pm" becomes AM or PM, but "a/p" becomes a or p
                        amPmUpper = showM || StringUtil.isUpperCase(part.charAt(0));

                        return "a";
                    }
                    //noinspection fallthrough

                default:
                    return null;
            }
        }

        public void updatePositions(int pos, int offset) {
            if (pos < hStart) {
                hStart += offset;
            }
            if (pos < mStart) {
                mStart += offset;
            }
        }

        public void finish(StringBuffer toAppendTo) {
            if (hStart >= 0 && !showAmPm) {
                for (int i = 0; i < hLen; i++) {
                    toAppendTo.setCharAt(hStart + i, 'H');
                }
            }
        }
    }

    /**
     * Creates a new date formatter with the given specification.
     *
     * @param format The format.
     */
    public CellDateFormatter(String format) {
        this(LocaleUtil.getUserLocale(), format);
    }

    /**
     * Creates a new date formatter with the given specification.
     *
     * @param locale The locale.
     * @param format The format.
     */
    public CellDateFormatter(Locale locale, String format) {
        super(format);
        DatePartHandler partHandler = new DatePartHandler();
        StringBuffer descBuf = CellFormatPart.parseFormat(format,
                CellFormatType.DATE, partHandler);
        partHandler.finish(descBuf);
        dateFmt = new SimpleDateFormat(descBuf.toString(), locale);
        dateFmt.setTimeZone(LocaleUtil.getUserTimeZone());
    }

    @Override
    public synchronized void formatValue(StringBuffer toAppendTo, Object value) {
        if (value == null)
            value = 0.0;
        if (value instanceof Number) {
            Number num = (Number) value;
            // Convert from fractional days to milliseconds. Excel always rounds up.
            double v = Math.round(num.doubleValue() * NUM_MILLISECONDS_IN_DAY);
            if (v == 0L) {
                value = EXCEL_EPOCH_CAL.getTime();
            } else {
                Calendar c = (Calendar)EXCEL_EPOCH_CAL.clone();
                // If milliseconds were not requested in the format string, round the seconds.
                int seconds = (int) (sFmt == null ? Math.round(v / 1000) : v / 1000);
                c.add(Calendar.SECOND, seconds);
                c.add(Calendar.MILLISECOND, (int)(v % 1000));
                value = c.getTime();
            }
        }

        AttributedCharacterIterator it = dateFmt.formatToCharacterIterator(value);
        boolean doneAm = false;
        boolean doneMillis = false;

        for (char ch = it.first();
             ch != CharacterIterator.DONE;
             ch = it.next()) {
            if (it.getAttribute(DateFormat.Field.MILLISECOND) != null) {
                if (!doneMillis) {
                    Date dateObj = (Date) value;
                    int pos = toAppendTo.length();
                    try (Formatter formatter = new Formatter(toAppendTo, Locale.ROOT)) {
                        long msecs = dateObj.getTime() % 1000;
                        if (msecs < 0) {
                            msecs += 1000;
                        }
                        formatter.format(locale, sFmt, msecs / 1000.0);
                    }
                    toAppendTo.delete(pos, pos + 2);
                    doneMillis = true;
                }
            } else if (it.getAttribute(DateFormat.Field.AM_PM) != null) {
                if (!doneAm) {
                    if (showAmPm) {
                        if (amPmUpper) {
                            toAppendTo.append(StringUtil.toUpperCase(ch));
                            if (showM)
                                toAppendTo.append('M');
                        } else {
                            toAppendTo.append(StringUtil.toLowerCase(ch));
                            if (showM)
                                toAppendTo.append('m');
                        }
                    }
                    doneAm = true;
                }
            } else {
                toAppendTo.append(ch);
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * For a date, this is {@code "mm/d/y"}.
     */
    @Override
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        CellDateFormatter cellDateFormatter = SIMPLE_DATE_FORMATTER;
        if (cellDateFormatter == null) {
            synchronized (CellDateFormatter.class) {
                cellDateFormatter = SIMPLE_DATE_FORMATTER;
                if (cellDateFormatter == null) {
                    SIMPLE_DATE_FORMATTER = cellDateFormatter = new CellDateFormatter("mm/d/y");
                }
            }
        }
        cellDateFormatter.formatValue(toAppendTo, value);
    }
}
