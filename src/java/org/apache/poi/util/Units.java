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
    public static final int EMU_PER_PIXEL = 9525;
    public static final int EMU_PER_POINT = 12700;

    /**
     * Converts points to EMUs
     * @param points points
     * @return emus
     */
    public static int toEMU(double points){
        return (int)Math.round(EMU_PER_POINT*points);
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
     * @param fixedPoint
     * @return floating point (double)
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd910765(v=office.12).aspx">[MS-OSHARED] - 2.2.1.6 FixedPoint</a>
     */
    public static double fixedPointToDouble(int fixedPoint) {
        int i = (fixedPoint >> 16);
        int f = (fixedPoint >> 0) & 0xFFFF;
        double floatPoint = (i + f/65536d);
        return floatPoint;
    }
    
    /**
     * Converts a value of type floating point to a FixedPoint
     *
     * @param floatPoint
     * @return fixedPoint
     * 
     * @see <a href="http://msdn.microsoft.com/en-us/library/dd910765(v=office.12).aspx">[MS-OSHARED] - 2.2.1.6 FixedPoint</a>
     */
    public static int doubleToFixedPoint(double floatPoint) {
        int i = (int)Math.floor(floatPoint);
        int f = (int)((floatPoint % 1d)*65536d);
        int fixedPoint = (i << 16) | (f & 0xFFFF);
        return fixedPoint;
    }
}
