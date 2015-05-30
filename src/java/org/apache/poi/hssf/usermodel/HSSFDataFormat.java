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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.poi.hssf.model.InternalWorkbook;
import org.apache.poi.hssf.record.FormatRecord;
import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormat;

/**
 * Identifies both built-in and user defined formats within a workbook.
 * <p/>
 * See {@link BuiltinFormats} for a list of supported built-in formats.
 * <p/>
 * <b>International Formats</b><br/>
 * Since version 2003 Excel has supported international formats.  These are denoted
 * with a prefix "[$-xxx]" (where xxx is a 1-7 digit hexadecimal number).
 * See the Microsoft article
 * <a href="http://office.microsoft.com/assistance/hfws.aspx?AssetID=HA010346351033&CTT=6&Origin=EC010272491033">
 * Creating international number formats
 * </a> for more details on these codes.
 */
public final class HSSFDataFormat implements DataFormat {

    private static final String[] builtinFormats = BuiltinFormats.getAll();

    private final Vector<String> formats = new Vector<String>();
    private final InternalWorkbook workbook;
    // Flag to see if need to check the built in list
    // or if the regular list has all entries.
    private boolean movedBuiltins = false;

    /**
     * Constructs a new data formatter.
     * It takes a workbook to have access to the workbooks format records.
     *
     * @param workbook the workbook the formats are tied to.
     */
    HSSFDataFormat(InternalWorkbook workbook) {
        this.workbook = workbook;

        Iterator<FormatRecord> i = workbook.getFormats().iterator();
        while (i.hasNext()) {
            FormatRecord r = i.next();
            ensureFormatsSize(r.getIndexCode());
            formats.set(r.getIndexCode(), r.getFormatString());
        }
    }

    public static List<String> getBuiltinFormats() {
        return Arrays.asList(builtinFormats);
    }

    /**
     * Gets the format index that matches the given format string.
     * Automatically converts "text" to Excel's format string to represent text.
     *
     * @param format string matching a built in format
     * @return index of format or -1 if undefined.
     */
    public static short getBuiltinFormat(String format) {
        return (short) BuiltinFormats.getBuiltinFormat(format);
    }

    /**
     * Gets the format index that matches the given format string,
     * creating a new format entry if required.
     * Aliases text to the proper format as required.
     *
     * @param pFormat string matching a built in format
     * @return index of format.
     */
    public short getFormat(String pFormat) {
        // Normalise the format string
        String format;
        if (pFormat.toUpperCase().equals("TEXT")) {
            format = "@";
        } else {
            format = pFormat;
        }

        // Merge in the built in formats if we haven't already
        if (!movedBuiltins) {
            for (int i = 0; i < builtinFormats.length; i++) {
                ensureFormatsSize(i);
                if (formats.get(i) == null) {
                    formats.set(i, builtinFormats[i]);
                } else {
                    // The workbook overrides this default format
                }
            }
            movedBuiltins = true;
        }

        // See if we can find it
        for (int i = 0; i < formats.size(); i++) {
            if (format.equals(formats.get(i))) {
                return (short) i;
            }
        }

        // We can't find it, so add it as a new one
        short index = workbook.getFormat(format, true);
        ensureFormatsSize(index);
        formats.set(index, format);
        return index;
    }

    /**
     * Gets the format string that matches the given format index.
     *
     * @param index of a format
     * @return string represented at index of format or null if there is not a  format at that index
     */
    public String getFormat(short index) {
        if (movedBuiltins) {
            return formats.get(index);
        }

        if (index == -1) {
            // YK: formatIndex can be -1, for example, for cell in column Y in test-data/spreadsheet/45322.xls
            // return null for those
            return null;
        }

        String fmt = formats.size() > index ? formats.get(index) : null;
        if (builtinFormats.length > index && builtinFormats[index] != null) {
            // It's in the built in range
            if (fmt != null) {
                // It's been overriden, use that value
                return fmt;
            } else {
                // Standard built in format
                return builtinFormats[index];
            }
        }
        return fmt;
    }

    /**
     * Gets the format string that matches the given format index.
     *
     * @param index of a built-in format
     * @return string represented at index of format or null if there is not a builtin format at that index
     */
    public static String getBuiltinFormat(short index) {
        return BuiltinFormats.getBuiltinFormat(index);
    }

    /**
     * Gets the number of built-in and reserved builtinFormats.
     *
     * @return number of built-in and reserved builtinFormats
     */
    public static int getNumberOfBuiltinBuiltinFormats() {
        return builtinFormats.length;
    }

    /**
     * Ensures that the formats list can hold entries
     * up to and including the entry with this index.
     */
    private void ensureFormatsSize(int index) {
        if (formats.size() <= index) {
            formats.setSize(index + 1);
        }
    }
}
