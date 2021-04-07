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

    private final Calendar EXCEL_EPOCH_CAL =
        LocaleUtil.getLocaleCalendar(1904, 0, 1);

    private static /* final */ CellDateFormatter SIMPLE_DATE;

    private class DatePartHandler implements CellFormatPart.PartHandler {
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
                if (part.length() == 3)
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
        // tweak the format pattern to pass tests on JDK 1.7,
        // See https://issues.apache.org/bugzilla/show_bug.cgi?id=53369
        String ptrn = descBuf.toString().replaceAll("((y)(?!y))(?<!yy)", "yy");
        dateFmt = new SimpleDateFormat(ptrn, locale);
        dateFmt.setTimeZone(LocaleUtil.getUserTimeZone());
    }

    /** {@inheritDoc} */
    public synchronized void formatValue(StringBuffer toAppendTo, Object value) {
        if (value == null)
            value = 0.0;
        if (value instanceof Number) {
            Number num = (Number) value;
            long v = num.longValue();
            if (v == 0L) {
                value = EXCEL_EPOCH_CAL.getTime();
            } else {
                Calendar c = (Calendar)EXCEL_EPOCH_CAL.clone();
                c.add(Calendar.SECOND, (int)(v / 1000));
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
     * For a date, this is <tt>"mm/d/y"</tt>.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        synchronized (CellDateFormatter.class) {
            if (SIMPLE_DATE == null || !SIMPLE_DATE.EXCEL_EPOCH_CAL.equals(EXCEL_EPOCH_CAL)) {
                SIMPLE_DATE = new CellDateFormatter("mm/d/y");
            }
        }
        SIMPLE_DATE.formatValue(toAppendTo, value);
    }
}
