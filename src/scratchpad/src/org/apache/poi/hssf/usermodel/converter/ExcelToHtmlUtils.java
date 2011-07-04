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
package org.apache.poi.hssf.usermodel.converter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.util.IOUtils;

public class ExcelToHtmlUtils
{
    static final String EMPTY = "";

    public static String getBorderStyle( short xlsBorder )
    {
        final String borderStyle;
        switch ( xlsBorder )
        {
        case HSSFCellStyle.BORDER_NONE:
            borderStyle = "none";
            break;
        case HSSFCellStyle.BORDER_DASH_DOT:
        case HSSFCellStyle.BORDER_DASH_DOT_DOT:
        case HSSFCellStyle.BORDER_DOTTED:
        case HSSFCellStyle.BORDER_HAIR:
        case HSSFCellStyle.BORDER_MEDIUM_DASH_DOT:
        case HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
        case HSSFCellStyle.BORDER_SLANTED_DASH_DOT:
            borderStyle = "dotted";
            break;
        case HSSFCellStyle.BORDER_DASHED:
        case HSSFCellStyle.BORDER_MEDIUM_DASHED:
            borderStyle = "dashed";
            break;
        case HSSFCellStyle.BORDER_DOUBLE:
            borderStyle = "double";
            break;
        default:
            borderStyle = "solid";
            break;
        }
        return borderStyle;
    }

    public static String getBorderWidth( short xlsBorder )
    {
        final String borderWidth;
        switch ( xlsBorder )
        {
        case HSSFCellStyle.BORDER_MEDIUM_DASH_DOT:
        case HSSFCellStyle.BORDER_MEDIUM_DASH_DOT_DOT:
        case HSSFCellStyle.BORDER_MEDIUM_DASHED:
            borderWidth = "2pt";
            break;
        case HSSFCellStyle.BORDER_THICK:
            borderWidth = "thick";
            break;
        default:
            borderWidth = "thin";
            break;
        }
        return borderWidth;
    }

    public static String getColor( HSSFColor color )
    {
        StringBuilder stringBuilder = new StringBuilder();
        for ( short s : color.getTriplet() )
        {
            if ( s < 10 )
                stringBuilder.append( '0' );

            stringBuilder.append( Integer.toHexString( s ) );
        }
        return stringBuilder.toString();
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
