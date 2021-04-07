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

package org.apache.poi.xssf.binary;


import org.apache.poi.util.Internal;
import org.apache.poi.util.LittleEndian;
import org.apache.poi.util.LittleEndianConsts;

/**
 * @since 3.16-beta3
 */
@Internal
class XSSFBCellRange {
    //TODO: Convert this to generate an AreaReference

    public static final int length = 4* LittleEndianConsts.INT_SIZE;
    /**
     * Parses an RfX cell range from the data starting at the offset.
     * This performs no range checking.
     * @param data raw bytes
     * @param offset offset at which to start reading from data
     * @param cellRange to overwrite. If null, a new cellRange will be created.
     * @return a mutable cell range.
     */
    public static XSSFBCellRange parse(byte[] data, int offset, XSSFBCellRange cellRange) {
        if (cellRange == null) {
            cellRange = new XSSFBCellRange();
        }
        cellRange.firstRow = XSSFBUtils.castToInt(LittleEndian.getUInt(data, offset)); offset += LittleEndianConsts.INT_SIZE;
        cellRange.lastRow = XSSFBUtils.castToInt(LittleEndian.getUInt(data, offset)); offset += LittleEndianConsts.INT_SIZE;
        cellRange.firstCol = XSSFBUtils.castToInt(LittleEndian.getUInt(data, offset)); offset += LittleEndianConsts.INT_SIZE;
        cellRange.lastCol = XSSFBUtils.castToInt(LittleEndian.getUInt(data, offset));

        return cellRange;
    }

    int firstRow;
    int lastRow;
    int firstCol;
    int lastCol;


}
