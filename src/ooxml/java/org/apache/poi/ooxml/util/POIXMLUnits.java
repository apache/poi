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

package org.apache.poi.ooxml.util;

import static org.apache.poi.util.Units.EMU_PER_DXA;
import static org.apache.poi.util.Units.EMU_PER_INCH;
import static org.apache.poi.util.Units.EMU_PER_POINT;

import java.util.Locale;

public class POIXMLUnits {

    /**
     * Office will read percentages formatted with a trailing percent sign or formatted
     * as 1000th of a percent without a trailing percent sign
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.main.STPositivePercentage pctUnion) {
        return parsePercentInner(pctUnion, 1);
    }

    /**
     * Office will read percentages formatted with a trailing percent sign or formatted
     * as 1000th of a percent without a trailing percent sign
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.main.STPositiveFixedPercentage pctUnion) {
        return parsePercentInner(pctUnion, 1);
    }

    /**
     * Office will read percentages formatted with a trailing percent sign or formatted
     * as 1000th of a percent without a trailing percent sign
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.main.STPercentage pctUnion) {
        return parsePercentInner(pctUnion, 1);
    }

    /**
     * Specifies the percentage of the text size that this bullet should be. It is specified here in terms of
     * 100% being equal to 100000 and 1% being specified in increments of 1000. This attribute should not be
     * lower than 25%, or 25000, and not be higher than 400%, or 400000.
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.main.STTextBulletSizePercent pctUnion) {
        return parsePercentInner(pctUnion, 1);
    }

    /**
     * Office will read percentages formatted with a trailing percent sign or formatted
     * as 1000th of a percent without a trailing percent sign
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.main.STTextSpacingPercentOrPercentString pctUnion) {
        return parsePercentInner(pctUnion, 1);
    }

    /**
     * Office will read percentages formatted with a trailing percent sign or formatted
     * as 1000th of a percent without a trailing percent sign
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.main.STTextFontScalePercentOrPercentString pctUnion) {
        return parsePercentInner(pctUnion, 1);
    }

    /**
     * This type seems to be interpreted as percent value when the trailing percent sign is missing.<br/>
     * sample snippet from settings.xml
     * <pre>
     *    &lt;w:zoom w:percent="50" /&gt;
     * <pre>
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.wordprocessingml.x2006.main.STDecimalNumberOrPercent pctUnion) {
        return parsePercentInner(pctUnion, 1000);
    }

    /**
     * This type seems to be interpreted as percent value when the trailing percent sign is missing.<br/>
     * sample snippet from settings.xml
     * <pre>
     *    &lt;w:textscale w:w="50" /&gt;
     * <pre>
     *
     * @return the percent scaled by 1000, so 100% = 100000
     */
    public static int parsePercent(org.openxmlformats.schemas.wordprocessingml.x2006.main.STTextScale pctUnion) {
        return parsePercentInner(pctUnion, 1000);
    }


    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.chart.STGapAmount pctUnion) {
        return parsePercentInner(pctUnion, 1000);
    }


    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.chart.STOverlap  pctUnion) {
        return parsePercentInner(pctUnion, 1000);
    }


    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.chart.STDepthPercent pctUnion) {
        return parsePercentInner(pctUnion, 1000);
    }


    public static int parsePercent(org.openxmlformats.schemas.drawingml.x2006.chart.STHPercent pctUnion) {
        return parsePercentInner(pctUnion, 1000);
    }


    private static int parsePercentInner(org.apache.xmlbeans.XmlAnySimpleType pctUnion, int noUnitScale) {
        String strVal = pctUnion.getStringValue();
        if (strVal.endsWith("%")) {
            return Integer.parseInt(strVal.substring(0, strVal.length()-1)) * 1000;
        } else {
            return Integer.parseInt(strVal) * noUnitScale;
        }
    }

    /**
     * The standard states that ST_Coordinate32 is read and written as either a length
     * followed by a unit, or EMUs with no unit present.
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.drawingml.x2006.main.STCoordinate32 coordUnion) {
        return parseLengthInner(coordUnion, 1d);
    }

    /**
     * The standard states that ST_Coordinate is read and written as either a length
     * followed by a unit, or EMUs with no unit present.
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.drawingml.x2006.main.STCoordinate coordUnion) {
        return parseLengthInner(coordUnion, 1d);
    }

    /**
     * The standard states that ST_TextPoint is read and written as a length followed by a unit
     * or as hundredths of a point with no unit present.
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.drawingml.x2006.main.STTextPoint coordUnion) {
        return parseLengthInner(coordUnion, EMU_PER_POINT/100d);
    }

    /**
     * If no unit is specified, a twips-measure is a twentieth of a point
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STTwipsMeasure coordUnion) {
        return parseLengthInner(coordUnion, EMU_PER_DXA);
    }

    /**
     * If no unit is specified, a twips-measure is a twentieth of a point
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.wordprocessingml.x2006.main.STSignedTwipsMeasure coordUnion) {
        return parseLengthInner(coordUnion, EMU_PER_DXA);
    }

    /**
     * If no unit is specified, a hps-measure is a half of a point
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.wordprocessingml.x2006.main.STHpsMeasure coordUnion) {
        return parseLengthInner(coordUnion, EMU_PER_POINT*2d);
    }


    /**
     * If no unit is specified, a hps-measure is a half of a point
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.wordprocessingml.x2006.main.STSignedHpsMeasure coordUnion) {
        return parseLengthInner(coordUnion, EMU_PER_POINT*2d);
    }


    /**
     * If not unit is specified, DXA (twentieth of a point) is assumed
     *
     * @return length in EMUs
     */
    public static long parseLength(org.openxmlformats.schemas.wordprocessingml.x2006.main.STMeasurementOrPercent coordUnion) {
        if (coordUnion.getStringValue().endsWith("%")) return -1;
        return parseLengthInner(coordUnion, EMU_PER_DXA);
    }


    /**
     * Returns the EMUs for the given measurment (mm|cm|in|pt|pc|pi, defaults to EMUs*noUnitEmuFactor if not specified)
     *
     * @param coordUnion the raw type
     * @return the EMUs for the given attribute
     */
    private static long parseLengthInner(org.apache.xmlbeans.XmlAnySimpleType coordUnion, double noUnitEmuFactor) {
        String strVal = coordUnion.getStringValue().toLowerCase(Locale.ROOT);
        double digVal = Double.parseDouble(strVal.replaceAll("(mm|cm|in|pt|pc|pi)", ""));
        long emu = 0;
        // http://startbigthinksmall.wordpress.com/2010/01/04/points-inches-and-emus-measuring-units-in-office-open-xml/
        if (strVal.endsWith("mm")) {
            emu = (long)(((digVal/10f)/2.54f)*EMU_PER_INCH);
        } else if (strVal.endsWith("cm")) {
            emu = (long)((digVal/2.54f)*EMU_PER_INCH);
        } else if (strVal.endsWith("in")) {
            emu = (long)(digVal*EMU_PER_INCH);
        } else if (strVal.endsWith("pc") || strVal.endsWith("pi")) {
            emu = (long)(digVal*0.166f*EMU_PER_INCH);
        } else if (strVal.endsWith("pt")) {
            emu = (long)(digVal*EMU_PER_POINT);
        } else {
            emu = (long)(digVal*noUnitEmuFactor);
        }
        return emu;
    }



    public static boolean parseOnOff(org.openxmlformats.schemas.wordprocessingml.x2006.main.CTOnOff onOff) {
        if(onOff == null) return false;
        if(!onOff.isSetVal()) return true;
        return parseOnOff(onOff.xgetVal());
    }

    public static boolean parseOnOff(org.openxmlformats.schemas.officeDocument.x2006.sharedTypes.STOnOff onOff) {
        if (onOff == null) return false;
        String str = onOff.getStringValue();
        return ("true".equalsIgnoreCase(str) || "on".equalsIgnoreCase(str) || "x".equalsIgnoreCase(str) || "1".equals(str));
    }

}
