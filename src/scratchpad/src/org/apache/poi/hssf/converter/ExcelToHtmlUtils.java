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

import java.util.Arrays;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Beta;

@Beta
public class ExcelToHtmlUtils extends AbstractExcelUtils
{
    public static void appendAlign( StringBuilder style, short alignment )
    {
        String cssAlign = getAlign( alignment );
        if ( isEmpty( cssAlign ) )
            return;

        style.append( "text-align:" );
        style.append( cssAlign );
        style.append( ";" );
    }

    /**
     * Creates a map (i.e. two-dimensional array) filled with ranges. Allow fast
     * retrieving {@link CellRangeAddress} of any cell, if cell is contained in
     * range.
     * 
     * @see #getMergedRange(CellRangeAddress[][], int, int)
     */
    public static CellRangeAddress[][] buildMergedRangesMap( HSSFSheet sheet )
    {
        CellRangeAddress[][] mergedRanges = new CellRangeAddress[1][];
        for ( int m = 0; m < sheet.getNumMergedRegions(); m++ )
        {
            final CellRangeAddress cellRangeAddress = sheet.getMergedRegion( m );

            final int requiredHeight = cellRangeAddress.getLastRow() + 1;
            if ( mergedRanges.length < requiredHeight )
            {
                CellRangeAddress[][] newArray = new CellRangeAddress[requiredHeight][];
                System.arraycopy( mergedRanges, 0, newArray, 0,
                        mergedRanges.length );
                mergedRanges = newArray;
            }

            for ( int r = cellRangeAddress.getFirstRow(); r <= cellRangeAddress
                    .getLastRow(); r++ )
            {
                final int requiredWidth = cellRangeAddress.getLastColumn() + 1;

                CellRangeAddress[] rowMerged = mergedRanges[r];
                if ( rowMerged == null )
                {
                    rowMerged = new CellRangeAddress[requiredWidth];
                    mergedRanges[r] = rowMerged;
                }
                else
                {
                    final int rowMergedLength = rowMerged.length;
                    if ( rowMergedLength < requiredWidth )
                    {
                        final CellRangeAddress[] newRow = new CellRangeAddress[requiredWidth];
                        System.arraycopy( rowMerged, 0, newRow, 0,
                                rowMergedLength );

                        mergedRanges[r] = newRow;
                        rowMerged = newRow;
                    }
                }

                Arrays.fill( rowMerged, cellRangeAddress.getFirstColumn(),
                        cellRangeAddress.getLastColumn() + 1, cellRangeAddress );
            }
        }
        return mergedRanges;
    }

}
