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

import org.apache.poi.ss.format.CellFormatPart.PartHandler;

import java.util.regex.Matcher;

/**
 * This class implements printing out text.
 *
 * @author Ken Arnold, Industrious Media LLC
 */
public class CellTextFormatter extends CellFormatter {
    private final int[] textPos;
    private final String desc;

    static final CellFormatter SIMPLE_TEXT = new CellTextFormatter("@");

    public CellTextFormatter(String format) {
        super(format);

        final int[] numPlaces = new int[1];

        desc = CellFormatPart.parseFormat(format, CellFormatType.TEXT,
                new PartHandler() {
                    public String handlePart(Matcher m, String part,
                            CellFormatType type, StringBuffer desc) {
                        if (part.equals("@")) {
                            numPlaces[0]++;
                            return "\u0000";
                        }
                        return null;
                    }
                }).toString();

        // Remember the "@" positions in last-to-first order (to make insertion easier)
        textPos = new int[numPlaces[0]];
        int pos = desc.length() - 1;
        for (int i = 0; i < textPos.length; i++) {
            textPos[i] = desc.lastIndexOf("\u0000", pos);
            pos = textPos[i] - 1;
        }
    }

    /** {@inheritDoc} */
    public void formatValue(StringBuffer toAppendTo, Object obj) {
        int start = toAppendTo.length();
        String text = obj.toString();
        toAppendTo.append(desc);
        for (int i = 0; i < textPos.length; i++) {
            int pos = start + textPos[i];
            toAppendTo.replace(pos, pos + 1, text);
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * For text, this is just printing the text.
     */
    public void simpleValue(StringBuffer toAppendTo, Object value) {
        SIMPLE_TEXT.formatValue(toAppendTo, value);
    }
}