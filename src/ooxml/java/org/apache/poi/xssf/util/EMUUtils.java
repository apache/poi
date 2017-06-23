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
package org.apache.poi.xssf.util;

import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Conversion methods for OOXML EMU values - "English Metric Units" or more accurately
 * "Evil Measurement Units".
 * <p/>
 * These are defined briefly in <a href="https://en.wikipedia.org/wiki/Office_Open_XML_file_formats#DrawingML">Wikipedia</a>
 * as a "rational" way to use an integer value to represent something that could be in 
 * inches, centimeters, points, or pixels.
 * So now we get to convert between all those.
 */
public class EMUUtils {
    public static final int EMUS_PER_INCH = 914400;
    public static final int EMUS_PER_POINT = 12700;
    public static final int EMUS_PER_CENTIMETER = 360000;
    
    // TODO: these could move here or something to standardize definitions
    public static final int EMU_PER_PIXEL = XSSFShape.EMU_PER_PIXEL;
    public static final int EMU_PER_POINT = XSSFShape.EMU_PER_POINT;
    public static final int PIXEL_DPI = XSSFShape.PIXEL_DPI;
    public static final int POINT_DPI = XSSFShape.POINT_DPI;
    public static final int EMU_PER_CHARACTER = (int) (EMU_PER_PIXEL * XSSFWorkbook.DEFAULT_CHARACTER_WIDTH);
    
    /**
     * @param columnWidth as (fractional # of characters) * 256
     * @return EMUs
     */
    public static final int EMUsFromColumnWidth(int columnWidth) {
        return (int) (columnWidth /256d * EMUUtils.EMU_PER_CHARACTER);
    }
    
    /**
     * @param twips (1/20th of a point) typically a row height
     * @return EMUs
     */
    public static final int EMUsFromTwips(short twips) {
        return (int) (twips / 20d * EMU_PER_POINT);
    }
    
    /**
     * @param points (fractional)
     * @return EMUs
     */
    public static final int EMUsFromPoints(float points) {
        return (int) (points * EMU_PER_POINT);
    }
}
