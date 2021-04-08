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

package org.apache.poi.xddf.usermodel;

public class Angles {
    /**
     * OOXML represents an angle in 60,000ths of a degree.
     *
     * Positive angles are clockwise (i.e., towards the positive y axis);
     * negative angles are counter-clockwise (i.e., towards the negative y axis).
     */
    public static final int OOXML_DEGREE = 60_000;

    public static final int degreesToAttribute(double angle) {
        return Math.toIntExact(Math.round(OOXML_DEGREE * angle));
    }

    public static final double attributeToDegrees(int angle) {
        return angle / ((double) OOXML_DEGREE);
    }
}
