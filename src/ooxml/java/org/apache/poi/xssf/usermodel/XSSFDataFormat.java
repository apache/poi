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
 */
public class XSSFDataFormat implements DataFormat {
    private StylesTable stylesSource;

    protected XSSFDataFormat(StylesTable stylesSource) {
        this.stylesSource = stylesSource;
    }

    /**
     * Get the format index that matches the given format
     *  string, creating a new format entry if required.
     * Aliases text to the proper format as required.
     *
     * @param format string matching a built in format
     * @return index of format.
     */
    public short getFormat(String format) {
        int idx = BuiltinFormats.getBuiltinFormat(format);
        if(idx == -1) idx = stylesSource.putNumberFormat(format);
        return (short)idx;
    }

    /**
     * get the format string that matches the given format index
     * @param index of a format
     * @return string represented at index of format or null if there is not a  format at that index
     */
    public String getFormat(short index) {
        String fmt = BuiltinFormats.getBuiltinFormat(index);
        if(fmt == null) fmt = stylesSource.getNumberFormatAt(index);
        return fmt;
    }
}
