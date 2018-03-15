/*
 *  ====================================================================
 *    Licensed to the Apache Software Foundation (ASF) under one or more
 *    contributor license agreements.  See the NOTICE file distributed with
 *    this work for additional information regarding copyright ownership.
 *    The ASF licenses this file to You under the Apache License, Version 2.0
 *    (the "License"); you may not use this file except in compliance with
 *    the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 * ====================================================================
 */

package org.apache.poi.ss.usermodel;

/**
 * @author Yegor Kozlov
 */
public interface PatternFormatting {
    /**  No background */
    public final static short     NO_FILL             = 0  ;
    /**  Solidly filled */
    public final static short     SOLID_FOREGROUND    = 1  ;
    /**  Small fine dots */
    public final static short     FINE_DOTS           = 2  ;
    /**  Wide dots */
    public final static short     ALT_BARS            = 3  ;
    /**  Sparse dots */
    public final static short     SPARSE_DOTS         = 4  ;
    /**  Thick horizontal bands */
    public final static short     THICK_HORZ_BANDS    = 5  ;
    /**  Thick vertical bands */
    public final static short     THICK_VERT_BANDS    = 6  ;
    /**  Thick backward facing diagonals */
    public final static short     THICK_BACKWARD_DIAG = 7  ;
    /**  Thick forward facing diagonals */
    public final static short     THICK_FORWARD_DIAG  = 8  ;
    /**  Large spots */
    public final static short     BIG_SPOTS           = 9  ;
    /**  Brick-like layout */
    public final static short     BRICKS              = 10 ;
    /**  Thin horizontal bands */
    public final static short     THIN_HORZ_BANDS     = 11 ;
    /**  Thin vertical bands */
    public final static short     THIN_VERT_BANDS     = 12 ;
    /**  Thin backward diagonal */
    public final static short     THIN_BACKWARD_DIAG  = 13 ;
    /**  Thin forward diagonal */
    public final static short     THIN_FORWARD_DIAG   = 14 ;
    /**  Squares */
    public final static short     SQUARES             = 15 ;
    /**  Diamonds */
    public final static short     DIAMONDS            = 16 ;
    /**  Less Dots */
    public final static short     LESS_DOTS           = 17 ;
    /**  Least Dots */
    public final static short     LEAST_DOTS          = 18 ;

    short getFillBackgroundColor();
    short getFillForegroundColor();
    Color getFillBackgroundColorColor();
    Color getFillForegroundColorColor();

    short getFillPattern();

    void setFillBackgroundColor(short bg);
    void setFillForegroundColor(short fg);
    void setFillBackgroundColor(Color bg);
    void setFillForegroundColor(Color fg);

    void setFillPattern(short fp);
}
