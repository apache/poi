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
package org.apache.poi.xssf.usermodel;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.model.StylesTable;

/**
 * Handles data formats for XSSF.
 * 
 * Per Microsoft Excel 2007+ format limitations:
 * Workbooks support between 200 and 250 "number formats"
 * (POI calls them "data formats") So short or even byte
 * would be acceptable data types to use for referring to
 * data format indices.
 * https://support.office.com/en-us/article/excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
 * 
 */
public class XSSFDataFormat implements DataFormat {
    private final StylesTable stylesSource;

    protected XSSFDataFormat(StylesTable stylesSource) {
        this.stylesSource = stylesSource;
    }

    /**
     * Get the format index that matches the given format
     *  string, creating a new format entry if required.
     * Aliases text to the proper format as required.
     *
     * @param format string matching a built-in format
     * @return index of format.
     */
    @Override
    public short getFormat(String format) {
        int idx = BuiltinFormats.getBuiltinFormat(format);
        if(idx == -1) idx = stylesSource.putNumberFormat(format);
        return (short)idx;
    }

    /**
     * get the format string that matches the given format index
     * @param index of a format
     * @return string represented at index of format or <code>null</code> if there is not a  format at that index
     */
    @Override
    public String getFormat(short index) {
        // Indices used for built-in formats may be overridden with
        // custom formats, such as locale-specific currency.
        // See org.apache.poi.xssf.usermodel.TestXSSFDataFormat#test49928() 
        // or bug 49928 for an example.
        // This is why we need to check stylesSource first and only fall back to
        // BuiltinFormats if the format hasn't been overridden.
        String fmt = stylesSource.getNumberFormatAt(index);
        if(fmt == null) fmt = BuiltinFormats.getBuiltinFormat(index);
        return fmt;
    }
    
    /**
     * Add a number format with a specific ID into the number format style table.
     * If a format with the same ID already exists, overwrite the format code
     * with <code>fmt</code>
     * This may be used to override built-in number formats.
     *
     * @param index the number format ID
     * @param format the number format code
     */
    public void putFormat(short index, String format) {
        stylesSource.putNumberFormat(index, format);
    }
}
