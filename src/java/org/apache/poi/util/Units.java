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
package org.apache.poi.util;

/**
 * @author Yegor Kozlov
 */
public class Units {
    /**
     * In Escher absolute distances are specified in
     * English Metric Units (EMUs), occasionally referred to as A units;
     * there are 360000 EMUs per centimeter, 914400 EMUs per inch, 12700 EMUs per point.
     */
    public static final int EMU_PER_PIXEL = 9525;
    public static final int EMU_PER_POINT = 12700;
    public static final int EMU_PER_CENTIMETER = 360000;

    /**
     * Master DPI (576 pixels per inch).
     * Used by the reference coordinate system in PowerPoint (HSLF)
     */
    public static final int MASTER_DPI = 576;    

    /**
     * Pixels DPI (96 pixels per inch)
     */
    public static final int PIXEL_DPI = 96;

    /**
     * Points DPI (72 pixels per inch)
     */
    public static final int POINT_DPI = 72;    


    /**
     * Width of one "standard character" of the default font in pixels. Same for Calibri and Arial.
     * "Standard character" defined as the widest digit character in the given font.
     * Copied from XSSFWorkbook, since that isn't available here.
     * <p>
     * Note this is only valid for workbooks using the default Excel font.
     * <p>
     * Would be nice to eventually support arbitrary document default fonts.
     */
    public static final float DEFAULT_CHARACTER_WIDTH = 7.0017f;

    /**
     * Column widths are in fractional characters, this is the EMU equivalent.
     * One character is defined as the widest value for the integers 0-9 in the 
     * default font.
     */
    public static final int EMU_PER_CHARACTER = (int) (EMU_PER_PIXEL * DEFAULT_CHARACTER_WIDTH);

    /**
     * Converts points to EMUs
     * @param points points
     * @return EMUs
     */
    public static int toEMU(double points){
        return (int)Math.rint(EMU_PER_POINT*points);
    }
    
    /**
     * Converts pixels to EMUs
     * @param pixels pixels
     * @return EMUs
     */
    public static int pixelToEMU(int pixels) {
        return pixels*EMU_PER_PIXEL;
    }

    /**
     * Converts EMUs to points
     * @param emu emu
     * @return points
     */
    public static double toPoints(long emu){
        return (double)emu/EMU_PER_POINT;
    }
    
    /**
     * Converts a value of type FixedPoint to a floating point
     *
     * @param fixedPoint value in fixed point notation
     * @return floating point (double)
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd910765(v=office.12).aspx">[MS-OSHARED] - 2.2.1.6 FixedPoint</a>
     */
    public static double fixedPointToDouble(int fixedPoint) {
        int i = (fixedPoint >> 16);
        int f = fixedPoint & 0xFFFF;
        return (i + f/65536d);
    }
    
    /**
     * Converts a value of type floating point to a FixedPoint
     *
     * @param floatPoint value in floating point notation
     * @return fixedPoint value in fixed points notation
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd910765(v=office.12).aspx">[MS-OSHARED] - 2.2.1.6 FixedPoint</a>
     */
    public static int doubleToFixedPoint(double floatPoint) {
        double fractionalPart = floatPoint % 1d;
        double integralPart = floatPoint - fractionalPart;
        int i = (int)Math.floor(integralPart);
        int f = (int)Math.rint(fractionalPart*65536d);
        return (i << 16) | (f & 0xFFFF);
    }

    public static double masterToPoints(int masterDPI) {
        double points = masterDPI;
        points *= POINT_DPI;
        points /= MASTER_DPI;
        return points;
    }

    public static int pointsToMaster(double points) {
        points *= MASTER_DPI;
        points /= POINT_DPI;
        return (int)Math.rint(points);
    }
    
    public static int pointsToPixel(double points) {
        points *= PIXEL_DPI;
        points /= POINT_DPI;
        return (int)Math.rint(points);
    }

    public static double pixelToPoints(int pixel) {
        double points = pixel;
        points *= POINT_DPI;
        points /= PIXEL_DPI;
        return points;
    }
    
    public static int charactersToEMU(double characters) {
        return (int) characters * EMU_PER_CHARACTER;
    }
    
    /**
     * @param columnWidth specified in 256ths of a standard character
     * @return equivalent EMUs
     */
    public static int columnWidthToEMU(int columnWidth) {
        return charactersToEMU(columnWidth / 256d);
    }
    
    /**
     * @param twips (1/20th of a point) typically used for row heights
     * @return equivalent EMUs
     */
    public static int TwipsToEMU(short twips) {
        return (int) (twips / 20d * EMU_PER_POINT);
    }
}
