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
import java.util.Formatter;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class implements printing out an elapsed time format.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class CellElapsedFormatter extends CellFormatter {
    private final List<TimeSpec> specs;
    private TimeSpec topmost;
    private final String printfFmt;

    private static final Pattern PERCENTS = Pattern.compile("%");

    private static final double HOUR__FACTOR = 1.0 / 24.0;
    private static final double MIN__FACTOR = HOUR__FACTOR / 60.0;
    private static final double SEC__FACTOR = MIN__FACTOR / 60.0;

    private static class TimeSpec {
        final char type;
        final int pos;
        final int len;
        final double factor;
        double modBy;

        public TimeSpec(char type, int pos, int len, double factor) {
            this.type = type;
            this.pos = pos;
            this.len = len;
            this.factor = factor;
            modBy = 0;
        }

        public long valueFor(double elapsed) {
            double val;
            if (modBy == 0)
                val = elapsed / factor;
            else
                val = elapsed / factor % modBy;
            if (type == '0')
                return Math.round(val);
            else
                return (long) val;
        }
    }

    private class ElapsedPartHandler implements CellFormatPart.PartHandler {
        // This is the one class that's directly using printf, so it can't use
        // the default handling for quoted strings and special characters.  The
        // only special character for this is '%', so we have to handle all the
        // quoting in this method ourselves.

        public String handlePart(Matcher m, String part, CellFormatType type,
                StringBuffer desc) {

            int pos = desc.length();
            char firstCh = part.charAt(0);
            switch (firstCh) {
            case '[':
                if (part.length() < 3)
                    break;
                if (topmost != null)
                    throw new IllegalArgumentException(
                            "Duplicate '[' times in format");
                part = part.toLowerCase();
                int specLen = part.length() - 2;
                topmost = assignSpec(part.charAt(1), pos, specLen);
                return part.substring(1, 1 + specLen);

            case 'h':
            case 'm':
            case 's':
            case '0':
                part = part.toLowerCase();
                assignSpec(part.charAt(0), pos, part.length());
                return part;

            case '\n':
                return "%n";

            case '\"':
                part = part.substring(1, part.length() - 1);
                break;

            case '\\':
                part = part.substring(1);
                break;

            case '*':
                if (part.length() > 1)
                    part = CellFormatPart.expandChar(part);
                break;

            // An escape we can let it handle because it can't have a '%'
            case '_':
                return null;
            }
            // Replace ever "%" with a "%%" so we can use printf
            return PERCENTS.matcher(part).replaceAll("%%");
        }
    }

    /**
     * Creates a elapsed time formatter.
     *
     * @param pattern The pattern to parse.
     */
    public CellElapsedFormatter(String pattern) {
        super(pattern);

        specs = new ArrayList<TimeSpec>();

        StringBuffer desc = CellFormatPart.parseFormat(pattern,
                CellFormatType.ELAPSED, new ElapsedPartHandler());

        ListIterator<TimeSpec> it = specs.listIterator(specs.size());
        while (it.hasPrevious()) {
            TimeSpec spec = it.previous();
            desc.replace(spec.pos, spec.pos + spec.len, "%0" + spec.len + "d");
            if (spec.type != topmost.type) {
                spec.modBy = modFor(spec.type, spec.len);
            }
        }

        printfFmt = desc.toString();
    }

    private TimeSpec assignSpec(char type, int pos, int len) {
        TimeSpec spec = new TimeSpec(type, pos, len, factorFor(type, len));
        specs.add(spec);
        return spec;
    }

    private static double factorFor(char type, int len) {
        switch (type) {
        case 'h':
            return HOUR__FACTOR;
        case 'm':
            return MIN__FACTOR;
        case 's':
            return SEC__FACTOR;
        case '0':
            return SEC__FACTOR / Math.pow(10, len);
        default:
            throw new IllegalArgumentException(
                    "Uknown elapsed time spec: " + type);
        }
    }

    private static double modFor(char type, int len) {
        switch (type) {
        case 'h':
            return 24;
        case 'm':
            return 60;
        case 's':
            return 60;
        case '0':
            return Math.pow(10, len);
        default:
            throw new IllegalArgumentException(
                    "Uknown elapsed time spec: " + type);
        }
    }

    /** {@inheritDoc} */
    public void formatValue(StringBuffer toAppendTo, Object value) {
        double elapsed = ((Number) value).doubleValue();

        if (elapsed < 0) {
            toAppendTo.append('-');
            elapsed = -elapsed;
        }

        Object[] parts = new Long[specs.size()];
        for (int i = 0; i < specs.size(); i++) {
            parts[i] = specs.get(i).valueFor(elapsed);
        }

        Formatter formatter = new Formatter(toAppendTo);
        formatter.format(printfFmt, parts);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For a date, this is <tt>"mm/d/y"</tt>.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        formatValue(toAppendTo, value);
    }
}