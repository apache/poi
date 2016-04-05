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
 * High level representation for Border Formatting component
 * of Conditional Formatting settings
 */
public interface BorderFormatting {
    /** No border
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_NONE                = 0x0;
    
    /** Thin border
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_THIN                = 0x1;
    
    /** Medium border
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_MEDIUM              = 0x2;
    
    /** dash border
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_DASHED              = 0x3;
    
    /** dot border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_DOTTED              = 0x4;
    
    /** Thick border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_THICK               = 0x5;
    
    /** double-line border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_DOUBLE              = 0x6;
    
    /** hair-line border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_HAIR                = 0x7;
    
    /** Medium dashed border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_MEDIUM_DASHED       = 0x8;
    
    
    /** dash-dot border
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_DASH_DOT            = 0x9;
    
    /** medium dash-dot border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_MEDIUM_DASH_DOT     = 0xA;
    
    /** dash-dot-dot border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_DASH_DOT_DOT        = 0xB;
    
    /** medium dash-dot-dot border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_MEDIUM_DASH_DOT_DOT = 0xC;
    
    /** slanted dash-dot border 
     * @deprecated 3.15 beta 1. Use {@link BorderStyle}
     */
    short BORDER_SLANTED_DASH_DOT    = 0xD;

    BorderStyle getBorderBottom();

    BorderStyle getBorderDiagonal();

    BorderStyle getBorderLeft();

    BorderStyle getBorderRight();

    BorderStyle getBorderTop();

    short getBottomBorderColor();
    Color getBottomBorderColorColor();

    short getDiagonalBorderColor();
    Color getDiagonalBorderColorColor();

    short getLeftBorderColor();
    Color getLeftBorderColorColor();

    short getRightBorderColor();
    Color getRightBorderColorColor();

    short getTopBorderColor();
    Color getTopBorderColorColor();

    /**
     * Set bottom border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 1
     */
    void setBorderBottom(short border);

    /**
     * Set bottom border.
     *
     * @param border
     */
    void setBorderBottom(BorderStyle border);
    
    /**
     * Set diagonal border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 1
     */
    void setBorderDiagonal(short border);
    
    /**
     * Set diagonal border.
     *
     * @param border
     */
    void setBorderDiagonal(BorderStyle border);

    /**
     * Set left border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 1
     */
    void setBorderLeft(short border);
    
    /**
     * Set left border.
     *
     * @param border
     */
    void setBorderLeft(BorderStyle border);

    /**
     * Set right border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 1
     */
    void setBorderRight(short border);
    
    /**
     * Set right border.
     *
     * @param border
     */
    void setBorderRight(BorderStyle border);

    /**
     * Set top border.
     *
     * @param border  MUST be a BORDER_* constant
     * @deprecated 3.15 beta 1
     */
    void setBorderTop(short border);
    
    /**
     * Set top border.
     *
     * @param border
     */
    void setBorderTop(BorderStyle border);

    void setBottomBorderColor(short color);
    void setBottomBorderColor(Color color);

    void setDiagonalBorderColor(short color);
    void setDiagonalBorderColor(Color color);

    void setLeftBorderColor(short color);
    void setLeftBorderColor(Color color);

    void setRightBorderColor(short color);
    void setRightBorderColor(Color color);

    void setTopBorderColor(short color);
    void setTopBorderColor(Color color);
}
