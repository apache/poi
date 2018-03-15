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

package org.apache.poi.ss.usermodel;

/**
 * The enumeration value indicating the style of fill pattern being used for a cell format.
 * 
 */
public enum FillPatternType {
    
    /**  No background */
     NO_FILL(0),

    /**  Solidly filled */
     SOLID_FOREGROUND(1),

    /**  Small fine dots */
     FINE_DOTS(2),

    /**  Wide dots */
     ALT_BARS(3),

    /**  Sparse dots */
     SPARSE_DOTS(4),

    /**  Thick horizontal bands */
     THICK_HORZ_BANDS(5),

    /**  Thick vertical bands */
     THICK_VERT_BANDS(6),

    /**  Thick backward facing diagonals */
     THICK_BACKWARD_DIAG(7),

    /**  Thick forward facing diagonals */
     THICK_FORWARD_DIAG(8),

    /**  Large spots */
     BIG_SPOTS(9),

    /**  Brick-like layout */
     BRICKS(10),

    /**  Thin horizontal bands */
     THIN_HORZ_BANDS(11),

    /**  Thin vertical bands */
     THIN_VERT_BANDS(12),

    /**  Thin backward diagonal */
     THIN_BACKWARD_DIAG(13),

    /**  Thin forward diagonal */
     THIN_FORWARD_DIAG(14),

    /**  Squares */
     SQUARES(15),

    /**  Diamonds */
     DIAMONDS(16),

    /**  Less Dots */
     LESS_DOTS(17),

    /**  Least Dots */
     LEAST_DOTS(18);
     
     /** Codes are used by ExtendedFormatRecord in HSSF */
     private final short code;
     private FillPatternType(int code) {
         this.code = (short) code;
     }
     
     public short getCode() {
         return code;
     }
     
     private final static int length = values().length;
     public static FillPatternType forInt(int code) {
         if (code < 0 || code > length) {
             throw new IllegalArgumentException("Invalid FillPatternType code: " + code);
         }
         return values()[code];
     }
     // it may also make sense to have an @Internal method to convert STPatternType.Enum
     // but may cause errors if poi-ooxml.jar is not on the classpath

}
