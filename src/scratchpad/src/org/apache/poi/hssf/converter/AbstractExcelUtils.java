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
package org.apache.poi.hssf.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.converter.AbstractWordUtils;
import org.apache.poi.util.Beta;
import org.apache.poi.util.IOUtils;

/**
 * Common class for {@link ExcelToFoUtils} and {@link ExcelToHtmlUtils}
 * 
 * @author Sergey Vladimirov (vlsergey {at} gmail {dot} com)
 * @see AbstractWordUtils
 */
@Beta
public class AbstractExcelUtils
{
    static final String EMPTY = "";
    private static final short EXCEL_COLUMN_WIDTH_FACTOR = 256;
    private static final int UNIT_OFFSET_LENGTH = 7;

    /**
     * See <a href=
     * "http://apache-poi.1045710.n5.nabble.com/Excel-Column-Width-Unit-Converter-pixels-excel-column-width-units-td2301481.html"
     * >here</a> for Xio explanation and details
     */
    public static int getColumnWidthInPx( int widthUnits )
    {
        int pixels = ( widthUnits / EXCEL_COLUMN_WIDTH_FACTOR )
                * UNIT_OFFSET_LENGTH;

        int offsetWidthUnits = widthUnits % EXCEL_COLUMN_WIDTH_FACTOR;
        pixels += Math.round( offsetWidthUnits
                / ( (float) EXCEL_COLUMN_WIDTH_FACTOR / UNIT_OFFSET_LENGTH ) );

        return pixels;
    }

    static boolean isEmpty( String str )
    {
        return str == null || str.length() == 0;
    }

    static boolean isNotEmpty( String str )
    {
        return !isEmpty( str );
    }

    public static HSSFWorkbook loadXls( File xlsFile ) throws IOException
    {
        final FileInputStream inputStream = new FileInputStream( xlsFile );
        try
        {
            return new HSSFWorkbook( inputStream );
        }
        finally
        {
            IOUtils.closeQuietly( inputStream );
        }
    }

}
